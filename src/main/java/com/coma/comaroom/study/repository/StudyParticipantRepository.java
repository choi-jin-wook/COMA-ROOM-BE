package com.coma.comaroom.study.repository;

import com.coma.comaroom.study.entity.StudyParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyParticipantRepository extends JpaRepository<StudyParticipant, Integer> {
}
