package com.coma.comaroom.event.service;

import com.coma.comaroom.BusinessException;
import com.coma.comaroom.event.EventError;
import com.coma.comaroom.event.EventPostError;
import com.coma.comaroom.event.dto.CreateAttendanceCheckRequestDto;
import com.coma.comaroom.event.dto.CreateAttendanceCheckResponseDto;
import com.coma.comaroom.event.dto.CreateAttendanceRequestDto;
import com.coma.comaroom.event.dto.request.EventRequest;
import com.coma.comaroom.event.dto.response.EventResponse;
import com.coma.comaroom.event.entity.Event;
import com.coma.comaroom.event.entity.EventCategory;
import com.coma.comaroom.event.entity.EventParticipant;
import com.coma.comaroom.event.mapper.AttendanceMapper;
import com.coma.comaroom.event.mapper.EventMapper;
import com.coma.comaroom.event.repository.EventParticipateRepository;
import com.coma.comaroom.event.repository.EventRepository;
import com.coma.comaroom.member.entity.Member;
import com.coma.comaroom.member.entity.Role;
import com.coma.comaroom.utils.SecurityUtils;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

@Service
@Transactional
@AllArgsConstructor
public class EventService {
    private final SecurityUtils securityUtils;
    private final EventRepository eventRepository;
    private StringRedisTemplate redisTemplate;
    private final EventParticipateRepository eventParticipateRepository;
    private final AttendanceMapper attendanceMapper;
    private final EventMapper eventMapper;


    // 출석 생성
    public CreateAttendanceCheckResponseDto createAttendanceCheck(CreateAttendanceCheckRequestDto createAttendanceCheckRequestDto) {
        Member currentUser = securityUtils.getCurrentMember();
        Event event = eventRepository.findById(createAttendanceCheckRequestDto.getEventId()).orElseThrow(() -> new BusinessException(EventError.EVENT_NOT_FOUND));

        // base64로 인코딩한 값으로 (행사이름-열거형)
        String qrCodeId = Base64.getEncoder().encodeToString(event.getTitle().getBytes()) + "-" + Base64.getEncoder().encodeToString(event.getEventCategory().toString().getBytes());
        CreateAttendanceCheckResponseDto createAttendanceCheckResponseDto = new CreateAttendanceCheckResponseDto(qrCodeId);

        // 레디스 값에 저장
        redisTemplate.opsForValue().set(qrCodeId, event.getEventId().toString(), Duration.ofMinutes(createAttendanceCheckRequestDto.getExpirationTime()));
        return createAttendanceCheckResponseDto;
    }


    // 출석하기
    public void createAttendance(CreateAttendanceRequestDto createAttendanceRequestDto) {
        String eventIdStr = redisTemplate.opsForValue().get(createAttendanceRequestDto.getQrCodeId());

        if (eventIdStr == null) {
            throw new BusinessException(EventError.INVALID_QR_CODE);
        }

        Long eventId = Long.valueOf(eventIdStr);

        Member currentUser = securityUtils.getCurrentMember();

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessException(EventError.EVENT_NOT_FOUND));

        event.addParticipant(currentUser);
    }

    // 1. 이달의 이벤트 조회
    public List<EventResponse> getMonthlyEvents(int year, int month) {
        LocalDateTime start = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime end = start.plusMonths(1).minusNanos(1);

        return eventRepository.findAllByEventDateBetween(start, end).stream()
                .map(EventResponse::from)
                .toList();
    }

    // 2. 이벤트 생성
    @Transactional
    public EventResponse createEvent(EventRequest request) {
        // 서비스 단계에서 현 사용자 정보를 가져옴
        Member currentMember = securityUtils.getCurrentMember();

        Event event = eventMapper.toEntity(request, currentMember);
        return EventResponse.from(eventRepository.save(event));
    }

    // 3. 이벤트 삭제
    @Transactional
    public void deleteEvent(Long eventId) {
        Member currentMember = securityUtils.getCurrentMember();
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessException(EventPostError.POST_NOT_FOUND));

        // 권한 확인: 관리자이거나 이벤트 호스트인 경우만 삭제 가능
        if (currentMember.getRole() != Role.ADMIN && !event.getHost().equals(currentMember)) {
            throw new BusinessException(EventPostError.UNAUTHORIZED_ACCESS);
        }

        eventRepository.delete(event);
    }

    // 4. 이벤트 수정
    @Transactional
    public EventResponse updateEvent(Long eventId, EventRequest request) {
        Member currentMember = securityUtils.getCurrentMember();
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessException(EventPostError.POST_NOT_FOUND));

        // 수정 권한 확인
        if (currentMember.getRole() != Role.ADMIN && !event.getHost().equals(currentMember)) {
            throw new BusinessException(EventPostError.UNAUTHORIZED_ACCESS);
        }

        // 엔티티 필드 업데이트 (도메인 메서드 사용 권장)
        // event.update(request.title(), request.eventDate(), ...);

        return EventResponse.from(event);
    }
}
