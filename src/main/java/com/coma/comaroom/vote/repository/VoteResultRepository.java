package com.coma.comaroom.vote.repository;

import com.coma.comaroom.vote.entity.VoteResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VoteResultRepository extends JpaRepository<VoteResult, Long> {
}
