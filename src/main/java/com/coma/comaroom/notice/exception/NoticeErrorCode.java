package com.coma.comaroom.notice.exception;

import com.coma.comaroom.utils.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum NoticeErrorCode implements ErrorCode {
    NOTICE_NOT_FOUND("NOTICE-001", HttpStatus.BAD_REQUEST, "존재하지 않는 공지입니다"), EXCEEDED_PINNED_LIMIT("NOTICE-002",HttpStatus.BAD_REQUEST , "고정 가능한 공지는 최대 3개입니다");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;

    NoticeErrorCode(String code, HttpStatus httpStatus, String message) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
