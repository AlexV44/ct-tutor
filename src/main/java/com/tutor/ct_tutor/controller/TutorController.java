package com.tutor.ct_tutor.controller;

import com.tutor.ct_tutor.dto.request.TutorRequest;
import com.tutor.ct_tutor.dto.response.DashboardStatsResponse;
import com.tutor.ct_tutor.dto.response.TutorResponse;
import com.tutor.ct_tutor.dto.response.UserProgressResponse;
import com.tutor.ct_tutor.entity.User;
import com.tutor.ct_tutor.service.AnalyticsService;
import com.tutor.ct_tutor.service.TelegramAuthService;
import com.tutor.ct_tutor.service.TutorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Tag(name = "Tutor API", description = "Эндпоинты ИИ-тьютора и аналитики")
public class TutorController {

    private final TutorService tutorService;
    private final AnalyticsService analyticsService;
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
            return ResponseEntity.badRequest().body(new TutorResponse(null, e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(new TutorResponse(null, "Ошибка сервера: " + e.getMessage()));
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
}