package com.coma.comaroom.study.repository;

import com.coma.comaroom.study.entity.StudyWeek;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface StudyWeekRepository extends JpaRepository<StudyWeek, Long> {
    List<StudyWeek> findByStudyIdOrderByWeekNumberAsc(Long studyId);
    Optional<StudyWeek> findByStudyIdAndWeekNumber(Long studyId, int weekNumber);
    boolean existsByStudyIdAndWeekNumber(Long studyId, int weekNumber);
}
