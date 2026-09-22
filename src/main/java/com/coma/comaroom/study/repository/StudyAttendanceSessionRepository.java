package com.coma.comaroom.study.repository;

import com.coma.comaroom.study.entity.StudyAttendanceSession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;

public interface StudyAttendanceSessionRepository extends JpaRepository<StudyAttendanceSession, Long> {
    boolean existsByPlanIdAndExpiresAtAfter(Long planId, Instant now);
}
