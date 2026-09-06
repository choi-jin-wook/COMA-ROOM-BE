package com.coma.comaroom.auth.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider provider;

    @BeforeEach
    void setUp() {
        provider = new JwtTokenProvider();
        ReflectionTestUtils.setField(provider, "secretKeyString",
                "test-secret-key-must-be-at-least-32-characters-long!");
        provider.init();
    }

    @Test
    @DisplayName("액세스 토큰 생성 및 검증 성공")
    void createAccessToken_validToken() {
        String token = provider.createAccessToken(1L, "USER", "202012345");

        assertThat(provider.validateToken(token)).isTrue();
        assertThat(provider.getMemberId(token)).isEqualTo(1L);
        assertThat(provider.getRole(token)).isEqualTo("USER");
        assertThat(provider.getStudentId(token)).isEqualTo("202012345");
    }

    @Test
    @DisplayName("[보안] 액세스 토큰 유효기간은 정확히 30분(1800초)이다")
    void createAccessToken_expiresIn30Minutes() {
        String token = provider.createAccessToken(1L, "USER", "202012345");

        // JWT payload는 서명 없이 Base64 디코딩 가능
        String payload = new String(Base64.getUrlDecoder().decode(token.split("\\.")[1]));
        long iat = extractLong(payload, "iat");
        long exp = extractLong(payload, "exp");
        long validitySeconds = exp - iat;

        assertThat(validitySeconds).isEqualTo(30 * 60);
    }

    @Test
    @DisplayName("리프레시 토큰 생성 및 검증 성공")
    void createRefreshToken_validToken() {
        String token = provider.createRefreshToken(1L);

        assertThat(provider.validateToken(token)).isTrue();
        assertThat(provider.getMemberId(token)).isEqualTo(1L);
        assertThat(provider.getRole(token)).isNull();
    }

    @Test
    @DisplayName("잘못된 토큰은 검증 실패")
    void validateToken_invalidToken_returnsFalse() {
        assertThat(provider.validateToken("invalid.token.here")).isFalse();
    }

    private long extractLong(String json, String key) {
        Matcher m = Pattern.compile("\"" + key + "\":(\\d+)").matcher(json);
        assertThat(m.find()).as("JWT payload에 '%s' 클레임이 없음", key).isTrue();
        return Long.parseLong(m.group(1));
    }
}
