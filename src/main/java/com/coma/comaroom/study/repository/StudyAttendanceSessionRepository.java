package com.coma.comaroom.study.repository;

import com.coma.comaroom.study.entity.StudyAttendanceSession;
import org.springframework.data.jpa.repository.JpaRepository;

/** 주차에 연결된 이벤트 출석 세션을 저장한다. */
public interface StudyAttendanceSessionRepository extends JpaRepository<StudyAttendanceSession, Long> {
}
