package com.coma.comaroom.event;

import com.coma.comaroom.utils.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum EventError implements ErrorCode {
    EVENT_NOT_FOUND("EVENT-001", HttpStatus.BAD_REQUEST, "존재하지 않는 이벤트입니다"),
    NOT_STUDY_MEMBER("EVENT-002", HttpStatus.BAD_REQUEST, "스터디원이 아닙니다");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}
