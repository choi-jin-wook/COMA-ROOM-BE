package com.coma.comaroom.study.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "study_attendance_session")
@Getter @Builder @NoArgsConstructor @AllArgsConstructor
public class StudyAttendanceSession {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_id", nullable = false)
    private StudyWeek plan;
    @Column(nullable = false, unique = true, length = 36)
    private String qrCodeId;
    @Column(nullable = false)
    private Instant expiresAt;
}
