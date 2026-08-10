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
        currentUser.setXp(currentUser.getXp() + 3);
    }

    // 1. 이달의 이벤트 조회
    public List<EventResponse> getMonthlyEvents(int year, int month) {
        LocalDateTime start = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime end = start.plusMonths(1).minusNanos(1);

        return eventRepository.findAllByEventDateBetween(start, end).stream()
                .map(EventResponse::from)
                .toList();
    }


}
