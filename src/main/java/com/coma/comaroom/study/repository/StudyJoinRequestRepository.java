package com.coma.comaroom.study.repository;

import com.coma.comaroom.study.entity.StudyJoinRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface StudyJoinRequestRepository extends JpaRepository<StudyJoinRequest, Long> {
    Optional<StudyJoinRequest> findByStudyIdAndMemberMemberId(Long studyId, Long memberId);
}
