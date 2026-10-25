package com.tutor.ct_tutor.service;

import com.tutor.ct_tutor.entity.User;
import com.tutor.ct_tutor.entity.enums.Role;
import com.tutor.ct_tutor.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TelegramAuthService {

    private final UserRepository userRepository;

    @Value("${telegram.bot.token}")
    private String botToken;

    /**
     * Валидирует initData от Telegram Mini App и возвращает или создает пользователя в БД
     */
    public User authenticateAndGet(String initData) {
        if (initData == null || initData.isBlank()) {
            throw new IllegalArgumentException("Telegram initData отсутствует");
        }

        Map<String, String> parsedData = parseInitData(initData);
        String hash = parsedData.remove("hash");

        if (hash == null || !validateHash(parsedData, hash, botToken)) {
            throw new SecurityException("Недействительная подпись Telegram InitData");
        }

        // Извлекаем данные пользователя из строки user (JSON-формат)
        String userJson = parsedData.get("user");
        if (userJson == null) {
            throw new IllegalArgumentException("Данные пользователя не найдены в initData");
        }

        Long telegramId = extractTelegramId(userJson);
        String username = extractField(userJson, "username");
        String firstName = extractField(userJson, "first_name");
        String lastName = extractField(userJson, "last_name");

        // Ищем или создаем пользователя в БД
        return userRepository.findByTelegramId(telegramId)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .telegramId(telegramId)
                            .username(username)
                            .firstName(firstName)
                            .lastName(lastName)
                            .role(Role.STUDENT) // По умолчанию студент
                            .build();
                    return userRepository.save(newUser);
                });
    }

    private Map<String, String> parseInitData(String initData) {
        Map<String, String> map = new LinkedHashMap<>();
        String[] pairs = initData.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            if (idx > 0) {
                String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
                String value = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
                map.put(key, value);
            }
        }
        return map;
    }

    private boolean validateHash(Map<String, String> data, String receivedHash, String token) {
        try {
            List<String> keys = new ArrayList<>(data.keySet());
            Collections.sort(keys);

            StringBuilder dataCheckString = new StringBuilder();
            for (int i = 0; i < keys.size(); i++) {
                String key = keys.get(i);
                dataCheckString.append(key).append("=").append(data.get(key));
                if (i < keys.size() - 1) {
                    dataCheckString.append("\n");
                }
            }

            Mac sha256HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec("WebAppData".getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256HMAC.init(secretKey);
            byte[] secret = sha256HMAC.doFinal(token.getBytes(StandardCharsets.UTF_8));

            Mac sha256HMAC2 = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey2 = new SecretKeySpec(secret, "HmacSHA256");
            sha256HMAC2.init(secretKey2);
            byte[] hashBytes = sha256HMAC2.doFinal(dataCheckString.toString().getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString().equalsIgnoreCase(receivedHash);

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            return false;
        }
    }

    private Long extractTelegramId(String userJson) {
        String idStr = extractField(userJson, "id");
        return idStr != null ? Long.parseLong(idStr) : 0L;
    }

    private String extractField(String json, String field) {
        String pattern = "\"" + field + "\":";
        int idx = json.indexOf(pattern);
        if (idx == -1) return null;
        int start = idx + pattern.length();
        char firstChar = json.charAt(start);
        int end;
        if (firstChar == '"') {
            start++;
            end = json.indexOf("\"", start);
        } else {
            end = json.indexOf(",", start);
            if (end == -1) end = json.indexOf("}", start);
        }
        return json.substring(start, end);
    }
}
