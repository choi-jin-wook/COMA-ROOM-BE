package com.coma.comaroom.vote.repository;

import com.coma.comaroom.member.entity.Member;
import com.coma.comaroom.vote.entity.VoteResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VoteResultRepository extends JpaRepository<VoteResult, Long> {
    List<VoteResult> findByVoterAndVoteOption_Vote_VoteId(Member voter, Long voteId);
    boolean existsByVoterAndVoteOption_Vote_VoteId(Member voter, Long voteId);
}
