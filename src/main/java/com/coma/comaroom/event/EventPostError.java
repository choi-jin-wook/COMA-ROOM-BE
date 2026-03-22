package com.coma.comaroom.event;

import com.coma.comaroom.utils.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum EventPostError implements ErrorCode {
    POST_NOT_FOUND("POST-001", HttpStatus.BAD_REQUEST, "게시글이 존재하지 않습니다."),
    UNAUTHORIZED_ACCESS("POST-002", HttpStatus.FORBIDDEN, "해당 게시글에 대한 권한이 없습니다."),
    INVALID_STATUS("POST-005", HttpStatus.BAD_REQUEST, "유효하지 않은 승인 상태값입니다."),
    INVALID_TITLE("POST-003", HttpStatus.BAD_REQUEST, "게시글 제목이 유효하지 않습니다.");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}
