package com.coma.comaroom.auth;

import com.coma.comaroom.member.AuthError;
import com.coma.comaroom.utils.Response;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class LoginFailureHandler implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
        Response<Void> errorResponse = Response.errorResponse(AuthError.LOGIN_FAIL);

        response.setStatus(errorResponse.getStatus());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String json = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(json);
    }
}
