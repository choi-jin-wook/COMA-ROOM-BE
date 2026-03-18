package com.coma.comaroom.event.repository;

import com.coma.comaroom.event.entity.Event;
import com.coma.comaroom.event.entity.EventCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {
    Optional<Event> findByTitleAndEventCategory(String title, EventCategory category);
    Optional<Event> findFirstByEventDateAfterOrderByEventDateAsc(LocalDateTime now);
    List<Event> findAllByOrderByEventDateDesc();
    // 이달의 이벤트 조회를 위한 기간 검색
    List<Event> findAllByEventDateBetween(LocalDateTime start, LocalDateTime end);}
