package com.coma.comaroom.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CommonError implements ErrorCode {
    INVALID_REQUEST("GEN-400", HttpStatus.BAD_REQUEST, "Invalid request"),
    RESOURCE_NOT_FOUND("GEN-404", HttpStatus.NOT_FOUND, "Resource not found");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}
