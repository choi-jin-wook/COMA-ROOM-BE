package com.coma.comaroom.vote.service;

import com.coma.comaroom.BusinessException;
import com.coma.comaroom.member.entity.Major;
import com.coma.comaroom.member.entity.Member;
import com.coma.comaroom.member.entity.Role;
import com.coma.comaroom.utils.SecurityUtils;
import com.coma.comaroom.vote.VoteError;
import com.coma.comaroom.vote.dto.request.ParticipateVoteRequestDto;
import com.coma.comaroom.vote.dto.response.VoteDetailResponseDto;
import com.coma.comaroom.vote.entity.Vote;
import com.coma.comaroom.vote.entity.VoteResult;
import com.coma.comaroom.vote.entity.VoteStatus;
import com.coma.comaroom.vote.repository.VoteOptionRepository;
import com.coma.comaroom.vote.repository.VoteRepository;
import com.coma.comaroom.vote.repository.VoteResultRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VoteServiceTest {

    @Mock private VoteRepository voteRepository;
    @Mock private VoteOptionRepository voteOptionRepository;
    @Mock private VoteResultRepository voteResultRepository;
    @Mock private SecurityUtils securityUtils;

    @InjectMocks
    private VoteService voteService;

    private Member member;
    private Vote vote;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .memberId(1L)
                .studentId("20210001")
                .name("투표자")
                .password("encoded")
                .xp(0L)
                .role(Role.USER)
                .major(Major.COMPUTER_INFO)
                .build();

        vote = Vote.builder()
                .voteId(1L)
                .title("점심 메뉴 투표")
                .isMultiVote(false)
                .voteStatus(VoteStatus.IN_PROGRESS)
                .deadline(LocalDateTime.now().plusDays(3))
                .build();
    }

    // ─────────────────────────────────────────────
    // voteDashboard
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("투표 목록 조회 성공")
    void voteDashboard_success() {
        when(voteRepository.findAllByVoteStatusOrderByCreatedAtDesc(eq(VoteStatus.IN_PROGRESS), any())).thenReturn(List.of(vote));

        List<VoteDetailResponseDto> result = voteService.voteDashboard(0, VoteStatus.IN_PROGRESS);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getVoteId()).isEqualTo(1L);
        assertThat(result.get(0).getTitle()).isEqualTo("점심 메뉴 투표");
        verify(voteRepository).findAllByVoteStatusOrderByCreatedAtDesc(eq(VoteStatus.IN_PROGRESS), any());
    }

    @Test
    @DisplayName("투표 목록 조회 - 빈 결과")
    void voteDashboard_empty() {
        when(voteRepository.findAllByVoteStatusOrderByCreatedAtDesc(eq(VoteStatus.CLOSED), any())).thenReturn(List.of());

        List<VoteDetailResponseDto> result = voteService.voteDashboard(0, VoteStatus.CLOSED);

        assertThat(result).isEmpty();
    }

    // ─────────────────────────────────────────────
    // participateVote
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("투표 참여 성공")
    void participateVote_success() {
        ParticipateVoteRequestDto dto = mock(ParticipateVoteRequestDto.class);
        when(dto.getVoteOptionId()).thenReturn(List.of(1L));
        when(voteRepository.findById(1L)).thenReturn(Optional.of(vote));
        when(securityUtils.getCurrentMember()).thenReturn(member);

        VoteDetailResponseDto result = voteService.participateVote(dto, 1L);

        assertThat(result.getVoteId()).isEqualTo(1L);
        assertThat(result.getVoted()).isTrue();
    }

    @Test
    @DisplayName("투표 참여 실패 - 투표 없음")
    void participateVote_notFound() {
        ParticipateVoteRequestDto dto = mock(ParticipateVoteRequestDto.class);
        when(voteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> voteService.participateVote(dto, 99L))
                .isInstanceOf(BusinessException.class);
    }

    // ─────────────────────────────────────────────
    // cancelVote
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("투표 취소 성공")
    void cancelVote_success() {
        VoteResult result = mock(VoteResult.class);
        when(securityUtils.getCurrentMember()).thenReturn(member);
        when(voteResultRepository.existsByVoterAndVoteOption_Vote_VoteId(member, 1L)).thenReturn(true);
        when(voteResultRepository.findByVoterAndVoteOption_Vote_VoteId(member, 1L)).thenReturn(List.of(result));

        voteService.cancelVote(1L);

        verify(voteResultRepository).deleteAll(List.of(result));
    }

    @Test
    @DisplayName("투표 취소 실패 - 투표한 내역 없음")
    void cancelVote_notFound() {
        when(securityUtils.getCurrentMember()).thenReturn(member);
        when(voteResultRepository.existsByVoterAndVoteOption_Vote_VoteId(member, 1L)).thenReturn(false);

        assertThatThrownBy(() -> voteService.cancelVote(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessage(VoteError.VOTE_RESULT_NOT_FOUND.getMessage());
    }
}
