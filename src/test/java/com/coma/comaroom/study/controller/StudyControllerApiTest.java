package com.coma.comaroom.study.controller;

import com.coma.comaroom.study.dto.response.*;
import com.coma.comaroom.study.entity.StudyStatus;
import com.coma.comaroom.study.service.*;
import com.coma.comaroom.utils.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class StudyControllerApiTest {
    private final StudyService studyService = mock(StudyService.class);
    private final StudyWeekService weekService = mock(StudyWeekService.class);
    private final StudyAttendanceService attendanceService = mock(StudyAttendanceService.class);
    private final StudyManagerService managerService = mock(StudyManagerService.class);
    private MockMvc mvc;

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders.standaloneSetup(
                new StudyController(mock(StudyJoinService.class), weekService, attendanceService),
                new MemberStudyController(studyService),
                new StudyManagerController(managerService),
                new StudyManagerCandidateController(studyService))
                .setControllerAdvice(new StudyExceptionHandler(), new GlobalExceptionHandler()).build();
    }

    @Test
    void creationReturnsCreatedAndActualManagerId() throws Exception {
        when(managerService.createStudy(any())).thenReturn(new StudyResponse(5L, "Study", 9L, "Leader"));
        mvc.perform(post("/api/admin/study").contentType(MediaType.APPLICATION_JSON)
                .content("{\"studyName\":\"Study\",\"managerId\":9}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.data.managerId").value(9))
                .andExpect(jsonPath("$.data.studyId").value(5));
    }

    @Test
    void invalidCreationIsBadRequest() throws Exception {
        mvc.perform(post("/api/admin/study").contentType(MediaType.APPLICATION_JSON)
                .content("{\"studyName\":\"  \",\"managerId\":9}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").isString());
        mvc.perform(post("/api/admin/study").contentType(MediaType.APPLICATION_JSON)
                .content("{\"studyName\":\"Study\"}")).andExpect(status().isBadRequest());
        verifyNoInteractions(managerService);
    }

    @Test
    void invalidStatusMissingStatusAndNonIntegerPageAreBadRequest() throws Exception {
        mvc.perform(get("/api/member/studies").param("status", "OTHER"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").isString());
        mvc.perform(get("/api/member/studies")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/member/studies").param("status", "ACTIVE").param("page", "abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void emptyPageUsesItemsAndPaginationContract() throws Exception {
        when(studyService.mine(StudyStatus.ACTIVE, 0, 20))
                .thenReturn(new StudyPage<>(List.of(), 0, 20, 0, 0));
        mvc.perform(get("/api/member/studies").param("status", "ACTIVE"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.items").isEmpty())
                .andExpect(jsonPath("$.data.page").value(0)).andExpect(jsonPath("$.data.size").value(20))
                .andExpect(jsonPath("$.data.totalElements").value(0));
    }

    @Test
    void multipartJsonPartWorksWithoutFile() throws Exception {
        when(weekService.create(eq(10L), any(), isNull())).thenReturn(
                new StudyWeekResponse(10L, "Study", "LEADER", 3L, 1, "Plan", null, null, List.of()));
        mvc.perform(multipart("/api/studies/10/weeks").file(new MockMultipartFile(
                "request", "", "application/json", "{\"weekNumber\":1,\"title\":\"Plan\"}".getBytes())))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.data.planId").value(3))
                .andExpect(jsonPath("$.data.materials").isEmpty());
    }

    @Test
    void invalidWeekBlankTitleAndMissingPartAreBadRequest() throws Exception {
        mvc.perform(multipart("/api/studies/10/weeks").file(new MockMultipartFile(
                "request", "", "application/json", "{\"weekNumber\":17,\"title\":\"Plan\"}".getBytes())))
                .andExpect(status().isBadRequest());
        mvc.perform(multipart("/api/studies/10/weeks").file(new MockMultipartFile(
                "request", "", "application/json", "{\"weekNumber\":1,\"title\":\"  \"}".getBytes())))
                .andExpect(status().isBadRequest());
        mvc.perform(multipart("/api/studies/10/weeks")).andExpect(status().isBadRequest());
        verifyNoInteractions(weekService);
    }

    @Test
    void expirationMustBePositiveInteger() throws Exception {
        mvc.perform(post("/api/studies/10/weeks/1/attendances").contentType(MediaType.APPLICATION_JSON)
                .content("{\"expirationTime\":0}")).andExpect(status().isBadRequest());
        mvc.perform(post("/api/studies/10/weeks/1/attendances").contentType(MediaType.APPLICATION_JSON)
                .content("{}")).andExpect(status().isBadRequest());
        verifyNoInteractions(attendanceService);
    }
}
