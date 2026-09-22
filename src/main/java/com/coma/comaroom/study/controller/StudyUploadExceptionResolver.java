package com.coma.comaroom.study.controller;

import com.coma.comaroom.study.StudyError;
import com.coma.comaroom.utils.Response;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.AbstractHandlerExceptionResolver;
import tools.jackson.databind.ObjectMapper;
import java.io.IOException;

/** multipart 크기 오류는 컨트롤러 선택 전에 발생하므로 요청 경로로 범위를 제한한다. */
@Component
public class StudyUploadExceptionResolver extends AbstractHandlerExceptionResolver {
    private final ObjectMapper mapper;

    public StudyUploadExceptionResolver(ObjectMapper mapper) {
        this.mapper = mapper;
        setOrder(-1);
    }

    @Override
    protected ModelAndView doResolveException(HttpServletRequest request, HttpServletResponse response,
                                             Object handler, Exception exception) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        if (!(exception instanceof MaxUploadSizeExceededException) || !path.startsWith("/api/studies/")) {
            return null;
        }
        response.setStatus(413);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        try {
            response.getWriter().write(mapper.writeValueAsString(Response.errorResponse(StudyError.FILE_TOO_LARGE)));
            return new ModelAndView();
        } catch (IOException ex) {
            return null;
        }
    }
}
