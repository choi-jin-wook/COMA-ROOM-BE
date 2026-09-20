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
    INVALID_QR_CODE("EVENT-003", HttpStatus.BAD_REQUEST, "출석이 만료되었거나 유효한 출석이 아닙니다"),
    APPROVAL_NOT_FOUND("EVENT-004", HttpStatus.BAD_REQUEST, "존재하지 않는 XP 승인 요청입니다"),
    ALREADY_ATTENDED("EVENT-005", HttpStatus.CONFLICT, "이미 출석 처리된 회원입니다"),
    APPROVAL_ALREADY_DECIDED("EVENT-007", HttpStatus.CONFLICT, "이미 처리된 XP 승인 요청입니다"),
    ATTENDANCE_NOT_FOUND("EVENT-006", HttpStatus.BAD_REQUEST, "출석 기록이 존재하지 않습니다");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}
