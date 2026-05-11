package com.coma.comaroom.utils;

import com.coma.comaroom.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Response<Void>> handleBusinessException(BusinessException ex) {
        Response<Void> response = Response.errorResponse(ex.getErrorCode());
        return response.toResponseEntity();
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Response<Void>> handleNoHandlerFound(NoHandlerFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}
