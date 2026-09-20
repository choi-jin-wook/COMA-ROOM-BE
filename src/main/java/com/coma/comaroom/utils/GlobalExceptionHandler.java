package com.coma.comaroom.utils;

import com.coma.comaroom.BusinessException;
import com.coma.comaroom.member.AuthError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.http.converter.HttpMessageNotReadableException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Response<Void>> handleBusinessException(BusinessException ex) {
        Response<Void> response = Response.errorResponse(ex.getErrorCode());
        return response.toResponseEntity();
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Response<Void>> handleInvalidRequestBody(HttpMessageNotReadableException ex) {
        return handleBusinessException(new BusinessException(AuthError.INVALID_REQUEST_BODY));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Response<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        return handleBusinessException(new BusinessException(AuthError.INVALID_REQUEST_PARAM));
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Response<Void>> handleNoHandlerFound(NoHandlerFoundException ex) {
        return handleBusinessException(new BusinessException(AuthError.API_NOT_FOUND));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Response<Void>> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        return handleBusinessException(new BusinessException(AuthError.METHOD_NOT_ALLOWED));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Response<Void>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        log.warn("데이터 무결성 위반", ex);
        return handleBusinessException(new BusinessException(AuthError.INVALID_REQUEST_PARAM));
    }

    // 예상하지 못한 예외가 스택트레이스 그대로 노출되지 않도록 하는 최종 방어선
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response<Void>> handleUnexpectedException(Exception ex) {
        log.error("처리되지 않은 예외 발생", ex);
        Response<Void> response = Response.<Void>builder()
                .code("GEN-999")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("서버 내부 오류가 발생했습니다")
                .build();
        return response.toResponseEntity();
    }
}
