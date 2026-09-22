package com.coma.comaroom.study.entity;

import com.coma.comaroom.utils.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "study_activity")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class StudyActivity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String activityName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id") // FK 컬럼 명시
    private Study study;
}
