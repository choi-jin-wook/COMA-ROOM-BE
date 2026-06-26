package com.coma.comaroom.event.repository;

import com.coma.comaroom.event.entity.ApprovalStatus;
import com.coma.comaroom.event.entity.EventApproval;
import com.coma.comaroom.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface EventApprovalRepository extends JpaRepository<EventApproval, Long> {
    // 1. 전체 조회 (정렬은 Pageable에서 처리)
    Page<EventApproval> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // 2. 상태별 조회 + 최근 생성순 페이징 (필드명 approvalStatus 필수 일치)
    Page<EventApproval> findByApprovalStatusOrderByCreatedAtDesc(ApprovalStatus approvalStatus, Pageable pageable);

    Long countByApprovalStatus(ApprovalStatus approvalStatus);

    Optional<EventApproval> findByApprovalStatus(ApprovalStatus approvalStatus);

    // 사용자 XP 내역: 특정 회원의 전체 승인 요청 (최신순)
    List<EventApproval> findByRequesterOrderByCreatedAtDesc(Member requester);

    // 사용자 XP 내역: 특정 회원 + 상태별 XP 합산용
    List<EventApproval> findByRequesterAndApprovalStatus(Member requester, ApprovalStatus approvalStatus);

    // 사용자 XP 메인페이지: 특정 회원의 페이징 조회
    Page<EventApproval> findByRequesterOrderByCreatedAtDesc(Member requester, Pageable pageable);

    // 사용자 XP 메인페이지: 특정 회원 + 상태별 페이징 조회
    Page<EventApproval> findByRequesterAndApprovalStatusOrderByCreatedAtDesc(Member requester, ApprovalStatus approvalStatus, Pageable pageable);

    // 사용자 XP 메인페이지: 특정 회원의 상태별 카운트
    Long countByRequesterAndApprovalStatus(Member requester, ApprovalStatus approvalStatus);
}
