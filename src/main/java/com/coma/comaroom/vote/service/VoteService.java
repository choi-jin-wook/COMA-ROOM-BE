package com.coma.comaroom.vote.service;

import com.coma.comaroom.BusinessException;
import com.coma.comaroom.member.entity.Member;
import com.coma.comaroom.utils.SecurityUtils;
import com.coma.comaroom.vote.VoteError;
import com.coma.comaroom.vote.component.VoteMapper;
import com.coma.comaroom.vote.dto.AddVoteOptionRequestDto;
import com.coma.comaroom.vote.dto.request.CreateNewVoteRequestDto;
import com.coma.comaroom.vote.dto.request.ParticipateVoteRequestDto;
import com.coma.comaroom.vote.dto.request.UpdateVoteRequestDto;
import com.coma.comaroom.vote.dto.response.VoteDetailResponseDto;
import com.coma.comaroom.vote.entity.Vote;
import com.coma.comaroom.vote.entity.VoteOption;
import com.coma.comaroom.vote.entity.VoteResult;
import com.coma.comaroom.vote.entity.VoteStatus;
import com.coma.comaroom.vote.repository.VoteOptionRepository;
import com.coma.comaroom.vote.repository.VoteRepository;
import com.coma.comaroom.vote.repository.VoteResultRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@AllArgsConstructor
@Transactional
public class VoteService {
    private final VoteRepository voteRepository;
    private final VoteOptionRepository voteOptionRepository;
    private final VoteResultRepository voteResultRepository;

    private final VoteMapper voteMapper;
    private final SecurityUtils securityUtils;

    // - 사용자
    // 1. 전체 투표 조회
    public List<VoteDetailResponseDto> voteDashboard(Integer page, VoteStatus status) {
        final int PAGE_SIZE = 5;
        Pageable pageable = PageRequest.of(page, PAGE_SIZE);
        List<Vote> votes = voteRepository.findAllByVoteStatusOrderByCreatedAtDesc(status, pageable);
        Member member = securityUtils.getCurrentMember();

        return votes.stream()
                .map(vote -> {
                    VoteDetailResponseDto dto = voteMapper.toDetailDto(vote);
                    dto.setVoted(voteResultRepository.existsByVoterAndVoteOption_Vote_VoteId(member, vote.getVoteId()));
                    return dto;
                })
                .toList();
    }

    // 2. 투표 참여
    public VoteDetailResponseDto participateVote(ParticipateVoteRequestDto participateVoteRequestDto, Long voteId) {
        Vote vote = voteRepository.findById(voteId).orElseThrow(() ->  new BusinessException(VoteError.VOTE_NOT_FOUND));
        Member member = securityUtils.getCurrentMember();
        member.setXp(member.getXp() + 2);

        vote.participate(participateVoteRequestDto.getVoteOptionId(), member);
        VoteDetailResponseDto dto = voteMapper.toDetailDto(vote);
        dto.setVoted(true);
        return dto;
    }

    // 3. 투표 취소
    public void cancelVote(Long voteId) {
        Member member = securityUtils.getCurrentMember();
        if (member.getXp() >= 2) {
            member.setXp(member.getXp() - 2);
        }

        if (!voteResultRepository.existsByVoterAndVoteOption_Vote_VoteId(member, voteId)) {
            throw new BusinessException(VoteError.VOTE_RESULT_NOT_FOUND);
        }

        List<VoteResult> results = voteResultRepository.findByVoterAndVoteOption_Vote_VoteId(member, voteId);
        voteResultRepository.deleteAll(results);
    }
}
