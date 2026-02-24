package com.coma.comaroom.notice.repository;


import com.coma.comaroom.notice.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NoticeRepository extends JpaRepository<Notice, Integer> {
    Optional<Notice> findFirstByOrderByCreatedAtDesc();
}
