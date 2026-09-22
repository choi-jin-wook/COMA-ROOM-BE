package com.coma.comaroom.auth;

import com.coma.comaroom.BusinessException;
import com.coma.comaroom.member.AuthError;
import com.coma.comaroom.member.dto.response.LoginResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OAuthLoginCodeServiceTest {

    @Mock private StringRedisTemplate redisTemplate;
    @Mock private ValueOperations<String, String> valueOperations;
    @Mock private ObjectMapper objectMapper;

    private OAuthLoginCodeService service;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        service = new OAuthLoginCodeService(redisTemplate, objectMapper);
    }

    @Test
    void issueStoresLoginResponseWithOneMinuteTtl() throws Exception {
        LoginResponse loginResponse = LoginResponse.builder().accessToken("access").build();
        when(objectMapper.writeValueAsString(loginResponse)).thenReturn("payload");
        when(valueOperations.setIfAbsent(anyString(), eq("payload"), eq(Duration.ofSeconds(60))))
                .thenReturn(true);

        String loginCode = service.issue(loginResponse);

        assertThat(loginCode).hasSize(43);
        verify(valueOperations).setIfAbsent(
                eq("oauth:login:" + loginCode),
                eq("payload"),
                eq(Duration.ofSeconds(60))
        );
    }

    @Test
    void exchangeReadsAndDeletesCodeAtomically() throws Exception {
        LoginResponse expected = LoginResponse.builder().accessToken("access").build();
        when(valueOperations.getAndDelete("oauth:login:code")).thenReturn("payload");
        when(objectMapper.readValue("payload", LoginResponse.class)).thenReturn(expected);

        LoginResponse actual = service.exchange("code");

        assertThat(actual).isSameAs(expected);
    }

    @Test
    void exchangeRejectsExpiredOrAlreadyUsedCode() {
        when(valueOperations.getAndDelete("oauth:login:expired")).thenReturn(null);

        assertThatThrownBy(() -> service.exchange("expired"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(AuthError.OAUTH_LOGIN_CODE_INVALID);
    }
}
