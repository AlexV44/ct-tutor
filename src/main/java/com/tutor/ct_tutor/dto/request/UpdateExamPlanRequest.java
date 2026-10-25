package com.tutor.ct_tutor.dto.request;

import java.util.UUID;

public record UpdateExamPlanRequest(
        UUID studentId,
        String subjectSlug,
        Integer days,
        Integer score
) {}