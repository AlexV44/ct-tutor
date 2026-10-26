package com.tutor.ct_tutor.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tutor.ct_tutor.dto.openrouter.OpenRouterRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Component
public class OpenRouterClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${openrouter.api.key}")
    private String apiKey;

    @Value("${openrouter.api.url:https://openrouter.ai/api/v1/chat/completions}")
    private String apiUrl;

    // Spring автоматически подставит сюда ObjectMapper, если подключен spring-boot-starter-web
    public OpenRouterClient(ObjectMapper objectMapper) {
        this.restClient = RestClient.create();
        this.objectMapper = objectMapper;
    }

    @Retryable(
            retryFor = { HttpClientErrorException.TooManyRequests.class }, // Перехватываем ошибку 429
            maxAttempts = 3,                                                // Сделать до 3 попыток
            backoff = @Backoff(delay = 3000, multiplier = 2)                // Пауза 3 сек, при следующем повторе умножается на 2 (3 сек -> 6 сек)
    )
    public String send(OpenRouterRequest request) {
        try {
            String jsonResponse = restClient.post()
                    .uri(apiUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("HTTP-Referer", "https://ct-tutor.by")
                    .header("X-Title", "CT Tutor App")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(jsonResponse);
            return root.path("choices")
                    .get(0)
                    .path("message")
                    .path("content")
                    .asText();

        } catch (Exception e) {
            throw new RuntimeException("Ошибка при вызове OpenRouter API: " + e.getMessage(), e);
        }
    }
}