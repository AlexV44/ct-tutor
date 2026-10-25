package com.tutor.ct_tutor.dto.request;

public record TutorRequest(
        String subject,
        String message,
        String initData
) {}