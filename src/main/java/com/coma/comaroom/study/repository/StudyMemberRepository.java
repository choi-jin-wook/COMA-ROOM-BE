package com.coma.comaroom.study.repository;

import com.coma.comaroom.study.entity.StudyMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StudyMemberRepository extends JpaRepository<StudyMember, Long> {
    boolean existsByStudyIdAndMemberMemberId(Long studyId, Long memberId);
    Optional<StudyMember> findByStudyIdAndMemberMemberId(Long studyId, Long memberId);

    @Query("select count(distinct member.memberId) from StudyMember sm join sm.member member where sm.study.id = :studyId")
    long countMembers(@Param("studyId") Long studyId);

    @Query("select coalesce(sum(sm.earnedXp), 0) from StudyMember sm where sm.member.memberId = :memberId")
    long totalEarnedXp(@Param("memberId") Long memberId);
}
