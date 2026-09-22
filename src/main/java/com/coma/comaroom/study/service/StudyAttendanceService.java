package com.coma.comaroom.study.service;

import com.coma.comaroom.BusinessException;
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
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class StudyAttendanceService {
    private final StudyAccessService access;
    private final StudyWeekRepository weeks;
    private final StudyAttendanceSessionRepository sessions;
    private final SecurityUtils security;

    public StudyAttendanceResponse create(Long studyId, int number, CreateStudyAttendanceRequest request) {
        StudyAccessService.validateWeek(number);
        Study study = access.lockStudy(studyId);
        access.requireLeader(study, security.getCurrentMember().getMemberId());
        StudyWeek plan = weeks.findByStudyIdAndWeekNumber(studyId, number)
                .orElseThrow(() -> new BusinessException(StudyError.WEEK_NOT_FOUND));
        Instant now = Instant.now();
        if (sessions.existsByPlanIdAndExpiresAtAfter(plan.getId(), now)) {
            throw new BusinessException(StudyError.ATTENDANCE_CONFLICT);
        }
        StudyAttendanceSession session = sessions.save(StudyAttendanceSession.builder().plan(plan)
                .qrCodeId(UUID.randomUUID().toString()).expiresAt(now.plusSeconds(request.expirationTime() * 60L))
                .build());
        return new StudyAttendanceResponse(session.getId(), session.getQrCodeId(), session.getExpiresAt());
    }
}
