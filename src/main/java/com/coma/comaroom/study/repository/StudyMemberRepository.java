package com.coma.comaroom.study.repository;

import com.coma.comaroom.study.entity.StudyMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudyMemberRepository extends JpaRepository<StudyMember, Long> {
    boolean existsByStudyIdAndMemberMemberId(Long studyId, Long memberId);
    Optional<StudyMember> findByStudyIdAndMemberMemberId(Long studyId, Long memberId);
}
