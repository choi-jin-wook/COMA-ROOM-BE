package com.coma.comaroom.auth;

import com.coma.comaroom.utils.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum AuthError implements ErrorCode {
    MEMBER_NOT_FOUND("AUTH-001", HttpStatus.BAD_REQUEST, "로그인 후 이용해주세요");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;

    AuthError(String code, HttpStatus httpStatus, String message) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
