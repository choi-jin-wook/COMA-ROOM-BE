package com.coma.comaroom.notice.exception;

import com.coma.comaroom.utils.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum NoticeError implements ErrorCode {
    NOTICE_NOT_FOUND("NOTICE-001", HttpStatus.BAD_REQUEST, "공지를 찾을 수 없습니다");
    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}
