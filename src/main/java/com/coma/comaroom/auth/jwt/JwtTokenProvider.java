package com.coma.comaroom.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKeyString;
    private SecretKey key;

    @PostConstruct
    protected void init() {
        this.key = Keys.hmacShaKeyFor(secretKeyString.getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(Long memberId, String role, String studentId) {
        return createToken(memberId, role, studentId, 30 * 60 * 1000L);
    }

    public String createRefreshToken(Long memberId) {
        return createToken(memberId, null, null, 14 * 24 * 60 * 60 * 1000L);
    }

    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Long getMemberId(String token) {
        return Long.valueOf(getClaims(token).getSubject());
    }

    public String getRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    public String getTokenType(String token) {
        return getClaims(token).get("tokenType", String.class);
    }

    public String getStudentId(String token) {
        return getClaims(token).get("studentId", String.class);
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private String createToken(Long memberId, String role, String studentId, long validity) {
        Date now = new Date();
        var builder = Jwts.builder()
                .subject(String.valueOf(memberId))
                .issuedAt(now)
                .expiration(new Date(now.getTime() + validity))
                .signWith(key);

        if (role != null) {
            builder.claim("tokenType", "access");
            builder.claim("role", role);
            builder.claim("studentId", studentId);
        } else {
            builder.claim("tokenType", "refresh");
        }

        return builder.compact();
    }
}