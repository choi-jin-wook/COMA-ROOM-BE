package com.coma.comaroom.notice.repository;


import com.coma.comaroom.notice.entity.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
    List<Notice> findByPinnedTrueAndHiddenFalse();
    Page<Notice> findByPinnedFalseAndHiddenFalse(Pageable pageable);

    long countByPinnedTrueAndHiddenFalse();

    // 1. 생성일자(createdAt) 기준 내림차순으로 가장 상위 1건 조회
    Optional<Notice> findFirstByOrderByCreatedAtDesc();
}
