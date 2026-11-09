package com.coma.comaroom.study.service;

import com.coma.comaroom.BusinessException;
import com.coma.comaroom.event.dto.CreateAttendanceCheckRequestDto;
import com.coma.comaroom.event.entity.Event;
import com.coma.comaroom.event.entity.EventCategory;
import com.coma.comaroom.event.repository.EventRepository;
import com.coma.comaroom.event.service.AdminEventService;
import com.coma.comaroom.study.StudyError;
import com.coma.comaroom.study.dto.request.CreateStudyAttendanceRequest;
import com.coma.comaroom.study.dto.response.StudyAttendanceResponse;
import com.coma.comaroom.study.entity.*;
import com.coma.comaroom.study.repository.*;
import com.coma.comaroom.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;

/** 스터디장이 주차별 QR 출석 세션을 생성한다. */
@Service
@Transactional
@RequiredArgsConstructor
public class StudyAttendanceService {
    private final StudyAccessService access;
    private final StudyWeekRepository weeks;
    private final StudyAttendanceSessionRepository sessions;
    private final EventRepository events;
    private final AdminEventService eventAttendance;
    private final SecurityUtils security;

    /** 기존 이벤트 출석의 Redis QR 발급 로직을 재사용한다. */
    public StudyAttendanceResponse create(Long studyId, int number, CreateStudyAttendanceRequest request) {
        StudyAccessService.validateWeek(number);
        Study study = access.lockStudy(studyId);
        access.requireLeader(study, security.getCurrentMember().getMemberId());
        StudyWeek plan = weeks.findByStudyIdAndWeekNumber(studyId, number)
                .orElseThrow(() -> new BusinessException(StudyError.WEEK_NOT_FOUND));
        Event event = events.findById(request.eventId())
                .orElseThrow(() -> new BusinessException(StudyError.INVALID_INPUT));
        if (event.getEventCategory() != EventCategory.STUDY || event.getHost() == null
                || !event.getHost().getMemberId().equals(study.getStudyManager().getMemberId())) {
            throw new BusinessException(StudyError.INVALID_INPUT);
        }
        CreateAttendanceCheckRequestDto attendanceRequest = new CreateAttendanceCheckRequestDto(
                event.getEventId(), request.expirationTime());
        StudyAttendanceSession session = sessions.save(StudyAttendanceSession.builder()
                .plan(plan).event(event).build());
        Instant expiresAt = Instant.now().plusSeconds(request.expirationTime() * 60L);
        String qrCodeId = eventAttendance.createAttendanceCheck(attendanceRequest).getQrCodeId();
        return new StudyAttendanceResponse(session.getId(), qrCodeId, expiresAt);
    }
}
