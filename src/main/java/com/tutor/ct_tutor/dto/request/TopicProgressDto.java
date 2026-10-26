package com.tutor.ct_tutor.dto.request;

import java.util.UUID;

public record TopicProgressDto(
        UUID topicId,
        String title,
        int orderIndex,
        String status // "done", "in_progress", "locked"
) {}
