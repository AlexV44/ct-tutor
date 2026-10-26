package com.tutor.ct_tutor.service;

import com.tutor.ct_tutor.dto.openrouter.OpenRouterMessage;
import com.tutor.ct_tutor.dto.openrouter.OpenRouterRequest;
import com.tutor.ct_tutor.dto.request.TutorRequest;
import com.tutor.ct_tutor.dto.response.TutorResponse;
import com.tutor.ct_tutor.entity.*;
import com.tutor.ct_tutor.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TutorService {

    private final TutorRepository tutorRepository;
    private final SubjectRepository subjectRepository;
    private final TopicRepository topicRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final TaskRepository taskRepository;         // Репозиторий для векторного поиска задач
    private final EmbeddingService embeddingService;     // Сервис генерации эмбедингов
    private final OpenRouterClient openRouterClient;     // Клиент для запросов к LLM

    private final String IMAGES_HOST_URL = "http://localhost:8080/images/";
    private final String AI_MODEL = "anthropic/claude-sonnet-5.5";

    public TutorResponse processRequest(User user, TutorRequest request) {
        try {
            String subjectSlug = request.subject();
            String userMessage = request.message();

            // 1. Получаем предмет и темы
            Subject subject = subjectRepository.findBySlug(subjectSlug)
                    .orElseThrow(() -> new RuntimeException("Предмет не найден: " + subjectSlug));

            List<Topic> topics = topicRepository.findBySubjectIdOrderByOrderIndexAsc(subject.getId());
            String topicsListString = topics.stream()
                    .map(t -> "- " + t.getTitle())
                    .collect(Collectors.joining("\n"));

            // 2. Получаем текущий профиль ученика
            StudentProfile profile = studentProfileRepository.findByStudentIdAndSubjectSlug(user.getId(), subjectSlug)
                    .orElseGet(() -> {
                        StudentProfile newProfile = new StudentProfile();
                        newProfile.setStudentId(user.getId());
                        newProfile.setSubjectSlug(subjectSlug);
                        newProfile.setAiProfile("Портрет еще не сформирован. Идет первичное знакомство.");
                        return studentProfileRepository.save(newProfile);
                    });

            // 3. ВЕКТОРНЫЙ ПОИСК (Vector RAG): Ищем релевантные задачи в базе по смыслу сообщения
            String retrievedContext = "";
            String resolvedImageUrl = null; // Переменная для хранения ссылки на картинку первой найденной задачи

            try {
                float[] queryVector = embeddingService.getEmbedding(userMessage);
                if (queryVector != null && queryVector.length > 0) {
                    String vectorString = Arrays.toString(queryVector);
                    List<Task> relevantTasks = taskRepository.findSimilarTasks(subjectSlug, vectorString);

                    if (!relevantTasks.isEmpty()) {
                        StringBuilder sb = new StringBuilder("\n\n### Релевантные материалы и задачи из базы по смыслу:\n");
                        for (int i = 0; i < relevantTasks.size(); i++) {
                            Task task = relevantTasks.get(i);
                            sb.append("- Условие задачи: ").append(task.getQuestionText()).append("\n")
                                    .append("  Варианты ответов: ").append(task.getOptions()).append("\n")
                                    .append("  Правильный ответ: ").append(task.getCorrectAnswer()).append("\n")
                                    .append("  Подсказка для разбора: ").append(task.getSolutionExplanation()).append("\n");

                            // Если у задачи есть картинка и мы еще не зафиксировали картинку для ответа (берем у первой релевантной)
                            if (i == 0 && task.getImagePath() != null && !task.getImagePath().isBlank()) {
                                // Укажите ваш актуальный базовый URL / домен сервера
                                resolvedImageUrl = IMAGES_HOST_URL + task.getImagePath();
                                sb.append("  [Системная пометка: к этой задаче прикреплен графический чертеж/график]\n");
                            }
                        }
                        retrievedContext = sb.toString();
                    }
                }
            } catch (Exception e) {
                System.err.println("Ошибка выполнения Vector RAG поиска: " + e.getMessage());
            }

            // 4. Собираем системный промпт с учетом портрета и RAG-контекста задач
            String baseSystemPrompt = buildSystemPrompt(subject.getName(), topicsListString, profile);
            String finalSystemPrompt = baseSystemPrompt + retrievedContext;

            List<OpenRouterMessage> messages = new ArrayList<>();
            messages.add(new OpenRouterMessage("system", finalSystemPrompt));

            // 5. Достаем историю для контекста чата
            List<TutorRequestEntity> history = tutorRepository.findTop5ByUserAndSubject(user, subjectSlug);
            Collections.reverse(history);

            for (TutorRequestEntity item : history) {
                messages.add(new OpenRouterMessage("user", item.getUserMessage()));
                if (item.getAiResponse() != null) {
                    messages.add(new OpenRouterMessage("assistant", item.getAiResponse()));
                }
            }

            messages.add(new OpenRouterMessage("user", userMessage));

            // 6. Запрос к OpenRouter API за ответом тьютора
            OpenRouterRequest openRouterRequest = new OpenRouterRequest(AI_MODEL, messages);
            String aiReply = openRouterClient.send(openRouterRequest);

            // 7. Сохраняем сообщение в историю БД
            TutorRequestEntity entity = new TutorRequestEntity();
            entity.setUser(user);
            entity.setSubject(subjectSlug);
            entity.setUserMessage(userMessage);
            entity.setAiResponse(aiReply);
            tutorRepository.save(entity);

            // 8. АВТОМАТИЧЕСКОЕ ОБНОВЛЕНИЕ ПОРТРЕТА КАЖДЫЕ 5 СООБЩЕНИЙ
            CompletableFuture.runAsync(() -> checkAndUpdateAiProfile(user, subjectSlug));

            // 9. Возвращаем ответ, включая ссылку на картинку (если она была в задаче)
            return new TutorResponse(aiReply, null, resolvedImageUrl);

        } catch (Exception e) {
            return new TutorResponse(null, "Ошибка при обработке запроса: " + e.getMessage(), null);
        }
    }

    // Метод для анализа диалога и генерации ai_profile
    private void checkAndUpdateAiProfile(User user, String subjectSlug) {
        long totalMessages = tutorRepository.countByUserAndSubject(user, subjectSlug);

        // Обновляем портрет каждые 5 сообщений
        if (totalMessages > 0 && totalMessages % 5 == 0) {
            try {
                List<TutorRequestEntity> fullHistory = tutorRepository.findTop5ByUserAndSubject(user, subjectSlug);
                StringBuilder historyText = new StringBuilder();
                for (TutorRequestEntity item : fullHistory) {
                    historyText.append("Ученик: ").append(item.getUserMessage()).append("\n");
                    historyText.append("Тьютор: ").append(item.getAiResponse()).append("\n");
                }

                List<OpenRouterMessage> profileMessages = new ArrayList<>();
                profileMessages.add(new OpenRouterMessage("system",
                        "Ты — методист и аналитик образования. На основе диалога ученика с тьютором составь краткий когнитивный портрет ученика (в 2-3 предложениях): его текущий уровень подготовки, пробелы в знаниях и сильные стороны. Пиши строго от третьего лица на русском языке."));
                profileMessages.add(new OpenRouterMessage("user", "История диалога:\n" + historyText.toString()));

                OpenRouterRequest profileReq = new OpenRouterRequest(AI_MODEL, profileMessages);
                String newProfileText = openRouterClient.send(profileReq);

                // Сохраняем обновленный портрет в БД
                StudentProfile profile = studentProfileRepository.findByStudentIdAndSubjectSlug(user.getId(), subjectSlug)
                        .orElseGet(() -> {
                            StudentProfile p = new StudentProfile();
                            p.setStudentId(user.getId());
                            p.setSubjectSlug(subjectSlug);
                            return p;
                        });

                profile.setAiProfile(newProfileText.trim());
                studentProfileRepository.save(profile);
            } catch (Exception e) {
                System.err.println("Не удалось обновить AI-профиль: " + e.getMessage());
            }
        }
    }

    public void updateProfile(UUID studentId, String subjectSlug, Integer targetScore, Integer examDaysLeft) {
        StudentProfile profile = studentProfileRepository.findByStudentIdAndSubjectSlug(studentId, subjectSlug)
                .orElseGet(() -> {
                    StudentProfile newProfile = new StudentProfile();
                    newProfile.setStudentId(studentId);
                    newProfile.setSubjectSlug(subjectSlug);
                    newProfile.setAiProfile("Портрет еще не сформирован. Идет первичное знакомство.");
                    return newProfile;
                });

        if (targetScore != null) {
            profile.setTargetScore(targetScore);
        }
        if (examDaysLeft != null) {
            profile.setExamDaysLeft(examDaysLeft);
        }
        profile.setUpdatedAt(java.time.LocalDateTime.now());

        studentProfileRepository.save(profile);
    }

    private String buildSystemPrompt(String subjectName, String topicsList, StudentProfile profile) {
        return "Ты — мягкий, поддерживающий ИИ-тьютор по предмету «" + subjectName + "» для подготовки к ЦТ/ЦЭ в Беларуси.\n" +
                "Твоя цель — помочь ученику подготовиться на максимальный балл, опираясь на официальные темы и ведя его через метод Сократа.\n\n" +
                "ОФИЦИАЛЬНЫЙ СПИСОК ТЕМ ДЛЯ ИЗУЧЕНИЯ:\n" + topicsList + "\n\n" +
                "ТЕКУЩИЙ ПРОФИЛЬ УЧЕНИКА:\n" +
                "- Желаемый балл: " + (profile.getTargetScore() != null ? profile.getTargetScore() : "не указан") + "\n" +
                "- Дней до экзамена: " + (profile.getExamDaysLeft() != null ? profile.getExamDaysLeft() : "не указано") + "\n" +
                "- Психологический/когнитивный портрет: " + profile.getAiProfile() + "\n\n" +
                "ПРАВИЛА И МЕТОДОЛОГИЯ:\n" +
                "1. СТАРТ: В начале диалога проведи короткий разогрев (1–2 вопроса): уточни слабые места ученика. Не спрашивай о баллах и времени до экзамена(эту инфу ученик введет во вкладке прогресс).\n" +
                "2. ОБУЧЕНИЕ (МЕТОД СОКРАТА): Дружелюбный тон на «ты». Не пиши готовых решений или полных ответов. Задавай по 1–2 наводящих вопроса за раз.\n" +
                "3. ПОШАГОВОСТЬ: Разбивай любые задачи на микро-шаги (Условие/ОДЗ → План → Вычисления → Проверка).\n" +
                "4. Краткость: Ответы до 3–4 коротких предложений.\n" +
                "5. Ловушки ЦТ/ЦЭ: Регулярно и ненавязчиво напоминай про ОДЗ, форматы бланков и частые ошибки.\n" +
                "6. Не используй смайлики и эмодзи.";
    }
}