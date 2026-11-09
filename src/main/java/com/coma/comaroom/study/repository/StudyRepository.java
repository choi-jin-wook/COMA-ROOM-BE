package com.coma.comaroom.study.repository;

import com.coma.comaroom.study.entity.Study;
import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

/** 스터디 조회와 모집·내 스터디 목록의 조건부 쿼리를 제공한다. */
public interface StudyRepository extends JpaRepository<Study, Long> {
    /** 참여 신청 등의 동시성 검사를 위해 스터디 행을 쓰기 잠금으로 조회한다. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Study s where s.id = :id")
    Optional<Study> findForUpdate(@Param("id") Long id);

    /** 스터디장 또는 멤버로 속한 스터디를 조회한다. */
    @Query("""
            select s from Study s left join s.studyManager manager where
            manager.memberId = :memberId or exists
              (select sm.id from StudyMember sm where sm.study = s and sm.member.memberId = :memberId)
            """)
    Page<Study> findMine(@Param("memberId") Long memberId, Pageable pageable);

    /** 현재 회원이 참여하지 않은 스터디를 조회한다. */
    @Query("""
            select s from Study s join s.studyManager manager
            where manager.memberId <> :memberId
            and not exists (select sm.id from StudyMember sm where sm.study = s and sm.member.memberId = :memberId)
            """)
    Page<Study> findRecruiting(@Param("memberId") Long memberId, Pageable pageable);
}
