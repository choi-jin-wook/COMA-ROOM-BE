package com.coma.comaroom.vote.repository;

import com.coma.comaroom.vote.entity.Vote;
import com.coma.comaroom.vote.entity.VoteStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {
    Optional<Vote> findFirstByVoteStatusOrderByCreatedAtDesc(VoteStatus status);

    List<Vote> findAllByVoteStatusOrderByCreatedAtDesc(VoteStatus status, Pageable pageable);
}
