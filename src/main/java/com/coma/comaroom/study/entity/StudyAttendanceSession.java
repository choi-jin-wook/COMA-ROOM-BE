package com.coma.comaroom.study.entity;

import com.coma.comaroom.event.entity.Event;
import jakarta.persistence.*;
import lombok.*;

/** 주차 계획과 기존 이벤트 출석을 연결한다. */
@Entity
@Table(name = "study_attendance_session")
@Getter @Builder @NoArgsConstructor @AllArgsConstructor
public class StudyAttendanceSession {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_id", nullable = false)
    private StudyWeek plan;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;
}
