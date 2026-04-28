package com.coma.comaroom.vote;

import com.coma.comaroom.utils.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum VoteError implements ErrorCode {
    VOTE_NOT_FOUND("VOTE-001", HttpStatus.BAD_REQUEST, "존재하지 않는 투표입니다"),
    VOTE_RESULT_NOT_FOUND("VOTE-002", HttpStatus.BAD_REQUEST, "투표 내역이 없습니다");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}
