package com.coma.comaroom.event.service;

import com.coma.comaroom.event.dto.response.AttendanceItemResponseDto;
import com.coma.comaroom.event.dto.response.EventAttendanceResponse;
import com.coma.comaroom.event.entity.Event;
import com.coma.comaroom.event.entity.EventParticipant;
import com.coma.comaroom.event.repository.EventParticipateRepository;
import com.coma.comaroom.event.repository.EventRepository;
import com.coma.comaroom.member.entity.Major;
import com.coma.comaroom.member.entity.Member;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminEventAttendanceUnitTest {

    @Mock private EventRepository events;
    @Mock private EventParticipateRepository participants;
    @InjectMocks private AdminEventService service;

    @ParameterizedTest(name = "이벤트 {0}개, 첫 이벤트 출석 {1}건")
    @CsvSource({"0, 0", "1, 0", "1, 1", "1, 2", "2, 0", "2, 1", "2, 2"})
    void returnsEveryEventAtTheZeroOneAndManyBoundaries(int eventCount, int attendanceCount) {
        List<Event> savedEvents = IntStream.range(0, eventCount)
                .mapToObj(index -> event(100L + index))
                .toList();
        List<EventParticipant> savedParticipants = IntStream.range(0, attendanceCount)
                .mapToObj(index -> participant(savedEvents.getFirst(), member(200L + index)))
                .toList();
        when(events.findAllByOrderByEventDateDesc()).thenReturn(savedEvents);
        when(participants.findAll()).thenReturn(savedParticipants);

        List<EventAttendanceResponse> result = service.getAllAttendanceLists();

        assertThat(result).extracting(EventAttendanceResponse::eventId)
                .containsExactlyElementsOf(savedEvents.stream().map(Event::getEventId).toList());
        assertThat(result).flatExtracting(EventAttendanceResponse::attendances)
                .extracting(AttendanceItemResponseDto::getMemberId)
                .containsExactlyElementsOf(savedParticipants.stream()
                        .map(row -> row.getParticipantMember().getMemberId()).toList());
        IntStream.range(0, result.size()).forEach(index ->
                assertThat(result.get(index).attendances()).hasSize(index == 0 ? attendanceCount : 0));
        verify(events).findAllByOrderByEventDateDesc();
        verify(participants).findAll();
        verifyNoMoreInteractions(events, participants);
    }

    @Test
    void groupsByEventIdAndKeepsWithdrawnMembersAndRepeatAttendees() {
        Event newer = event(102L);
        Event older = event(101L);
        Member active = member(200L);
        when(events.findAllByOrderByEventDateDesc()).thenReturn(List.of(newer, older));
        when(participants.findAll()).thenReturn(List.of(
                participant(older, active), participant(newer, null), participant(newer, active)));

        List<EventAttendanceResponse> result = service.getAllAttendanceLists();

        assertThat(result).extracting(EventAttendanceResponse::eventId).containsExactly(102L, 101L);
        assertThat(result).extracting(EventAttendanceResponse::title).containsExactly("정기 모임", "정기 모임");
        assertThat(result.getFirst().attendances()).satisfiesExactlyInAnyOrder(row -> {
            assertThat(row.getMemberId()).isNull();
            assertThat(row.getName()).isEqualTo("탈퇴한 회원");
            assertThat(row.getStudentId()).isEqualTo("-");
            assertThat(row.getMajor()).isNull();
        }, row -> {
            assertThat(row.getMemberId()).isEqualTo(200L);
            assertThat(row.getName()).isEqualTo("출석 회원");
            assertThat(row.getStudentId()).isEqualTo("student-200");
            assertThat(row.getMajor()).isEqualTo(Major.COMPUTER_INFO.getMajor());
        });
        assertThat(result.getLast().attendances()).extracting(AttendanceItemResponseDto::getMemberId)
                .containsExactly(200L);
    }

    private Event event(long id) {
        return Event.builder().eventId(id).title("정기 모임").build();
    }

    private Member member(long id) {
        return Member.builder().memberId(id).name("출석 회원").studentId("student-" + id)
                .major(Major.COMPUTER_INFO).build();
    }

    private EventParticipant participant(Event event, Member member) {
        return EventParticipant.builder().event(event).participantMember(member).build();
    }
}
