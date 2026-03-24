package com.coma.comaroom.study;

import com.coma.comaroom.utils.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum StudyError implements ErrorCode {
    STUDY_NOT_FOUND("STUDY-001", HttpStatus.NOT_FOUND, "존재하지 않는 스터디입니다."),
    MEMBER_NOT_FOUND("STUDY-002", HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."),
    ALREADY_STUDY_MEMBER("STUDY-003", HttpStatus.BAD_REQUEST, "이미 스터디 멤버입니다."),
    NOT_STUDY_MEMBER("STUDY-004", HttpStatus.BAD_REQUEST, "해당 스터디의 멤버가 아닙니다."),
    ACTIVITY_NOT_FOUND("STUDY-005", HttpStatus.NOT_FOUND, "존재하지 않는 스터디 일정입니다."),
    ACTIVITY_NOT_IN_STUDY("STUDY-006", HttpStatus.BAD_REQUEST, "해당 스터디의 일정이 아닙니다.");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}
