package com.coma.comaroom.study.entity;

import com.coma.comaroom.member.entity.Member;
import com.coma.comaroom.utils.BaseEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/** 스터디의 기본 정보, 운영 상태, 스터디장과 멤버십을 보관한다. */
@Entity
@Table(name = "study")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Study extends BaseEntity {
    // 스터디를 식별하는 기본 키다.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 스터디의 이름이다.
    @Column(name = "study_name", nullable = false)
    private String studyName;

    // 스터디를 운영하는 스터디장이다(manager_id).
    // 기존 데이터의 스터디장 참조가 사라져도 스터디 조회는 유지한다.
    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private Member studyManager;

    // 스터디 소개와 활동 내용이다.
    @Column(length = 2000)
    private String description;

    // 정기 모임 일정에 대한 설명이다.
    private String scheduleDescription;

    // 스터디에서 다루는 주제다.
    private String topic;

    // 스터디에 소속된 회원들의 참여 정보다(study_member 테이블).
    @OneToMany(mappedBy = "study", cascade = CascadeType.ALL)
    private List<StudyMember> studyMembers = new ArrayList<>();
}
