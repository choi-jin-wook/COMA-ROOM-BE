package com.coma.comaroom.study.repository;

import com.coma.comaroom.study.entity.StudyParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

/** 기존 스터디 참여자 엔티티의 기본 영속성 작업을 제공한다. */
public interface StudyParticipantRepository extends JpaRepository<StudyParticipant, Integer> {
}
