package com.coma.comaroom.study.entity;

import com.coma.comaroom.member.entity.Member;
import com.coma.comaroom.utils.BaseEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import lombok.*;

/** 스터디에 등록된 회원을 저장한다. */
@Entity
@Table(name = "study_member")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class StudyMember extends BaseEntity {
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
