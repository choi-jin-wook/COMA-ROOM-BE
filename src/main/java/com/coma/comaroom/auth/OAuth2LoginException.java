package com.coma.comaroom.auth;

import com.coma.comaroom.utils.ErrorCode;
import lombok.Getter;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;

@Getter
public class OAuth2LoginException extends OAuth2AuthenticationException {

    private final ErrorCode errorCode;

    public OAuth2LoginException(ErrorCode errorCode) {
        super(new OAuth2Error(errorCode.getCode()), errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
