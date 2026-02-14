package com.coma.comaroom.notice.repository;


import com.coma.comaroom.notice.entity.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
    List<Notice> findByPinnedTrueAndHiddenFalse();
    Page<Notice> findByPinnedFalseAndHiddenFalse(Pageable pageable);

    long countByPinnedTrueAndHiddenFalse();
}
