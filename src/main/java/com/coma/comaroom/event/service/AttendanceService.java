package com.coma.comaroom.event.service;

import com.coma.comaroom.event.dto.CreateAttendanceCheckRequestDto;
import com.coma.comaroom.event.dto.CreateAttendanceCheckResponseDto;
import com.coma.comaroom.event.dto.CreateAttendanceRequestDto;
import com.coma.comaroom.event.entity.Event;
import com.coma.comaroom.event.entity.EventCategory;
import com.coma.comaroom.event.entity.EventParticipant;
import com.coma.comaroom.event.repository.EventParticipateRepository;
import com.coma.comaroom.event.repository.EventRepository;
import com.coma.comaroom.member.entity.Member;
import com.coma.comaroom.utils.SecurityUtils;
import lombok.AllArgsConstructor;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Base64;


import java.time.LocalDateTime;
import java.util.Optional;

@Service
@AllArgsConstructor
public class AttendanceService {
    private final SecurityUtils securityUtils;
    private final EventRepository eventRepository;
    private StringRedisTemplate redisTemplate;
    private final EventParticipateRepository eventParticipateRepository;


    public CreateAttendanceCheckResponseDto createAttendanceCheck(CreateAttendanceCheckRequestDto createAttendanceCheckRequestDto) {
        Member currentUser = securityUtils.getCurrentMember();

        LocalDateTime now = LocalDateTime.now();
        Event event = Event.builder()
                .title(createAttendanceCheckRequestDto.getEventTitle())
                .eventDate(now)
                .eventCategory(createAttendanceCheckRequestDto.getEventCategory())
                .host(currentUser)
                .location(createAttendanceCheckRequestDto.getLocation())
//                .rewardXp(createAttendanceCheckRequestDto.getEventCategory().getDefaultXp())
                .build();

        eventRepository.save(event);

        String qrCodeId = Base64.getEncoder().encodeToString(event.getTitle().getBytes()) + "-" + Base64.getEncoder().encodeToString(event.getEventCategory().toString().getBytes());

        CreateAttendanceCheckResponseDto createAttendanceCheckResponseDto = CreateAttendanceCheckResponseDto.builder()
                .qrCodeId(qrCodeId)
                .build();
        // base64로 인코딩한 값으로 (행사이름-열거형)

        String eventId = event.getEventId().toString();
        redisTemplate.opsForValue().set(qrCodeId, eventId, Duration.ofMinutes(createAttendanceCheckRequestDto.getExpirationTime()));

        return createAttendanceCheckResponseDto;
    }

    public void createAttendance(CreateAttendanceRequestDto createAttendanceRequestDto) {
        Long eventId = Long.valueOf(redisTemplate.opsForValue().get(createAttendanceRequestDto.getQrCodeId()));
        System.out.println(eventId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalStateException("이벤트 없음"));

        Member currentUser = securityUtils.getCurrentMember();

        EventParticipant participant = EventParticipant.builder()
                .event(event)
                .participantMember(currentUser)
                .build();

        eventParticipateRepository.save(participant);
    }
}
