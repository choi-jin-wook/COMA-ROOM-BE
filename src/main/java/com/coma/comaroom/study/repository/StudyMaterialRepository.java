package com.coma.comaroom.study.repository;

import com.coma.comaroom.study.entity.StudyMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

/** 주차 자료 조회와 스터디·주차 소속 검증용 쿼리를 제공한다. */
public interface StudyMaterialRepository extends JpaRepository<StudyMaterial, Long> {
    List<StudyMaterial> findByPlanIdOrderByIdAsc(Long planId);
    Optional<StudyMaterial> findByIdAndPlanStudyIdAndPlanWeekNumber(Long id, Long studyId, int weekNumber);
}
