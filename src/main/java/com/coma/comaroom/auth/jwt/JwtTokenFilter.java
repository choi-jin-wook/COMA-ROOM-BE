package com.coma.comaroom.auth.jwt;

import com.coma.comaroom.auth.CustomUserDetails;
import com.coma.comaroom.member.entity.Member;
import com.coma.comaroom.member.entity.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

@Component
public class JwtTokenFilter extends OncePerRequestFilter {

    private final String secretKeyString = "your-256-bit-secret-key-must-be-at-least-32-characters-long";
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

                // 1. 토큰에서 정보 추출
                String studentId = claims.getSubject();
                String roleStr = claims.get("role", String.class);
                String subject = claims.getSubject();
                Long memberId = Long.valueOf(subject);

                // 토큰에 xp 정보가 포함되어 있다고 가정하거나, 없으면 0으로 세팅
                // 만약 토큰에 "xp" 클레임이 없다면 null 방지를 위해 0L 사용
                Long xp = claims.get("xp") != null ? Long.valueOf(claims.get("xp").toString()) : 0L;

                // 2. 쿼리 없이 Member 객체 생성 (Stub 객체)
                // @Builder가 있으므로 빌더를 사용합니다.
                Member member = Member.builder()
                        .memberId(memberId)
                        .studentId(studentId)
                        .role(Role.valueOf(roleStr))
                        .xp(xp) // 서비스 45번 라인 getXp() 대응
                        .build();

                // 3. CustomUserDetails를 사용하여 Principal 생성
                // 서비스 계층에서 CustomUserDetails 혹은 Member를 꺼낼 수 있게 함
                CustomUserDetails customUserDetails = new CustomUserDetails(member);

                // 4. 인증 객체 생성 시 principal에 customUserDetails 주입
                Authentication auth = new UsernamePasswordAuthenticationToken(
                        customUserDetails,
                        null,
                        customUserDetails.getAuthorities()
                );

                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (Exception e) {
                SecurityContextHolder.clearContext();
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