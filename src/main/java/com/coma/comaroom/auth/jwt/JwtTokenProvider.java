package com.coma.comaroom.auth.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final String secretKeyString = "your-256-bit-secret-key-must-be-at-least-32-characters-long";
    private SecretKey key;

    @PostConstruct
    protected void init() {
        this.key = Keys.hmacShaKeyFor(secretKeyString.getBytes(StandardCharsets.UTF_8));
    }

    // 액세스 토큰 발급 (30분)
    public String createAccessToken(Long memberId, String role) {
        return createToken(memberId, role, 30 * 60 * 1000L);
    }

    // 리프레시 토큰 발급 (14일)
    public String createRefreshToken(Long memberId) {
        // 리프레시는 권한 정보를 넣지 않는 것이 일반적입니다.
        return createToken(memberId, null, 14 * 24 * 60 * 60 * 1000L);
    }

    private String createToken(Long memberId, String role, long validity) {
        Date now = new Date();
        var builder = Jwts.builder()
                .subject(String.valueOf(memberId))
                .issuedAt(now)
                .expiration(new Date(now.getTime() + validity))
                .signWith(key);

        if (role != null) builder.claim("role", role);

        return builder.compact();
    }
}