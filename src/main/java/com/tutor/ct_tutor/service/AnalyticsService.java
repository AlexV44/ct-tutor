package com.tutor.ct_tutor.service;

import com.tutor.ct_tutor.dto.response.DashboardStatsResponse;
import com.tutor.ct_tutor.dto.response.UserProgressResponse;
import com.tutor.ct_tutor.entity.TutorRequestEntity;
import com.tutor.ct_tutor.entity.User;
import com.tutor.ct_tutor.repository.TutorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final TutorRepository tutorRepository;
    private final TelegramAuthService telegramAuthService;

    /**
     * Получить историю обращений ученика к ИИ
     */
    public List<UserProgressResponse> getUserHistory(String initData) {
        User user = telegramAuthService.authenticateAndGet(initData);
        List<TutorRequestEntity> requests = tutorRepository.findByUserOrderByCreatedAtDesc(user);

        return requests.stream()
                .map(req -> new UserProgressResponse(
                        req.getId().toString(),
                        req.getSubject(),
                        req.getUserMessage(),
                        req.getAiResponse(),
                        req.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Получить общую статистику для дашборда родителя/учителя
     */
    public DashboardStatsResponse getDashboardStats(String initData) {
        User user = telegramAuthService.authenticateAndGet(initData);

        // Для MVP рассчитываем базовые метрики на основе реальных данных из базы
        List<TutorRequestEntity> requests = tutorRepository.findByUserOrderByCreatedAtDesc(user);
        long total = requests.size();

        return new DashboardStatsResponse(
                total,
                total > 0 ? 3 : 0, // заглушка активных дней для демонстрации
                "68%", // пример текущей готовности к ЦТ
                total > 0 ? requests.get(0).getSubject() : "Математика"
        );
    }
}
