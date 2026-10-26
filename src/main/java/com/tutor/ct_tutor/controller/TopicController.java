package com.tutor.ct_tutor.controller;

import com.tutor.ct_tutor.dto.request.TopicProgressDto;
import com.tutor.ct_tutor.entity.User;
import com.tutor.ct_tutor.service.TelegramAuthService;
import com.tutor.ct_tutor.service.TopicService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/topics")
@RequiredArgsConstructor
public class TopicController {

    private final TopicService topicService;
    private final TelegramAuthService telegramAuthService;

    @GetMapping
    public ResponseEntity<List<TopicProgressDto>> getTopics(
            @RequestParam String initData,
            @RequestParam String subject) {
        User user = telegramAuthService.authenticateAndGet(initData);
        return ResponseEntity.ok(topicService.getStudentTopics(user, subject));
    }

    /**
     * Вызывается, когда ученик кликает/начинает тему — она автоматически становится "in_progress"
     */
    @PostMapping("/start")
    public ResponseEntity<List<TopicProgressDto>> startTopic(
            @RequestParam String initData,
            @RequestParam String subject,
            @RequestParam UUID topicId) {
        User user = telegramAuthService.authenticateAndGet(initData);
        return ResponseEntity.ok(topicService.startTopic(user, subject, topicId));
    }

    /**
     * Вызывается ИИ-тьютором или при успешном завершении блока — тема становится "done", а следующая "in_progress"
     */
    @PostMapping("/complete")
    public ResponseEntity<List<TopicProgressDto>> completeTopic(
            @RequestParam String initData,
            @RequestParam String subject,
            @RequestParam UUID topicId) {
        User user = telegramAuthService.authenticateAndGet(initData);
        return ResponseEntity.ok(topicService.completeTopicAutomatically(user, subject, topicId));
    }
}