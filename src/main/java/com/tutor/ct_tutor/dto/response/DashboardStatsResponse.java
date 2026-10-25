package com.tutor.ct_tutor.dto.response;

public record DashboardStatsResponse(
        long totalRequests,
        long activeDays,
        String averageReadiness,
        String topSubject
) {}