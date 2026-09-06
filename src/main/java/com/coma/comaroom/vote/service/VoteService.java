package com.coma.comaroom.vote.service;

import com.coma.comaroom.BusinessException;
import com.coma.comaroom.member.entity.Member;
import com.coma.comaroom.member.repository.MemberRepository;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Transactional
public class VoteService {
    private final VoteRepository voteRepository;
    private final VoteOptionRepository voteOptionRepository;
    private final VoteResultRepository voteResultRepository;
    private final MemberRepository memberRepository;

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

        if (vote.getVoteStatus() == VoteStatus.CLOSED || vote.getDeadline().isBefore(LocalDateTime.now())) {
            throw new BusinessException(VoteError.VOTE_CLOSED);
        }

        if (voteResultRepository.existsByVoterAndVoteOption_Vote_VoteId(member, voteId)) {
            throw new BusinessException(VoteError.ALREADY_VOTED);
        }

        List<Long> voteOptionIds = participateVoteRequestDto.getVoteOptionId();
        if (!vote.isMultiVote() && voteOptionIds.size() > 1) {
            throw new BusinessException(VoteError.MULTI_VOTE_NOT_ALLOWED);
        }

        Set<Long> validOptionIds = vote.getVoteOptions().stream()
                .map(VoteOption::getVoteOptionId)
                .collect(Collectors.toSet());
        if (voteOptionIds.isEmpty() || !validOptionIds.containsAll(voteOptionIds)) {
            throw new BusinessException(VoteError.VOTE_OPTION_NOT_FOUND);
        }

        vote.participate(voteOptionIds, member);

        // exists() 체크와 저장 사이의 경쟁(check-then-act)으로 중복 저장이 시도될 수 있다.
        // vote_result 의 유니크 제약(uk_voter_option)에 기대되, 커밋 시점이 아닌 지금 flush 해
        // 제약 위반을 잡아 500 대신 명확한 ALREADY_VOTED(409)로 변환한다.
        try {
            voteResultRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(VoteError.ALREADY_VOTED);
        }

        // XP 는 애플리케이션 레벨 read-modify-write 대신 DB 원자적 증가로 갱신해 lost update 를 방지한다.
        memberRepository.incrementXp(member.getMemberId(), 2L);

        VoteDetailResponseDto dto = voteMapper.toDetailDto(vote);
        dto.setVoted(true);
        return dto;
    }

    // 3. 투표 취소
    public void cancelVote(Long voteId) {
        Member member = securityUtils.getCurrentMember();

        if (!voteResultRepository.existsByVoterAndVoteOption_Vote_VoteId(member, voteId)) {
            throw new BusinessException(VoteError.VOTE_RESULT_NOT_FOUND);
        }

        List<VoteResult> results = voteResultRepository.findByVoterAndVoteOption_Vote_VoteId(member, voteId);
        voteResultRepository.deleteAll(results);

        if (member.getXp() >= 2) {
            member.setXp(member.getXp() - 2);
        }
    }
}
