package com.coma.comaroom.utils;

import org.springframework.http.HttpStatus;

public interface ErrorCode {
    String getCode();

    HttpStatus getHttpStatus();

    String getMessage();
}
