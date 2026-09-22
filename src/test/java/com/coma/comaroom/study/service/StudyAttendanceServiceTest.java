package com.coma.comaroom.study.service;

import com.coma.comaroom.member.entity.Member;
import com.coma.comaroom.study.StudyError;
import com.coma.comaroom.study.dto.request.CreateStudyAttendanceRequest;
import com.coma.comaroom.study.entity.*;
import com.coma.comaroom.study.repository.*;
import com.coma.comaroom.utils.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class StudyAttendanceServiceTest {
    private final StudyRepository studies = mock(StudyRepository.class);
    private final StudyWeekRepository weeks = mock(StudyWeekRepository.class);
    private final StudyAttendanceSessionRepository sessions = mock(StudyAttendanceSessionRepository.class);
    private final SecurityUtils security = mock(SecurityUtils.class);
    private final StudyAttendanceService service = new StudyAttendanceService(
            new StudyAccessService(studies, mock(StudyMemberRepository.class)), weeks, sessions, security);

    @BeforeEach
    void setup() {
        Member leader = Member.builder().memberId(1L).build();
        when(security.getCurrentMember()).thenReturn(leader);
        when(studies.findForUpdate(10L)).thenReturn(Optional.of(Study.builder().id(10L).studyManager(leader).build()));
    }

    @Test
    void requiresLeaderAndExistingPlan() {
        assertThatThrownBy(() -> service.create(10L, 1, new CreateStudyAttendanceRequest(5)))
                .hasMessage(StudyError.WEEK_NOT_FOUND.getMessage());
        when(security.getCurrentMember()).thenReturn(Member.builder().memberId(2L).build());
        assertThatThrownBy(() -> service.create(10L, 1, new CreateStudyAttendanceRequest(5)))
                .hasMessage(StudyError.ACCESS_DENIED.getMessage());
        verify(sessions, never()).save(any());
    }

    @Test
    void rejectsActiveSessionAndAllowsNewSessionAfterExpiry() {
        when(weeks.findByStudyIdAndWeekNumber(10L, 1)).thenReturn(Optional.of(StudyWeek.builder().id(20L).build()));
        when(sessions.existsByPlanIdAndExpiresAtAfter(eq(20L), any())).thenReturn(true);
        assertThatThrownBy(() -> service.create(10L, 1, new CreateStudyAttendanceRequest(5)))
                .hasMessage(StudyError.ATTENDANCE_CONFLICT.getMessage());
        when(sessions.existsByPlanIdAndExpiresAtAfter(eq(20L), any())).thenReturn(false);
        when(sessions.save(any())).thenAnswer(call -> call.getArgument(0));
        var result = service.create(10L, 1, new CreateStudyAttendanceRequest(5));
        assertThat(result.qrCodeId()).isNotBlank();
        assertThat(result.expiresAt()).isBetween(Instant.now().plusSeconds(295), Instant.now().plusSeconds(305));
    }
}
