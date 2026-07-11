package com.coma.comaroom.member.repository;

import com.coma.comaroom.member.entity.Member;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByStudentId(String studentId);
    void deleteByStudentId(String studentId);

    @Query("SELECT COUNT(m) + 1 FROM Member m " +
            "WHERE m.xp > :#{#member.xp} " +
            "OR (m.xp = :#{#member.xp} AND m.memberId < :#{#member.memberId})")
    Long findRankByMember(@Param("member") Member member);


    List<Member> findAllByOrderByXpDescMemberIdAsc(Pageable pageable);

    Page<Member> findPageByOrderByXpDescMemberIdAsc(Pageable pageable);

    @Query("SELECT COALESCE(AVG(m.xp), 0) FROM Member m")
    Double findAverageXp();

    boolean existsByStudentId(String studentId);

    // @SQLRestriction("status = 'ACTIVE'")을 우회해 탈퇴 회원까지 포함한 학번 중복 확인
    @Query(value = "SELECT COUNT(*) FROM member WHERE student_id = :studentId", nativeQuery = true)
    long countByStudentIdIncludingWithdrawn(@Param("studentId") String studentId);
}
