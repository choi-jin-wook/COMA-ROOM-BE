package com.coma.comaroom.study.repository;

import com.coma.comaroom.study.entity.Study;
import org.springframework.data.jpa.repository.JpaRepository;
import com.coma.comaroom.study.entity.StudyStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface StudyRepository extends JpaRepository<Study, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Study s where s.id = :id")
    Optional<Study> findForUpdate(@Param("id") Long id);

    @Query("""
            select s from Study s left join s.studyManager manager where s.status = :status and
            (manager.memberId = :memberId or exists
              (select sm.id from StudyMember sm where sm.study = s and sm.member.memberId = :memberId))
            """)
    Page<Study> findMine(@Param("memberId") Long memberId, @Param("status") StudyStatus status, Pageable pageable);

    @Query("""
            select count(s) from Study s left join s.studyManager manager where s.status = :status and
            (manager.memberId = :memberId or exists
              (select sm.id from StudyMember sm where sm.study = s and sm.member.memberId = :memberId))
            """)
    long countMine(@Param("memberId") Long memberId, @Param("status") StudyStatus status);

    @Query("""
            select s from Study s join s.studyManager manager
            where s.status = com.coma.comaroom.study.entity.StudyStatus.ACTIVE
            and manager.memberId <> :memberId
            and not exists (select sm.id from StudyMember sm where sm.study = s and sm.member.memberId = :memberId)
            and (s.maxMembers is null or s.maxMembers >
                ((select count(distinct member.memberId) from StudyMember sm join sm.member member where sm.study = s)
                 + case when exists (select sm.id from StudyMember sm where sm.study = s
                     and sm.member.memberId = manager.memberId) then 0 else 1 end))
            """)
    Page<Study> findRecruiting(@Param("memberId") Long memberId, Pageable pageable);
}
