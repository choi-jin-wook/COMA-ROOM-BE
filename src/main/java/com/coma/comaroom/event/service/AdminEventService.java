package com.coma.comaroom.event.service;

import com.coma.comaroom.BusinessException;
import com.coma.comaroom.event.EventError;
import com.coma.comaroom.event.dto.CreateAttendanceCheckRequestDto;
import com.coma.comaroom.event.dto.CreateAttendanceCheckResponseDto;
import com.coma.comaroom.event.entity.Event;
import com.coma.comaroom.event.mapper.AttendanceMapper;
import com.coma.comaroom.event.mapper.EventMapper;
import com.coma.comaroom.event.repository.EventParticipateRepository;
import com.coma.comaroom.event.repository.EventRepository;
import com.coma.comaroom.member.entity.Member;
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
}
