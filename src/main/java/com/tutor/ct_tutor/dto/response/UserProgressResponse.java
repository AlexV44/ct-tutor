package com.tutor.ct_tutor.dto.response;

import java.time.LocalDateTime;

public record UserProgressResponse(
        String id,
        String subject,
        String userMessage,
        String aiResponse,
        LocalDateTime createdAt
) {}