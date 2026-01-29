package com.coma.comaroom.event.repository;

import com.coma.comaroom.event.entity.EventPost;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventPostRepository extends JpaRepository<EventPost, Integer> {
}
