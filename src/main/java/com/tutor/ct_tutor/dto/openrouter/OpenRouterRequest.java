package com.tutor.ct_tutor.dto.openrouter;

import java.util.List;

public record OpenRouterRequest(
        String model,
        List<OpenRouterMessage> messages
) {}
