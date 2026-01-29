package com.coma.comaroom.notice.repository;


import com.coma.comaroom.notice.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeRepository extends JpaRepository<Notice, Integer> {
}
