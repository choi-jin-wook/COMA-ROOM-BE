package com.coma.comaroom.event.repository;

import com.coma.comaroom.event.entity.EventParticipant;
import com.coma.comaroom.event.entity.EventPost;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventParticipateRepository extends JpaRepository<EventParticipant, Long> {
}
