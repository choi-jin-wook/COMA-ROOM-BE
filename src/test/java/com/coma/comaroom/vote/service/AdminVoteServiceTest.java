package com.coma.comaroom.vote.service;

import com.coma.comaroom.utils.SecurityUtils;
import com.coma.comaroom.vote.dto.AddVoteOptionRequestDto;
import com.coma.comaroom.vote.dto.request.CreateNewVoteRequestDto;
import com.coma.comaroom.vote.dto.request.CreateVoteOptionRequestDto;
import com.coma.comaroom.vote.dto.request.UpdateVoteRequestDto;
import com.coma.comaroom.vote.dto.response.VoteDetailResponseDto;
import com.coma.comaroom.vote.dto.response.VoteOptionDetailResponseDto;
import com.coma.comaroom.vote.entity.Vote;
import com.coma.comaroom.vote.entity.VoteOption;
import com.coma.comaroom.vote.entity.VoteStatus;
import com.coma.comaroom.vote.repository.VoteOptionRepository;
import com.coma.comaroom.vote.repository.VoteRepository;
import com.coma.comaroom.vote.repository.VoteResultRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminVoteServiceTest {

    @Mock private VoteRepository voteRepository;
    @Mock private VoteOptionRepository voteOptionRepository;
    @Mock private VoteResultRepository voteResultRepository;
    @Mock private SecurityUtils securityUtils;

    @InjectMocks
    private AdminVoteService adminVoteService;

    private Vote vote;

    @BeforeEach
    void setUp() {
        vote = Vote.builder()
                .voteId(1L)
                .title("점심 메뉴 투표")
                .isMultiVote(false)
                .voteStatus(VoteStatus.IN_PROGRESS)
                .deadline(LocalDateTime.now().plusDays(3))
                .build();
    }

    // ─────────────────────────────────────────────
    // createNewVote
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("투표 생성 성공")
    void createNewVote_success() {
        CreateNewVoteRequestDto dto = CreateNewVoteRequestDto.builder()
                .title("점심 메뉴 투표")
                .isMultiple(false)
                .deadline(LocalDateTime.now().plusDays(3))
                .options(List.of(
                        CreateVoteOptionRequestDto.builder().content("짜장면").build(),
                        CreateVoteOptionRequestDto.builder().content("짬뽕").build()
                ))
                .build();

        when(voteRepository.save(any(Vote.class))).thenReturn(vote);

        VoteDetailResponseDto result = adminVoteService.createNewVote(dto);

        assertThat(result.getTitle()).isEqualTo("점심 메뉴 투표");
        assertThat(result.getStatus()).isEqualTo(VoteStatus.IN_PROGRESS);

        ArgumentCaptor<Vote> captor = ArgumentCaptor.forClass(Vote.class);
        verify(voteRepository).save(captor.capture());

        Vote saved = captor.getValue();
        assertThat(saved.getTitle()).isEqualTo("점심 메뉴 투표");
        assertThat(saved.getVoteStatus()).isEqualTo(VoteStatus.IN_PROGRESS);
        assertThat(saved.getVoteOptions())
                .extracting(VoteOption::getContent)
                .containsExactly("짜장면", "짬뽕");
        assertThat(saved.getVoteOptions()).allSatisfy(option ->
                assertThat(option.getVote()).isSameAs(saved));
    }

    // ─────────────────────────────────────────────
    // updateVote
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("투표 수정 성공")
    void updateVote_success() {
        UpdateVoteRequestDto dto = mock(UpdateVoteRequestDto.class);
        when(dto.getTitle()).thenReturn("수정된 투표");
        when(dto.getIsMultiple()).thenReturn(null);
        when(dto.getDeadline()).thenReturn(null);
        when(voteRepository.findById(1L)).thenReturn(Optional.of(vote));

        VoteDetailResponseDto result = adminVoteService.updateVote(dto, 1L);

        assertThat(result.getTitle()).isEqualTo("수정된 투표");
        assertThat(vote.getTitle()).isEqualTo("수정된 투표");
    }

    @Test
    @DisplayName("투표 수정 실패 - 투표 없음")
    void updateVote_notFound() {
        UpdateVoteRequestDto dto = mock(UpdateVoteRequestDto.class);
        when(voteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminVoteService.updateVote(dto, 99L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    // ─────────────────────────────────────────────
    // addVoteOption
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("투표 옵션 추가 성공")
    void addVoteOption_success() {
        AddVoteOptionRequestDto dto = AddVoteOptionRequestDto.builder()
                .content("새 옵션")
                .build();
        when(voteRepository.findById(1L)).thenReturn(Optional.of(vote));
        when(voteRepository.saveAndFlush(vote)).thenReturn(vote);

        VoteDetailResponseDto result = adminVoteService.addVoteOption(dto, 1L);

        assertThat(result.getOptions())
                .extracting(VoteOptionDetailResponseDto::getContent)
                .containsExactly("새 옵션");
        verify(voteRepository).saveAndFlush(vote);
        assertThat(vote.getVoteOptions())
                .extracting(VoteOption::getContent)
                .containsExactly("새 옵션");
        assertThat(vote.getVoteOptions().get(0).getVote()).isSameAs(vote);
    }

    @Test
    @DisplayName("투표 옵션 추가 실패 - 투표 없음")
    void addVoteOption_notFound() {
        AddVoteOptionRequestDto dto = AddVoteOptionRequestDto.builder()
                .content("새 옵션")
                .build();
        when(voteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminVoteService.addVoteOption(dto, 99L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    // ─────────────────────────────────────────────
    // deleteVoteOption
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("투표 옵션 삭제 성공")
    void deleteVoteOption_success() {
        assertThatNoException().isThrownBy(() -> adminVoteService.deleteVoteOption(1L, 1L));
        verify(voteOptionRepository).deleteById(1L);
    }

    // ─────────────────────────────────────────────
    // deleteVote
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("투표 삭제 성공")
    void deleteVote_success() {
        assertThatNoException().isThrownBy(() -> adminVoteService.deleteVote(1L));
        verify(voteRepository).deleteById(1L);
    }

    // ─────────────────────────────────────────────
    // closeVote
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("투표 종료 성공")
    void closeVote_success() {
        when(voteRepository.findById(1L)).thenReturn(Optional.of(vote));

        VoteDetailResponseDto result = adminVoteService.closeVote(1L);

        assertThat(result.getStatus()).isEqualTo(VoteStatus.CLOSED);
        assertThat(vote.getVoteStatus()).isEqualTo(VoteStatus.CLOSED);
    }

    @Test
    @DisplayName("투표 종료 실패 - 투표 없음")
    void closeVote_notFound() {
        when(voteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminVoteService.closeVote(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
