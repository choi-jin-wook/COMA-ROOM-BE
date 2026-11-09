package com.coma.comaroom.study.service;

import com.coma.comaroom.member.entity.Member;
import com.coma.comaroom.event.dto.CreateAttendanceCheckResponseDto;
import com.coma.comaroom.event.entity.Event;
import com.coma.comaroom.event.entity.EventCategory;
import com.coma.comaroom.event.repository.EventRepository;
import com.coma.comaroom.event.service.AdminEventService;
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

/** 출석 세션 생성 권한과 기존 이벤트 QR 발급 연결을 확인한다. */
class StudyAttendanceServiceTest {
    private final StudyRepository studies = mock(StudyRepository.class);
    private final StudyWeekRepository weeks = mock(StudyWeekRepository.class);
    private final StudyAttendanceSessionRepository sessions = mock(StudyAttendanceSessionRepository.class);
    private final EventRepository events = mock(EventRepository.class);
    private final AdminEventService eventAttendance = mock(AdminEventService.class);
    private final SecurityUtils security = mock(SecurityUtils.class);
    private final StudyAttendanceService service = new StudyAttendanceService(
            new StudyAccessService(studies, mock(StudyMemberRepository.class)), weeks, sessions,
            events, eventAttendance, security);

    @BeforeEach
    void setup() {
        Member leader = Member.builder().memberId(1L).build();
        when(security.getCurrentMember()).thenReturn(leader);
        when(studies.findForUpdate(10L)).thenReturn(Optional.of(Study.builder().id(10L).studyManager(leader).build()));
    }

    @Test
    void requiresLeaderAndExistingPlan() {
        assertThatThrownBy(() -> service.create(10L, 1, new CreateStudyAttendanceRequest(30L, 5)))
                .hasMessage(StudyError.WEEK_NOT_FOUND.getMessage());
        when(security.getCurrentMember()).thenReturn(Member.builder().memberId(2L).build());
        assertThatThrownBy(() -> service.create(10L, 1, new CreateStudyAttendanceRequest(30L, 5)))
                .hasMessage(StudyError.ACCESS_DENIED.getMessage());
        verify(sessions, never()).save(any());
    }

    @Test
    void delegatesQrCreationToExistingEventAttendance() {
        when(weeks.findByStudyIdAndWeekNumber(10L, 1)).thenReturn(Optional.of(StudyWeek.builder().id(20L).build()));
        Event event = Event.builder().eventId(30L).eventCategory(EventCategory.STUDY)
                .host(Member.builder().memberId(1L).build()).build();
        when(events.findById(30L)).thenReturn(Optional.of(event));
        when(eventAttendance.createAttendanceCheck(any())).thenReturn(new CreateAttendanceCheckResponseDto("qr-token"));
        when(sessions.save(any())).thenAnswer(call -> call.getArgument(0));
        var result = service.create(10L, 1, new CreateStudyAttendanceRequest(30L, 5));
        assertThat(result.qrCodeId()).isEqualTo("qr-token");
        assertThat(result.expiresAt()).isBetween(Instant.now().plusSeconds(295), Instant.now().plusSeconds(305));
        verify(eventAttendance).createAttendanceCheck(argThat(request ->
                request.getEventId().equals(30L) && request.getExpirationTime().equals(5)));
        verify(sessions).save(argThat(session -> session.getEvent() == event && session.getPlan().getId().equals(20L)));
    }

    @Test
    void rejectsUnrelatedEventBeforeIssuingQr() {
        when(weeks.findByStudyIdAndWeekNumber(10L, 1)).thenReturn(Optional.of(StudyWeek.builder().id(20L).build()));
        when(events.findById(30L)).thenReturn(Optional.of(Event.builder().eventId(30L)
                .eventCategory(EventCategory.EVENT).host(Member.builder().memberId(1L).build()).build()));
        assertThatThrownBy(() -> service.create(10L, 1, new CreateStudyAttendanceRequest(30L, 5)))
                .hasMessage(StudyError.INVALID_INPUT.getMessage());
        verifyNoInteractions(eventAttendance, sessions);
    }

    @Test
    void rejectsStudyEventHostedByAnotherMember() {
        when(weeks.findByStudyIdAndWeekNumber(10L, 1)).thenReturn(Optional.of(StudyWeek.builder().id(20L).build()));
        when(events.findById(30L)).thenReturn(Optional.of(Event.builder().eventId(30L)
                .eventCategory(EventCategory.STUDY).host(Member.builder().memberId(2L).build()).build()));
        assertThatThrownBy(() -> service.create(10L, 1, new CreateStudyAttendanceRequest(30L, 5)))
                .hasMessage(StudyError.INVALID_INPUT.getMessage());
        verifyNoInteractions(eventAttendance, sessions);
    }
}
