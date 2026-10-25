package com.tutor.ct_tutor.service;

import com.tutor.ct_tutor.dto.openrouter.OpenRouterMessage;
import com.tutor.ct_tutor.dto.openrouter.OpenRouterRequest;
import com.tutor.ct_tutor.dto.request.TutorRequest;
import com.tutor.ct_tutor.dto.response.TutorResponse;
import com.tutor.ct_tutor.entity.TutorRequestEntity;
import com.tutor.ct_tutor.entity.User;
import com.tutor.ct_tutor.repository.TutorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TutorService {

    private final TutorRepository tutorRepository;
    private final OpenRouterClient openRouterClient; // Ваш WebClient/RestTemplate клиент

    public static final String TUTOR_SYSTEM_PROMPT =
            "Ты — мягкий, поддерживающий ИИ-тьютор по математике для подготовки к ЦТ/ЦЭ. Твоя цель — провести диагностику, составить план и вести ученика к решению задач через метод Сократа.\n\n" +
                    "СТАРТ (ДИАГНОСТИКА И ПЛАН):\n" +
                    "В начале диалога не переходи сразу к задачам. Проведи короткий опрос (по 1–2 вопроса за раз):\n" +
                    "1. Выясни цель (желаемый балл на ЦТ/ЦЭ) и сколько времени есть до экзамена.\n" +
                    "2. Оцени текущий уровень (какие темы даются легко, а где чаще всего ошибки).\n" +
                    "3. На основе ответов предложи простой пошаговый план подготовки по разделам.\n\n" +
                    "ОБУЧЕНИЕ (МЕТОД СОКРАТА):\n" +
                    "1. Тон: Дружелюбный, эмпатичный, на «ты». Хвали за попытки, не критикуй за ошибки.\n" +
                    "2. Без готовых решений: Не пиши ответ и полное решение. Задавай по 1–2 простых наводящих вопроса за раз.\n" +
                    "3. Пошаговость: Разбивай любую задачу на микро-шаги (Условие/ОДЗ → План → Вычисления → Проверка).\n" +
                    "4. Краткость: Ответы до 3 коротких предложений.\n" +
                    "5. Ловушки ЦТ/ЦЭ: Напоминай про ОДЗ, форматы бланка и частые ошибки.";

    public TutorResponse processRequest(User user, TutorRequest request) {
        try {
            List<OpenRouterMessage> messages = new ArrayList<>();

            // 1. Добавляем системную инструкцию
            String systemContent = TUTOR_SYSTEM_PROMPT;
            if (user.getContextSummary() != null && !user.getContextSummary().isBlank()) {
                systemContent += "\n\nРезюме предыдущего общения:\n" + user.getContextSummary();
            }
            messages.add(new OpenRouterMessage("system", systemContent));

            // 2. Достаем последние 5 диалогов из БД для актуального контекста
            List<TutorRequestEntity> history = tutorRepository.findTop5ByUserOrderByCreatedAtDesc(user);
            Collections.reverse(history); // Приводим к хронологическому порядку

            for (TutorRequestEntity item : history) {
                messages.add(new OpenRouterMessage("user", item.getUserMessage()));
                if (item.getAiResponse() != null) {
                    messages.add(new OpenRouterMessage("assistant", item.getAiResponse()));
                }
            }

            // 3. Добавляем новое сообщение пользователя из запроса
            messages.add(new OpenRouterMessage("user", request.message()));

            // 4. Запрос к OpenRouter API
            OpenRouterRequest openRouterRequest = new OpenRouterRequest("anthropic/claude-sonnet-5.5", messages);
            String aiReply = openRouterClient.send(openRouterRequest);

            // 5. Сохраняем новую запись в БД
            TutorRequestEntity entity = new TutorRequestEntity();
            entity.setUser(user);
            entity.setSubject(request.subject());
            entity.setUserMessage(request.message());
            entity.setAiResponse(aiReply);
            tutorRepository.save(entity);

            return new TutorResponse(aiReply, null);

        } catch (Exception e) {
            return new TutorResponse(null, "Ошибка при обработке запроса: " + e.getMessage());
        }
    }
}