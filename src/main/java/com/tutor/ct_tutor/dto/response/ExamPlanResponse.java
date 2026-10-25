package com.tutor.ct_tutor.dto.response;

import java.util.UUID;

public record ExamPlanResponse(
        UUID studentId,
        String subjectSlug,
        String subjectName,
        Integer daysUntilExam,
        Integer targetScore
) {}