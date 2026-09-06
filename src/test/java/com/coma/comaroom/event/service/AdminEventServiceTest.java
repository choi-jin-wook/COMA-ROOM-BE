package com.coma.comaroom.event.service;

import com.coma.comaroom.BusinessException;
import com.coma.comaroom.event.EventError;
import com.coma.comaroom.event.EventPostError;
import com.coma.comaroom.event.dto.CreateAttendanceCheckRequestDto;
import com.coma.comaroom.event.dto.CreateAttendanceCheckResponseDto;
import com.coma.comaroom.event.dto.request.CreateEventRequest;
import com.coma.comaroom.event.dto.request.EventRequest;
import com.coma.comaroom.event.dto.response.AttendanceItemResponseDto;
import com.coma.comaroom.event.dto.response.EventResponse;
import com.coma.comaroom.event.entity.Event;
import com.coma.comaroom.event.entity.EventCategory;
import com.coma.comaroom.event.entity.EventParticipant;
import com.coma.comaroom.event.repository.EventParticipateRepository;
import com.coma.comaroom.event.repository.EventRepository;
import com.coma.comaroom.member.AuthError;
import com.coma.comaroom.member.entity.Major;
import com.coma.comaroom.member.entity.Member;
import com.coma.comaroom.member.entity.Role;
import com.coma.comaroom.member.repository.MemberRepository;
import com.coma.comaroom.utils.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminEventServiceTest {

    @Mock private SecurityUtils securityUtils;
    @Mock private EventRepository eventRepository;
    @Mock private StringRedisTemplate redisTemplate;
    @Mock private EventParticipateRepository eventParticipateRepository;
    @Mock private MemberRepository memberRepository;
    @Mock private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private AdminEventService adminEventService;

    private Member adminMember;
    private Member userMember;
    private Event event;

    @BeforeEach
    void setUp() {
        adminMember = Member.builder()
                .memberId(1L)
                .studentId("20210001")
                .name("관리자")
                .password("encoded")
                .xp(0L)
                .role(Role.ADMIN)
                .major(Major.COMPUTER_INFO)
                .build();

        userMember = Member.builder()
                .memberId(2L)
                .studentId("20210002")
                .name("일반유저")
                .password("encoded")
                .xp(0L)
                .role(Role.USER)
                .major(Major.COMPUTER_INFO)
                .build();

        event = Event.builder()
                .eventId(1L)
                .title("정기 모임")
                .eventDate(LocalDateTime.now().plusDays(1))
                .rewardXp(3L)
                .location("강의실 A")
                .eventCategory(EventCategory.REGULAR_MEETING)
                .host(userMember)
                .eventParticipants(new ArrayList<>())
                .eventPosts(new ArrayList<>())
                .build();
    }

    // ─────────────────────────────────────────────
    // createAttendanceCheck
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("출석 QR 생성 성공")
    void createAttendanceCheck_success() {
        CreateAttendanceCheckRequestDto dto = mock(CreateAttendanceCheckRequestDto.class);
        when(dto.getEventId()).thenReturn(1L);
        when(dto.getExpirationTime()).thenReturn(10);
        when(securityUtils.getCurrentMember()).thenReturn(adminMember);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        CreateAttendanceCheckResponseDto result = adminEventService.createAttendanceCheck(dto);

        assertThat(result).isNotNull();
        assertThat(result.getQrCodeId()).isNotBlank();
        verify(valueOperations).set(anyString(), eq("1"), any(Duration.class));
    }

    @Test
    @DisplayName("출석 QR 생성 실패 - 이벤트 없음")
    void createAttendanceCheck_eventNotFound() {
        CreateAttendanceCheckRequestDto dto = mock(CreateAttendanceCheckRequestDto.class);
        when(dto.getEventId()).thenReturn(99L);
        when(securityUtils.getCurrentMember()).thenReturn(adminMember);
        when(eventRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminEventService.createAttendanceCheck(dto))
                .isInstanceOf(BusinessException.class);
    }

    // ─────────────────────────────────────────────
    // createEvent
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("이벤트 생성 성공 - 요청한 rewardXp 값이 그대로 유지된다")
    void createEvent_success() {
        CreateEventRequest request = new CreateEventRequest(
                "정기 모임", LocalDateTime.now().plusDays(1), "강의실 A", EventCategory.EVENT, 7L);
        when(securityUtils.getCurrentMember()).thenReturn(adminMember);
        when(eventRepository.save(any(Event.class))).thenReturn(event);

        EventResponse result = adminEventService.createEvent(request);

        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo("정기 모임");
        assertThat(request.getRewardXp()).isEqualTo(7L);

        ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository).save(captor.capture());

        Event saved = captor.getValue();
        assertThat(saved.getTitle()).isEqualTo("정기 모임");
        assertThat(saved.getLocation()).isEqualTo("강의실 A");
        assertThat(saved.getEventCategory()).isEqualTo(EventCategory.EVENT);
        assertThat(saved.getHost()).isSameAs(adminMember);
        assertThat(saved.getRewardXp()).isEqualTo(7L);
    }

    @Test
    @DisplayName("이벤트 생성 - rewardXp가 없으면 카테고리 기본값이 적용된다")
    void createEvent_defaultRewardXp() {
        CreateEventRequest request = new CreateEventRequest(
                "정기 모임", LocalDateTime.now().plusDays(1), "강의실 A", EventCategory.EVENT, null);
        when(securityUtils.getCurrentMember()).thenReturn(adminMember);
        when(eventRepository.save(any(Event.class))).thenReturn(event);

        adminEventService.createEvent(request);

        assertThat(request.getRewardXp()).isEqualTo(EventCategory.EVENT.getDefaultXp());
    }

    // ─────────────────────────────────────────────
    // deleteEvent
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("이벤트 삭제 성공 - 관리자 권한")
    void deleteEvent_successAsAdmin() {
        when(securityUtils.getCurrentMember()).thenReturn(adminMember);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        assertThatNoException().isThrownBy(() -> adminEventService.deleteEvent(1L));
        verify(eventRepository).delete(event);
    }

    @Test
    @DisplayName("이벤트 삭제 성공 - 호스트 권한")
    void deleteEvent_successAsHost() {
        when(securityUtils.getCurrentMember()).thenReturn(userMember);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        assertThatNoException().isThrownBy(() -> adminEventService.deleteEvent(1L));
        verify(eventRepository).delete(event);
    }

    @Test
    @DisplayName("이벤트 삭제 실패 - 권한 없는 일반 유저")
    void deleteEvent_unauthorized() {
        Member anotherUser = Member.builder()
                .memberId(3L)
                .studentId("20210003")
                .name("다른유저")
                .password("encoded")
                .xp(0L)
                .role(Role.USER)
                .major(Major.COMPUTER_INFO)
                .build();

        when(securityUtils.getCurrentMember()).thenReturn(anotherUser);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> adminEventService.deleteEvent(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessage(EventPostError.UNAUTHORIZED_ACCESS.getMessage());
    }

    @Test
    @DisplayName("이벤트 삭제 실패 - 이벤트 없음")
    void deleteEvent_eventNotFound() {
        when(securityUtils.getCurrentMember()).thenReturn(adminMember);
        when(eventRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminEventService.deleteEvent(99L))
                .isInstanceOf(BusinessException.class);
    }

    // ─────────────────────────────────────────────
    // updateEvent
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("이벤트 수정 성공 - 관리자 권한")
    void updateEvent_successAsAdmin() {
        EventRequest request = new EventRequest("수정된 모임", null, null, null, null);
        when(securityUtils.getCurrentMember()).thenReturn(adminMember);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        EventResponse result = adminEventService.updateEvent(1L, request);

        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo("수정된 모임");
    }

    @Test
    @DisplayName("이벤트 수정 실패 - 권한 없는 일반 유저")
    void updateEvent_unauthorized() {
        Member anotherUser = Member.builder()
                .memberId(3L)
                .studentId("20210003")
                .name("다른유저")
                .password("encoded")
                .xp(0L)
                .role(Role.USER)
                .major(Major.COMPUTER_INFO)
                .build();

        EventRequest request = new EventRequest("수정된 모임", null, null, null, null);
        when(securityUtils.getCurrentMember()).thenReturn(anotherUser);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> adminEventService.updateEvent(1L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage(EventPostError.UNAUTHORIZED_ACCESS.getMessage());
    }

    @Test
    @DisplayName("이벤트 수정 실패 - 이벤트 없음")
    void updateEvent_eventNotFound() {
        EventRequest request = new EventRequest("수정된 모임", null, null, null, null);
        when(securityUtils.getCurrentMember()).thenReturn(adminMember);
        when(eventRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminEventService.updateEvent(99L, request))
                .isInstanceOf(BusinessException.class);
    }

    // ─────────────────────────────────────────────
    // getAttendanceList
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("출석 명단 조회 성공")
    void getAttendanceList_success() {
        EventParticipant participant = EventParticipant.builder()
                .eventParticipantId(1L)
                .event(event)
                .participantMember(userMember)
                .build();

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(eventParticipateRepository.findByEvent(event)).thenReturn(List.of(participant));

        List<AttendanceItemResponseDto> result = adminEventService.getAttendanceList(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMemberId()).isEqualTo(userMember.getMemberId());
        assertThat(result.get(0).getName()).isEqualTo(userMember.getName());
    }

    @Test
    @DisplayName("출석 명단 조회 성공 - 출석자 없음")
    void getAttendanceList_empty() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(eventParticipateRepository.findByEvent(event)).thenReturn(List.of());

        List<AttendanceItemResponseDto> result = adminEventService.getAttendanceList(1L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("출석 명단 조회 실패 - 이벤트 없음")
    void getAttendanceList_eventNotFound() {
        when(eventRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminEventService.getAttendanceList(99L))
                .isInstanceOf(BusinessException.class)
                .hasMessage(EventError.EVENT_NOT_FOUND.getMessage());
    }

    // ─────────────────────────────────────────────
    // addAttendance
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("출석 추가 성공")
    void addAttendance_success() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(memberRepository.findById(2L)).thenReturn(Optional.of(userMember));
        when(eventParticipateRepository.existsByParticipantMemberAndEvent(userMember, event)).thenReturn(false);

        assertThatNoException().isThrownBy(() -> adminEventService.addAttendance(1L, 2L));

        assertThat(userMember.getXp()).isEqualTo(3L); // 기존 0 + rewardXp 3
        assertThat(event.getEventParticipants()).hasSize(1);
    }

    @Test
    @DisplayName("출석 추가 실패 - 이벤트 없음")
    void addAttendance_eventNotFound() {
        when(eventRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminEventService.addAttendance(99L, 2L))
                .isInstanceOf(BusinessException.class)
                .hasMessage(EventError.EVENT_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("출석 추가 실패 - 멤버 없음")
    void addAttendance_memberNotFound() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(memberRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminEventService.addAttendance(1L, 99L))
                .isInstanceOf(BusinessException.class)
                .hasMessage(AuthError.MEMBER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("출석 추가 실패 - 이미 출석 처리된 멤버")
    void addAttendance_alreadyAttended() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(memberRepository.findById(2L)).thenReturn(Optional.of(userMember));
        when(eventParticipateRepository.existsByParticipantMemberAndEvent(userMember, event)).thenReturn(true);

        assertThatThrownBy(() -> adminEventService.addAttendance(1L, 2L))
                .isInstanceOf(BusinessException.class)
                .hasMessage(EventError.ALREADY_ATTENDED.getMessage());
    }

    // ─────────────────────────────────────────────
    // removeAttendance
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("출석 삭제 성공")
    void removeAttendance_success() {
        userMember.setXp(10L);
        EventParticipant participant = EventParticipant.builder()
                .eventParticipantId(1L)
                .event(event)
                .participantMember(userMember)
                .build();

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(memberRepository.findById(2L)).thenReturn(Optional.of(userMember));
        when(eventParticipateRepository.findByEventAndParticipantMember(event, userMember))
                .thenReturn(Optional.of(participant));

        assertThatNoException().isThrownBy(() -> adminEventService.removeAttendance(1L, 2L));

        assertThat(userMember.getXp()).isEqualTo(7L); // 10 - rewardXp 3
        verify(eventParticipateRepository).delete(participant);
    }

    @Test
    @DisplayName("출석 삭제 성공 - XP가 0 미만으로 내려가지 않음")
    void removeAttendance_xpNotBelowZero() {
        userMember.setXp(1L);
        EventParticipant participant = EventParticipant.builder()
                .eventParticipantId(1L)
                .event(event)
                .participantMember(userMember)
                .build();

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(memberRepository.findById(2L)).thenReturn(Optional.of(userMember));
        when(eventParticipateRepository.findByEventAndParticipantMember(event, userMember))
                .thenReturn(Optional.of(participant));

        assertThatNoException().isThrownBy(() -> adminEventService.removeAttendance(1L, 2L));

        assertThat(userMember.getXp()).isEqualTo(0L); // Math.max(0, 1-3)
    }

    @Test
    @DisplayName("출석 삭제 실패 - 이벤트 없음")
    void removeAttendance_eventNotFound() {
        when(eventRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminEventService.removeAttendance(99L, 2L))
                .isInstanceOf(BusinessException.class)
                .hasMessage(EventError.EVENT_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("출석 삭제 실패 - 멤버 없음")
    void removeAttendance_memberNotFound() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(memberRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminEventService.removeAttendance(1L, 99L))
                .isInstanceOf(BusinessException.class)
                .hasMessage(AuthError.MEMBER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("출석 삭제 실패 - 출석 기록 없음")
    void removeAttendance_attendanceNotFound() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(memberRepository.findById(2L)).thenReturn(Optional.of(userMember));
        when(eventParticipateRepository.findByEventAndParticipantMember(event, userMember))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminEventService.removeAttendance(1L, 2L))
                .isInstanceOf(BusinessException.class)
                .hasMessage(EventError.ATTENDANCE_NOT_FOUND.getMessage());
    }

    // ─────────────────────────────────────────────
    // [보안] QR 코드 예측 불가능성 (Vuln 6)
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("[보안] 동일 이벤트로 QR 생성 시 매번 다른 ID가 발급된다")
    void createAttendanceCheck_qrCodeIsUnique() {
        CreateAttendanceCheckRequestDto dto = mock(CreateAttendanceCheckRequestDto.class);
        when(dto.getEventId()).thenReturn(1L);
        when(dto.getExpirationTime()).thenReturn(10);
        when(securityUtils.getCurrentMember()).thenReturn(adminMember);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        String qr1 = adminEventService.createAttendanceCheck(dto).getQrCodeId();
        String qr2 = adminEventService.createAttendanceCheck(dto).getQrCodeId();

        assertThat(qr1).isNotEqualTo(qr2);
    }

    @Test
    @DisplayName("[보안] QR 코드 ID는 이벤트 제목·카테고리로 역산할 수 없다 (UUID 형식)")
    void createAttendanceCheck_qrCodeIsUuid() {
        CreateAttendanceCheckRequestDto dto = mock(CreateAttendanceCheckRequestDto.class);
        when(dto.getEventId()).thenReturn(1L);
        when(dto.getExpirationTime()).thenReturn(10);
        when(securityUtils.getCurrentMember()).thenReturn(adminMember);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        String qrCodeId = adminEventService.createAttendanceCheck(dto).getQrCodeId();

        // UUID 형식 검증 (8-4-4-4-12)
        assertThat(qrCodeId).matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
        // 이벤트 제목이나 카테고리 문자열이 포함되지 않음
        assertThat(qrCodeId).doesNotContain(event.getTitle());
        assertThat(qrCodeId).doesNotContain(event.getEventCategory().toString());
    }
}
