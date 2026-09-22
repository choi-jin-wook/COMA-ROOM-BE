package com.coma.comaroom.study.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "study_material")
@Getter @Builder @NoArgsConstructor @AllArgsConstructor
public class StudyMaterial {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_id", nullable = false)
    private StudyWeek plan;
    @Column(nullable = false)
    private String fileName;
    @Column(nullable = false)
    private String contentType;
    @Column(nullable = false)
    private long sizeBytes;
    @Column(nullable = false, unique = true, length = 512)
    private String objectKey;
}
