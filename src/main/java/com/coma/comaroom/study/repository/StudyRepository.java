package com.coma.comaroom.study.repository;

import com.coma.comaroom.study.entity.Study;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyRepository extends JpaRepository<Study, Long> {
}
