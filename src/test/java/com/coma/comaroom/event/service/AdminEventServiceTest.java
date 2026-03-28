package com.coma.comaroom.event.service;

import com.coma.comaroom.BusinessException;
import com.coma.comaroom.event.EventPostError;
import com.coma.comaroom.event.dto.CreateAttendanceCheckRequestDto;
import com.coma.comaroom.event.dto.CreateAttendanceCheckResponseDto;
import com.coma.comaroom.event.dto.request.CreateEventRequest;
import com.coma.comaroom.event.dto.request.EventRequest;
import com.coma.comaroom.event.dto.response.EventResponse;
import com.coma.comaroom.event.entity.Event;
import com.coma.comaroom.event.entity.EventCategory;
import com.coma.comaroom.event.mapper.EventMapper;
import com.coma.comaroom.event.repository.EventRepository;
import com.coma.comaroom.member.entity.Major;
import com.coma.comaroom.member.entity.Member;
import com.coma.comaroom.member.entity.Role;
import com.coma.comaroom.utils.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminEventServiceTest {

    @Mock private SecurityUtils securityUtils;
    @Mock private EventRepository eventRepository;
    @Mock private StringRedisTemplate redisTemplate;
    @Mock private EventMapper eventMapper;
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
    @DisplayName("이벤트 생성 성공")
    void createEvent_success() {
        CreateEventRequest request = mock(CreateEventRequest.class);
        when(request.getRewardXp()).thenReturn(5L);
        when(request.getEventCategory()).thenReturn(EventCategory.EVENT);
        when(securityUtils.getCurrentMember()).thenReturn(adminMember);
        when(eventMapper.toEntity(request, adminMember)).thenReturn(event);
        when(eventRepository.save(event)).thenReturn(event);

        EventResponse result = adminEventService.createEvent(request);

        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo("정기 모임");
        verify(eventRepository).save(event);
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
}
