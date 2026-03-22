package com.coma.comaroom.event.service;

import com.coma.comaroom.BusinessException;
import com.coma.comaroom.event.EventError;
import com.coma.comaroom.event.EventPostError;
import com.coma.comaroom.event.dto.CreateAttendanceCheckRequestDto;
import com.coma.comaroom.event.dto.CreateAttendanceCheckResponseDto;
import com.coma.comaroom.event.dto.request.CreateEventRequest;
import com.coma.comaroom.event.dto.request.EventRequest;
import com.coma.comaroom.event.dto.response.EventResponse;
import com.coma.comaroom.event.entity.Event;
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
import java.util.Base64;

@Service
@Transactional
@AllArgsConstructor
public class AdminEventService {
    private final SecurityUtils securityUtils;
    private final EventRepository eventRepository;
    private StringRedisTemplate redisTemplate;
    private final EventMapper eventMapper;

    // 출석 생성 (테스트 완료)
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

    public EventResponse createEvent(CreateEventRequest request) {
        Member currentUser = securityUtils.getCurrentMember();

        // XP 결정 로직 (요청값이 없으면 카테고리 기본값 사용)
        if (request.getRewardXp() != null) {
            request.setRewardXp(request.getEventCategory().getDefaultXp());
        }

        Event event = eventMapper.toEntity(request, currentUser);

        Event savedEvent = eventRepository.save(event);

        return EventResponse.from(savedEvent);
    }
    // 3. 이벤트 삭제
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
        event.update(request);

        return EventResponse.from(event);
    }


}
