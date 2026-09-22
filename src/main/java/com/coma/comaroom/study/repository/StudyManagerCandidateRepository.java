package com.coma.comaroom.study.repository;

import com.coma.comaroom.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;

public interface StudyManagerCandidateRepository extends Repository<Member, Long> {
    Page<Member> findByNameContainingIgnoreCaseOrStudentIdContainingIgnoreCase(
            String name, String studentId, Pageable pageable);
}
