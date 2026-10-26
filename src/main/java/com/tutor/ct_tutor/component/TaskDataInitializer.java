package com.tutor.ct_tutor.component;

import com.tutor.ct_tutor.entity.Task;
import com.tutor.ct_tutor.repository.TaskRepository;
import com.tutor.ct_tutor.service.EmbeddingService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TaskDataInitializer implements CommandLineRunner {

    private final TaskRepository taskRepository;
    private final EmbeddingService embeddingService;

    @Override
    public void run(String... args) {
        // Проверяем, если база задач пуста — добавляем стартовый набор
        if (taskRepository.count() == 0) {
            System.out.println("Инициализация банка задач для Vector RAG...");

            saveTask(
                    2285,
                    "math",
                    "Укажите номер промежутка, которому принадлежит число 5π.",
                    "1) (-∞; 15), 2) (-∞; 14), 3) [17; +∞), 4) (15; +∞), 5) (-∞; 15)",
                    "4",
                    "Число π ≈ 3.14159. Тогда 5π ≈ 15.707. Это число принадлежит промежутку (15; +∞)."
            );

            saveTask(
                    2286,
                    "math",
                    "На рисунке изображены подобные треугольники АВС и A1B1C1. Используя данные рисунка, найдите длину стороны B1C1 треугольника A1B1C1.",
                    "1) 4, 2) 6, 3) 8, 4) 7, 5) 9",
                    "4",
                    "Треугольники подобны. Используйте отношение сходственных сторон для расчета неизвестной длины."
            );

            System.out.println("Банк задач успешно загружен и векторизован!");
        }
    }

    private void saveTask(int number, String subjectSlug, String question, String options, String answer, String explanation) {
        Task task = new Task();
        task.setTaskNumber(number);
        task.setSubjectSlug(subjectSlug);
        task.setQuestionText(question);
        task.setOptions(options);
        task.setCorrectAnswer(answer);
        task.setSolutionExplanation(explanation);

        // Автоматически отправляем текст вопроса на API эмбедингов и сохраняем полученный вектор
        float[] vector = embeddingService.getEmbedding(question);
        task.setEmbedding(vector);

        taskRepository.save(task);
    }
}