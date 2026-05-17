package com.coma.comaroom.utils;

import com.coma.comaroom.BusinessException;
import com.coma.comaroom.member.AuthError;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.http.converter.HttpMessageNotReadableException;

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
}
