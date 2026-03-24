package com.coma.comaroom.study.entity;

import com.coma.comaroom.member.entity.Member;
import com.coma.comaroom.utils.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "study")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Study extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "study_name", nullable = false)
    private String studyName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private Member studyManager;

    // 양방향 매핑이 필요한 경우 추가 (선택 사항)
    @OneToMany(mappedBy = "study", cascade = CascadeType.ALL)
    private List<StudyActivity> activities = new ArrayList<>();

    @OneToMany(mappedBy = "study", cascade = CascadeType.ALL)
    private List<StudyMember> studyMembers = new ArrayList<>();
}
