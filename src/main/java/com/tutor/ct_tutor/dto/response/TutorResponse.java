package com.tutor.ct_tutor.dto.response;

public record TutorResponse(
        String reply,
        String error,
        String imageUrl // Поле для ссылки на чертеж/график (может быть null)
) {}
