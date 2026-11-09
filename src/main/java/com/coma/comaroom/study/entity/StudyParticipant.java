package com.coma.comaroom.study.entity;

import com.coma.comaroom.member.entity.Member;
import com.coma.comaroom.utils.BaseEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import lombok.*;

/** 기존 스터디 참여자 테이블과 회원·스터디 관계를 매핑한다. */
@Entity
@Table(name = "study_participant")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class StudyParticipant extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id")
    private Study study;
}
