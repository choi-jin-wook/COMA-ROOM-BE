package com.coma.comaroom.event.repository;

import com.coma.comaroom.event.entity.EventApproval;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventApprovalRepository extends JpaRepository<EventApproval, Long> {
}
