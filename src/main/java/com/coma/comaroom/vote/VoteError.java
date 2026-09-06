package com.coma.comaroom.vote;

import com.coma.comaroom.utils.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum VoteError implements ErrorCode {
    VOTE_NOT_FOUND("VOTE-001", HttpStatus.BAD_REQUEST, "존재하지 않는 투표입니다"),
    VOTE_RESULT_NOT_FOUND("VOTE-002", HttpStatus.BAD_REQUEST, "투표 내역이 없습니다"),
    VOTE_CLOSED("VOTE-003", HttpStatus.BAD_REQUEST, "마감된 투표입니다"),
    ALREADY_VOTED("VOTE-004", HttpStatus.BAD_REQUEST, "이미 참여한 투표입니다"),
    MULTI_VOTE_NOT_ALLOWED("VOTE-005", HttpStatus.BAD_REQUEST, "복수 선택이 불가능한 투표입니다"),
    VOTE_OPTION_NOT_FOUND("VOTE-006", HttpStatus.BAD_REQUEST, "존재하지 않는 투표 항목입니다");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}
