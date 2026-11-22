package com.coma.comaroom.study.repository;

import com.coma.comaroom.study.entity.StudyJoinRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/** 회원의 스터디별 참여 신청을 조회하고 저장한다. */
public interface StudyJoinRequestRepository extends JpaRepository<StudyJoinRequest, Long> {
    Optional<StudyJoinRequest> findByStudyIdAndMemberMemberId(Long studyId, Long memberId);
}
