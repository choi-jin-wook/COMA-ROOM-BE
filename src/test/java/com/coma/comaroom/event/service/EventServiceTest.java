package com.coma.comaroom.event.service;

import com.coma.comaroom.BusinessException;
import com.coma.comaroom.event.EventError;
import com.coma.comaroom.event.dto.CreateAttendanceRequestDto;
import com.coma.comaroom.event.dto.response.EventResponse;
import com.coma.comaroom.event.entity.Event;
import com.coma.comaroom.event.entity.EventCategory;
import com.coma.comaroom.event.entity.EventParticipant;
import com.coma.comaroom.event.entity.EventPost;
import com.coma.comaroom.event.mapper.EventMapper;
import com.coma.comaroom.event.repository.EventParticipateRepository;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock private SecurityUtils securityUtils;
    @Mock private EventRepository eventRepository;
    @Mock private StringRedisTemplate redisTemplate;
    @Mock private EventParticipateRepository eventParticipateRepository;
    @Mock private EventMapper eventMapper;
    @Mock private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private EventService eventService;

    private Member member;
    private Event event;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .memberId(1L)
                .studentId("20210001")
                .name("테스터")
                .password("encoded_password")
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
                .host(member)
                .eventParticipants(new ArrayList<>())
                .eventPosts(new ArrayList<>())
                .build();
    }

    // ─────────────────────────────────────────────
    // createAttendance
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("출석 성공 - 유효한 QR 코드와 존재하는 이벤트")
    void createAttendance_success() {
        CreateAttendanceRequestDto dto = mock(CreateAttendanceRequestDto.class);
        when(dto.getQrCodeId()).thenReturn("validQrCode");
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("validQrCode")).thenReturn("1");
        when(securityUtils.getCurrentMember()).thenReturn(member);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        assertThatNoException().isThrownBy(() -> eventService.createAttendance(dto));
        verify(eventRepository).findById(1L);
    }

    @Test
    @DisplayName("출석 실패 - QR 코드가 만료되거나 유효하지 않음")
    void createAttendance_invalidQrCode() {
        CreateAttendanceRequestDto dto = mock(CreateAttendanceRequestDto.class);
        when(dto.getQrCodeId()).thenReturn("expiredQrCode");
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("expiredQrCode")).thenReturn(null);

        assertThatThrownBy(() -> eventService.createAttendance(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessage(EventError.INVALID_QR_CODE.getMessage());
    }

    @Test
    @DisplayName("출석 실패 - 이벤트가 존재하지 않음")
    void createAttendance_eventNotFound() {
        CreateAttendanceRequestDto dto = mock(CreateAttendanceRequestDto.class);
        when(dto.getQrCodeId()).thenReturn("validQrCode");
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("validQrCode")).thenReturn("99");
        when(securityUtils.getCurrentMember()).thenReturn(member);
        when(eventRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.createAttendance(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessage(EventError.EVENT_NOT_FOUND.getMessage());
    }

    // ─────────────────────────────────────────────
    // getMonthlyEvents
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("이달의 이벤트 조회 성공")
    void getMonthlyEvents_success() {
        when(eventRepository.findAllByEventDateBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(event));

        List<EventResponse> result = eventService.getMonthlyEvents(2026, 3);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).title()).isEqualTo("정기 모임");
        assertThat(result.get(0).hostname()).isEqualTo("테스터");
    }

    @Test
    @DisplayName("이달의 이벤트 조회 - 이벤트 없음")
    void getMonthlyEvents_empty() {
        when(eventRepository.findAllByEventDateBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of());

        List<EventResponse> result = eventService.getMonthlyEvents(2026, 3);

        assertThat(result).isEmpty();
    }
}
