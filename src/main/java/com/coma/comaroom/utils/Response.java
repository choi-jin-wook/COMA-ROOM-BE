package com.coma.comaroom.utils;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response<T> {

    private String code;
    private int status;
    private String message;
    private T data;

    // 성공 응답 (Data 없음)
    public static Response<Void> ok() {
        return Response.<Void>builder()
                .code("GEN-000")
                .status(HttpStatus.OK.value())
                .message("Success")
                .build();
    }

    // 성공 응답 (Data 포함)
    public static <T> Response<T> ok(T data, HttpStatus status) {
        return Response.<T>builder()
                .code("GEN-000")
                .status(status.value())
                .message("Success")
                .data(data)
                .build();
    }

    // 에러 응답 (ErrorCode ENUM 활용)
    public static <T> Response<T> errorResponse(ErrorCode errorCode) {
        return Response.<T>builder()
                .code(errorCode.getCode())
                .status(errorCode.getHttpStatus().value())
                .message(errorCode.getMessage())
                .build();
    }

    public ResponseEntity<Response<T>> toResponseEntity() {
        return ResponseEntity.status(this.status).body(this);
    }
}