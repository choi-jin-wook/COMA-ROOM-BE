package com.coma.comaroom.study.repository;

import com.coma.comaroom.study.entity.StudyMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface StudyMaterialRepository extends JpaRepository<StudyMaterial, Long> {
    List<StudyMaterial> findByPlanIdOrderByIdAsc(Long planId);
    Optional<StudyMaterial> findByIdAndPlanStudyIdAndPlanWeekNumber(Long id, Long studyId, int weekNumber);
}
