package com.coma.comaroom.member.controller;

import com.coma.comaroom.member.dto.request.RegisterMemberRequestDto;
import com.coma.comaroom.member.service.AuthService;
import com.coma.comaroom.utils.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AuthControllerApiTest {

    private MockMvc mockMvc;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = mock(AuthService.class);
        AuthController controller = new AuthController(authService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void registerRejectsNonArrayBodyWithBadRequest() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "studentId": "20210001",
                                  "name": "member1",
                                  "password": "password1",
                                  "major": "COMPUTER_INFO",
                                  "role": "USER"
                                }
                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("AUTH-006"));
    }

    @Test
    void registerAcceptsMultipleMembers() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                [
                                  {
                                    "studentId": "20210001",
                                    "name": "member1",
                                    "password": "password1",
                                    "major": "COMPUTER_INFO",
                                    "role": "USER"
                                  },
                                  {
                                    "studentId": "20210002",
                                    "name": "member2",
                                    "password": "password2",
                                    "major": "DATA_SCIENCE",
                                    "role": "USER"
                                  }
                                ]
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].studentId").value("20210001"))
                .andExpect(jsonPath("$.data[1].studentId").value("20210002"));

        verify(authService).registerMembers(anyList());
    }

    @Test
    void registerAcceptsOneHundredMembers() throws Exception {
        StringBuilder requestBody = new StringBuilder("[");
        for (int i = 1; i <= 100; i++) {
            if (i > 1) {
                requestBody.append(',');
            }
            requestBody.append("""
                    {
                      "studentId": "%08d",
                      "name": "member%d",
                      "password": "password%d",
                      "major": "COMPUTER_INFO",
                      "role": "USER"
                    }
                    """.formatted(i, i, i));
        }
        requestBody.append(']');

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody.toString()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.length()").value(100))
                .andExpect(jsonPath("$.data[0].studentId").value("00000001"))
                .andExpect(jsonPath("$.data[99].studentId").value("00000100"));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<RegisterMemberRequestDto>> captor = ArgumentCaptor.forClass(List.class);
        verify(authService).registerMembers(captor.capture());
        assertThat(captor.getValue()).hasSize(100);
    }

}
