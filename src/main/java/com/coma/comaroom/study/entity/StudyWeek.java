package com.coma.comaroom.study.entity;

import com.coma.comaroom.utils.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/** 스터디별 1~16주차 계획의 제목과 선택적 내용을 저장한다. */
@Entity
@Table(name = "study_week", uniqueConstraints = @UniqueConstraint(
        name = "uk_study_week", columnNames = {"study_id", "week_number"}))
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudyWeek extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "study_id", nullable = false)
    private Study study;

    @Column(name = "week_number", nullable = false)
    private int weekNumber;

    @Column(nullable = false)
    private String title;

    private String topic;

    @Column(length = 2000)
    private String description;
}
