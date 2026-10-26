package com.tutor.ct_tutor.controller;

import com.tutor.ct_tutor.dto.request.TutorRequest;
import com.tutor.ct_tutor.dto.request.UpdateProfileRequest;
import com.tutor.ct_tutor.dto.response.DashboardStatsResponse;
import com.tutor.ct_tutor.dto.response.TutorResponse;
import com.tutor.ct_tutor.dto.response.UserProgressResponse;
import com.tutor.ct_tutor.entity.User;
import com.tutor.ct_tutor.repository.StudentProfileRepository;
import com.tutor.ct_tutor.service.AnalyticsService;
import com.tutor.ct_tutor.service.TelegramAuthService;
import com.tutor.ct_tutor.service.TutorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Tag(name = "Tutor API", description = "Эндпоинты ИИ-тьютора и аналитики")
public class TutorController {

    private final TutorService tutorService;
    private final AnalyticsService analyticsService;
    private final StudentProfileRepository studentProfileRepository;
    private final TelegramAuthService telegramAuthService; // Внедряем сервис авторизации Telegram

    @Operation(summary = "Отправить задание ИИ-тьютору (Метод Сократа)")
    @PostMapping("/tutor")
    public ResponseEntity<TutorResponse> askTutor(@RequestBody TutorRequest request) {
        try {
            // 1. Авторизуем пользователя по initData из DTO
            User user = telegramAuthService.authenticateAndGet(request.initData());

            // 2. Передаем и пользователя, и запрос в TutorService
            TutorResponse response = tutorService.processRequest(user, request);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            // Исправлено: передаем три аргумента (reply, error, imageUrl)
            return ResponseEntity.badRequest().body(new TutorResponse(null, e.getMessage(), null));
        } catch (Exception e) {
            e.printStackTrace();
            // Исправлено: передаем три аргумента (reply, error, imageUrl)
            return ResponseEntity.status(500).body(new TutorResponse(null, "Ошибка сервера: " + e.getMessage(), null));
        }
    }

    @Operation(summary = "Получить историю решенных задач ученика")
    @GetMapping("/analytics/history")
    public ResponseEntity<?> getHistory(@RequestHeader("X-Telegram-Init-Data") String initData) {
        try {
            List<UserProgressResponse> history = analyticsService.getUserHistory(initData);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Ошибка авторизации: " + e.getMessage());
        }
    }

    @Operation(summary = "Получить статистику для дашборда родителя/учителя")
    @GetMapping("/analytics/stats")
    public ResponseEntity<?> getStats(@RequestHeader("X-Telegram-Init-Data") String initData) {
        try {
            DashboardStatsResponse stats = analyticsService.getDashboardStats(initData);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Ошибка авторизации: " + e.getMessage());
        }
    }

    @Operation(summary = "Обновить целевой балл и дни до экзамена ученика")
    @PostMapping("/profile/update")
    public ResponseEntity<?> updateProfile(@RequestBody UpdateProfileRequest request) {
        try {
            // 1. Авторизуем пользователя через Telegram initData
            User user = telegramAuthService.authenticateAndGet(request.initData());

            // 2. Обновляем данные в БД
            tutorService.updateProfile(user.getId(), request.subject(), request.targetScore(), request.examDaysLeft());

            return ResponseEntity.ok().body(java.util.Map.of("message", "Профиль успешно обновлен"));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(java.util.Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Получить профиль/цели ученика по предмету")
    @GetMapping("/exam-plan")
    public ResponseEntity<?> getExamPlan(@RequestParam String initData, @RequestParam String subject) {
        try {
            User user = telegramAuthService.authenticateAndGet(initData);

            Map<String, Object> response = new HashMap<>();
            response.put("subjectSlug", subject);

            // Ищем профиль в базе данных
            studentProfileRepository.findByStudentIdAndSubjectSlug(user.getId(), subject).ifPresentOrElse(profile -> {
                response.put("targetScore", profile.getTargetScore()); // Может быть null
                response.put("daysUntilExam", profile.getExamDaysLeft()); // Может быть null
                response.put("calculatedScore", profile.getTargetScore() != null ? Math.max(0, profile.getTargetScore() - 12) : null);
            }, () -> {
                // Если записи в БД вообще нет
                response.put("targetScore", null);
                response.put("daysUntilExam", null);
                response.put("calculatedScore", null);
            });

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Ошибка авторизации: " + e.getMessage()));
        }
    }
}