package com.coma.comaroom.study.repository;

import com.coma.comaroom.study.entity.StudyMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/** 스터디 멤버십 확인과 회원 수 집계를 제공한다. */
public interface StudyMemberRepository extends JpaRepository<StudyMember, Long> {
    boolean existsByStudyIdAndMemberMemberId(Long studyId, Long memberId);
    Optional<StudyMember> findByStudyIdAndMemberMemberId(Long studyId, Long memberId);

    /** 중복 멤버 행이 있어도 회원은 한 번만 센다. */
    @Query("select count(distinct member.memberId) from StudyMember sm join sm.member member where sm.study.id = :studyId")
    long countMembers(@Param("studyId") Long studyId);
}
