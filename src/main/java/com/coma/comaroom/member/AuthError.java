package com.coma.comaroom.member;

import com.coma.comaroom.utils.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum AuthError implements ErrorCode {
    MEMBER_NOT_FOUND("AUTH-001", HttpStatus.BAD_REQUEST, "로그인 후 이용해주세요"),
    LOGIN_FAIL("AUTH-002", HttpStatus.BAD_REQUEST, "아이디 혹은 비밀번호를 확인해주세요"),
    NOT_ADMIN("AUTH-003", HttpStatus.BAD_REQUEST, "관리자가 아닙니다"),
    MEMBER_ALREADY_EXISTS("AUTH-005", HttpStatus.CONFLICT, "이미 가입된 회원입니다"),
    INVALID_TOKEN("AUTH-004", HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다");


    private final String code;
    private final HttpStatus httpStatus;
    private final String message;

    AuthError(String code, HttpStatus httpStatus, String message) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
