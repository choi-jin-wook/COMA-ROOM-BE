package com.coma.comaroom.study.entity;

import com.coma.comaroom.member.entity.Member;
import com.coma.comaroom.utils.BaseEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.time.OffsetDateTime;

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

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private Member studyManager;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(16) default 'ACTIVE'")
    @Builder.Default
    private StudyStatus status = StudyStatus.ACTIVE;

    @Column(length = 2000)
    private String description;

    private Integer maxMembers;
    private String scheduleDescription;
    private OffsetDateTime nextSessionAt;
    private OffsetDateTime completedAt;

    @Enumerated(EnumType.STRING)
    private StudyLevel level;

    @ElementCollection
    @CollectionTable(name = "study_tag", joinColumns = @JoinColumn(name = "study_id"))
    @Column(name = "tag")
    @OrderColumn(name = "tag_order")
    @Builder.Default
    private List<String> tags = new ArrayList<>();

    // 양방향 매핑이 필요한 경우 추가 (선택 사항)
    @OneToMany(mappedBy = "study", cascade = CascadeType.ALL)
    private List<StudyActivity> activities = new ArrayList<>();

    @OneToMany(mappedBy = "study", cascade = CascadeType.ALL)
    private List<StudyMember> studyMembers = new ArrayList<>();
}
