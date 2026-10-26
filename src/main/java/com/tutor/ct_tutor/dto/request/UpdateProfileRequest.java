package com.tutor.ct_tutor.dto.request;

public record UpdateProfileRequest(
        String initData,     // Данные авторизации Telegram
        String subject,      // Слаг предмета (например, "math")
        Integer targetScore, // Желаемый балл
        Integer examDaysLeft // Дней до экзамена
) {}