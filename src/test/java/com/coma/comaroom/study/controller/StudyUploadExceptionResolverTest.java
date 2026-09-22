package com.coma.comaroom.study.controller;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import tools.jackson.databind.ObjectMapper;
import static org.assertj.core.api.Assertions.assertThat;

class StudyUploadExceptionResolverTest {
    @Test
    void handlesOversizedStudyUploadsBeforeControllerSelectionOnly() throws Exception {
        var resolver = new StudyUploadExceptionResolver(new ObjectMapper());
        var response = new MockHttpServletResponse();
        var result = resolver.resolveException(new MockHttpServletRequest("POST", "/api/studies/1/weeks"),
                response, null, new MaxUploadSizeExceededException(1024));
        assertThat(result).isNotNull();
        assertThat(response.getStatus()).isEqualTo(413);
        assertThat(response.getContentAsString()).contains("message", "STUDY-012");
        assertThat(resolver.resolveException(new MockHttpServletRequest("POST", "/api/other"),
                new MockHttpServletResponse(), null, new MaxUploadSizeExceededException(1024))).isNull();
    }
}
