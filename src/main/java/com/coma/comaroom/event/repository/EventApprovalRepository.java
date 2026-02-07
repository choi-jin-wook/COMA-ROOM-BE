package com.coma.comaroom.event.repository;

import com.coma.comaroom.event.entity.ApprovalStatus;
import com.coma.comaroom.event.entity.EventApproval;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface EventApprovalRepository extends JpaRepository<EventApproval, Long> {
    // 1. 전체 조회 (정렬은 Pageable에서 처리)
    Page<EventApproval> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // 2. 상태별 조회 + 최근 생성순 페이징 (필드명 approvalStatus 필수 일치)
    Page<EventApproval> findByApprovalStatusOrderByCreatedAtDesc(ApprovalStatus approvalStatus, Pageable pageable);

    Long countByApprovalStatus(ApprovalStatus approvalStatus);

    Optional<EventApproval> findByApprovalStatus(ApprovalStatus approvalStatus);


}
