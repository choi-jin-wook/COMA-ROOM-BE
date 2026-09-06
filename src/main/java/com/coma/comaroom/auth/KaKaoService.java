package com.coma.comaroom.auth;

import com.coma.comaroom.auth.jwt.JwtTokenProvider;
import com.coma.comaroom.member.AuthError;
import com.coma.comaroom.member.dto.response.LoginResponse;
import com.coma.comaroom.member.entity.Member;
import com.coma.comaroom.member.repository.MemberRepository;
import com.coma.comaroom.utils.Response;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class KaKaoService extends SimpleUrlAuthenticationSuccessHandler {

    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        String phoneNumber = oauth2User.getAttribute("phone_number");

        Member member = memberRepository.findByPhoneNumber(phoneNumber).orElse(null);
        if (member == null) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(objectMapper.writeValueAsString(Response.errorResponse(AuthError.SOCIAL_LOGIN_MEMBER_NOT_FOUND)));
            return;
        }

        LoginResponse loginResponse = LoginResponse.builder()
                .accessToken(jwtTokenProvider.createAccessToken(
                        member.getMemberId(),
                        member.getRole().name(),
                        member.getStudentId()
                ))
                .refreshToken(jwtTokenProvider.createRefreshToken(member.getMemberId()))
                .message("로그인 성공")
                .role(member.getRole())
                .build();

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(Response.ok(loginResponse, HttpStatus.OK)));
    }

}
