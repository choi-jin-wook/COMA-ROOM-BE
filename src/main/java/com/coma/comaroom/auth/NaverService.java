package com.coma.comaroom.auth;

import com.coma.comaroom.auth.jwt.JwtTokenProvider;
import com.coma.comaroom.member.AuthError;
import com.coma.comaroom.member.dto.response.LoginResponse;
import com.coma.comaroom.member.entity.Member;
import com.coma.comaroom.member.repository.MemberRepository;
import com.coma.comaroom.utils.PhoneNumberNormalizer;
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
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NaverService extends SimpleUrlAuthenticationSuccessHandler {

    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;
    private final OAuthLoginCodeService oauthLoginCodeService;

    @org.springframework.beans.factory.annotation.Value("${app.oauth.frontend-success-url}")
    private String frontendSuccessUrl;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        Map<String, Object> naverResponse = oauth2User.getAttribute("response");
        String phoneNumber = PhoneNumberNormalizer.toKoreanLocalFormat(
                naverResponse == null ? null : (String) naverResponse.get("mobile")
        );

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

        String loginCode = oauthLoginCodeService.issue(loginResponse);
        response.sendRedirect(frontendSuccessUrl + "#loginCode=" + loginCode);
    }
}
