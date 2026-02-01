package com.coma.comaroom.member.repository;

import com.coma.comaroom.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByStudentId(String studentId);
    void deleteByStudentId(String studentId);


}
