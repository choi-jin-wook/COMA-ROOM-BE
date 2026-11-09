package com.coma.comaroom.study.entity;

import com.coma.comaroom.member.entity.Member;
import com.coma.comaroom.utils.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/** 회원별 스터디 참여 신청과 처리 상태를 저장한다. */
@Entity
@Table(name = "study_join_request", uniqueConstraints = @UniqueConstraint(
        name = "uk_study_join_member", columnNames = {"study_id", "member_id"}))
@Getter @Builder @NoArgsConstructor @AllArgsConstructor
public class StudyJoinRequest extends BaseEntity {
    /** 신청 대기, 승인, 거절 상태를 구분한다. */
    public enum Status { PENDING, APPROVED, REJECTED }
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "study_id", nullable = false)
    private Study study;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Status status = Status.PENDING;

    /** 재신청 시 기존 행을 대기 상태로 되돌린다. */
    public void requestAgain() {
        status = Status.PENDING;
    }
}
