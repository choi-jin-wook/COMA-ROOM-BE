package com.coma.comaroom.event;

import com.coma.comaroom.utils.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum EventError implements ErrorCode {
    EVENT_NOT_FOUND("EVENT-001", HttpStatus.BAD_REQUEST, "존재하지 않는 이벤트입니다"),
    NOT_STUDY_MEMBER("EVENT-002", HttpStatus.BAD_REQUEST, "스터디원이 아닙니다"),
    INVALID_QR_CODE("EVENT-003", HttpStatus.BAD_REQUEST, "출석이 만료되었거나 유효한 출석이 아닙니다");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}
