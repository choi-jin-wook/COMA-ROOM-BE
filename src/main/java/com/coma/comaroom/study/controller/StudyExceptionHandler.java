package com.coma.comaroom.study.controller;

import com.coma.comaroom.study.StudyError;
import com.coma.comaroom.utils.Response;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.HttpMediaTypeNotSupportedException;

@Order(-1)
@RestControllerAdvice(basePackageClasses = StudyController.class)
public class StudyExceptionHandler {
    @ExceptionHandler({MissingServletRequestParameterException.class, MethodArgumentTypeMismatchException.class,
            MissingServletRequestPartException.class})
    public ResponseEntity<Response<Void>> invalidInput(Exception exception) {
        return Response.<Void>errorResponse(StudyError.INVALID_INPUT).toResponseEntity();
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Response<Void>> tooLarge(MaxUploadSizeExceededException exception) {
        return Response.<Void>errorResponse(StudyError.FILE_TOO_LARGE).toResponseEntity();
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Response<Void>> unsupportedType(HttpMediaTypeNotSupportedException exception) {
        return Response.<Void>errorResponse(StudyError.UNSUPPORTED_FILE).toResponseEntity();
    }
}
