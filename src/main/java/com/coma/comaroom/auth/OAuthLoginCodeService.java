package com.coma.comaroom.auth;

import com.coma.comaroom.BusinessException;
import com.coma.comaroom.member.AuthError;
import com.coma.comaroom.member.dto.response.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class OAuthLoginCodeService {

    private static final String KEY_PREFIX = "oauth:login:";
    private static final Duration CODE_TTL = Duration.ofSeconds(60);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public String issue(LoginResponse loginResponse) {
        try {
            String payload = objectMapper.writeValueAsString(loginResponse);

            for (int attempt = 0; attempt < 3; attempt++) {
                byte[] randomBytes = new byte[32];
                SECURE_RANDOM.nextBytes(randomBytes);
                String loginCode = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

                Boolean stored = redisTemplate.opsForValue()
                        .setIfAbsent(KEY_PREFIX + loginCode, payload, CODE_TTL);
                if (Boolean.TRUE.equals(stored)) {
                    return loginCode;
                }
            }
        } catch (Exception exception) {
            throw new IllegalStateException("OAuth 로그인 코드 발급에 실패했습니다.", exception);
        }

        throw new IllegalStateException("OAuth 로그인 코드를 생성하지 못했습니다.");
    }

    public LoginResponse exchange(String loginCode) {
        String payload = redisTemplate.opsForValue().getAndDelete(KEY_PREFIX + loginCode);
        if (payload == null) {
            throw new BusinessException(AuthError.OAUTH_LOGIN_CODE_INVALID);
        }

        try {
            return objectMapper.readValue(payload, LoginResponse.class);
        } catch (Exception exception) {
            throw new BusinessException(AuthError.OAUTH_LOGIN_CODE_INVALID);
        }
    }
}
