package com.coma.comaroom.auth.jwt;

import com.coma.comaroom.auth.CustomUserDetails;
import com.coma.comaroom.member.entity.Member;
import com.coma.comaroom.member.entity.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class JwtTokenFilter extends OncePerRequestFilter {

    @Value("${jwt.secret}")
    private String secretKeyString;
    private SecretKey key;

    @PostConstruct
    protected void init() {
        this.key = Keys.hmacShaKeyFor(secretKeyString.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = resolveToken(request);

        if (token != null && token.contains(".")) {
            try {
                Claims claims = Jwts.parser()
                        .verifyWith(key)
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();

                String subject = claims.getSubject();
                String roleStr = claims.get("role", String.class);
                Long memberId = Long.valueOf(subject);

                // role 클레임이 없는 토큰(리프레시 토큰 등)은 인증 컨텍스트를 설정하지 않음
                if (roleStr == null) {
                    filterChain.doFilter(request, response);
                    return;
                }

                Long xp = claims.get("xp") != null ? Long.valueOf(claims.get("xp").toString()) : 0L;

                Member member = Member.builder()
                        .memberId(memberId)
                        .studentId(subject)
                        .role(Role.valueOf(roleStr))
                        .xp(xp)
                        .build();

                CustomUserDetails customUserDetails = new CustomUserDetails(member);

                Authentication auth = new UsernamePasswordAuthenticationToken(
                        customUserDetails,
                        null,
                        customUserDetails.getAuthorities()
                );

                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (JwtException | IllegalArgumentException e) {
                // 유효하지 않거나 만료된 토큰 → 401
                SecurityContextHolder.clearContext();
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}