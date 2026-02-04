package com.coma.comaroom;

import com.coma.comaroom.member.entity.Member;
import com.coma.comaroom.member.entity.Role;
import com.coma.comaroom.utils.SecurityUtils;
import com.coma.comaroom.vote.component.VoteMapper;
import com.coma.comaroom.vote.dto.AddVoteOptionRequestDto;
import com.coma.comaroom.vote.dto.request.CreateNewVoteRequestDto;
import com.coma.comaroom.vote.dto.request.CreateVoteOptionRequestDto;
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
import com.coma.comaroom.vote.service.VoteService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

// 1. AssertJ 종합 선물 세트 (assertThat, assertThatThrownBy 등 포함)
import static org.assertj.core.api.Assertions.*;

// 2. Mockito 도구들 (when, verify, times 등)
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

//@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class VoteServiceTest {

    @Mock // 가짜 객체 생성
    private VoteRepository voteRepository;

    @Mock // 가짜 객체 생성
    private VoteOptionRepository voteOptionRepository;

    @Mock
    private VoteResultRepository voteResultRepository;

    @InjectMocks // 위에서 만든 @Mock 객체들을 VoteService 생성자에 자동으로 넣어줌
    private VoteService voteService;

    @Mock
    private SecurityUtils securityUtils;

    @Spy
    private VoteMapper voteMapper;

    // 사용자
    // 1. 투표 메인페이지
    @Test
    @DisplayName("투표 대시보드 조회 테스트 - 상태와 페이징이 적용된 목록을 반환해야 한다")
    void voteDashboardTest() {
        // 1. Given: 가짜 투표 데이터 2개 생성
        Vote vote1 = Vote.builder()
                .voteId(1L).title("투표 1").isMultiVote(true).voteStatus(VoteStatus.IN_PROGRESS)
                .voteOptions(new ArrayList<>()).build();

        // 옵션 추가 (카운트 테스트용)
        vote1.addOption(VoteOption.builder().voteOptionId(10L).content("옵션 A").build());

        List<Vote> mockVotes = List.of(vote1);

        // Pageable 검증: page=1이면 pageNumber=0, size=5여야 함
        Pageable expectedPageable = PageRequest.of(0, 5);

        when(voteRepository.findAllByVoteStatusOrderByCreatedAtDesc(eq(VoteStatus.IN_PROGRESS), any(Pageable.class)))
                .thenReturn(mockVotes);

        // 2. When: 1페이지, 진행중(IN_PROGRESS) 상태로 조회
        List<VoteDetailResponseDto> response = voteService.voteDashboard(1, VoteStatus.IN_PROGRESS);

        // 3. Then: 결과 검증
        assertThat(response).hasSize(1);
        assertThat(response.get(0).getTitle()).isEqualTo("투표 1");
        assertThat(response.get(0).getIsMultiple()).isTrue();
        assertThat(response.get(0).getOptions().get(0).getContent()).isEqualTo("옵션 A");
        assertThat(response.get(0).getOptions().get(0).getCount()).isEqualTo(0L); // 결과 없을 때 0 확인

        // Repository 호출 시 인자값이 정확했는지 검증
        verify(voteRepository).findAllByVoteStatusOrderByCreatedAtDesc(VoteStatus.IN_PROGRESS, expectedPageable);
    }

    // 2. 투표 참여
    @Test
    @DisplayName("투표 참여 성공 테스트 - Spy NPE 해결")
    void participateVote_Success() {
        // 1. Given
        Long voteId = 1L;
        Long optionId = 10L;
        Member member = Member.builder().memberId(1L).build();

        // VoteOption 리스트 초기화 필수
        VoteOption option = VoteOption.builder()
                .voteOptionId(optionId)
                .voteResults(new ArrayList<>())
                .build();

        Vote vote = Vote.builder()
                .voteId(voteId)
                .voteOptions(new ArrayList<>(List.of(option)))
                .build();

        ParticipateVoteRequestDto requestDto = new ParticipateVoteRequestDto(List.of(optionId));

        // 2. Mocking
        when(voteRepository.findById(voteId)).thenReturn(Optional.of(vote));
        when(securityUtils.getCurrentMember()).thenReturn(member);

        // [핵심] @Spy 객체의 NPE를 방지하기 위해 doReturn 사용
        // doReturn(결과값).when(스파이객체).메서드호출(인자);
        doReturn(new VoteDetailResponseDto())
                .when(voteMapper)
                .toDetailDto(any(Vote.class));

        // 3. When
        VoteDetailResponseDto response = voteService.participateVote(requestDto, voteId);

        // 4. Then
        assertThat(response).isNotNull();
        assertThat(vote.getVoteOptions().get(0).getVoteResults()).hasSize(1);

        verify(voteRepository).findById(voteId);
        verify(securityUtils).getCurrentMember();
    }


    // 관리자
    // 1. 투표생성
    @Test
    void createNewVote_success() {
        // given
        CreateNewVoteRequestDto request = CreateNewVoteRequestDto.builder()
                .title("새 투표")
                .isMultiple(true)
                .options(List.of(
                        CreateVoteOptionRequestDto.builder().content("옵션1").build(),
                        CreateVoteOptionRequestDto.builder().content("옵션2").build()
                ))
                .build();

        when(voteRepository.save(any(Vote.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        VoteDetailResponseDto result = voteService.createNewVote(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("새 투표");
        assertThat(result.getOptions()).hasSize(2);
        assertThat(result.getStatus()).isEqualTo(VoteStatus.IN_PROGRESS);

        verify(voteRepository, times(1)).save(any(Vote.class));
    }


    // 2. 투표 수정
    @Test
    @DisplayName("투표 수정 테스트 - 제목과 마감기한이 정상적으로 수정되어야 한다")
    void updateVoteTest() {
        // 1. Given: 기존 투표 데이터 생성
        Vote existingVote = Vote.builder()
                .voteId(1L)
                .title("수정 전 제목")
                .isMultiVote(false)
                .voteStatus(VoteStatus.IN_PROGRESS)
                .deadline(LocalDateTime.now().plusDays(1))
                .voteOptions(new ArrayList<>()) // 초기화
                .build();

        // 기존 투표에 옵션 하나 추가
        existingVote.addOption(VoteOption.builder().voteOptionId(10L).content("기존 옵션").build());

        // 수정 요청 DTO 생성 (제목과 중복투표 여부 변경)
        UpdateVoteRequestDto requestDto = UpdateVoteRequestDto.builder()
                .title("수정 후 제목")
                .isMultiple(true)
                .deadline(LocalDateTime.now().plusDays(2))
                .build();

        // 레포지토리 동작 모킹
        when(voteRepository.findById(1L)).thenReturn(Optional.of(existingVote));

        // 2. When: 수정 메서드 실행
        VoteDetailResponseDto response = voteService.updateVote(requestDto, 1L);

        // 3. Then: 검증
        assertThat(response.getTitle()).isEqualTo("수정 후 제목");
        assertThat(response.getIsMultiple()).isTrue();
        assertThat(response.getVoteId()).isEqualTo(1L);

        // 기존 옵션이 그대로 잘 있는지 확인 (NPE 방지 로직 검증 포함)
        assertThat(response.getOptions()).hasSize(1);
        assertThat(response.getOptions().get(0).getContent()).isEqualTo("기존 옵션");
        assertThat(response.getOptions().get(0).getCount()).isEqualTo(0L); // 투표 결과가 null일 때 0인지 확인

        // 엔티티 필드 업데이트 확인
        verify(voteRepository, times(1)).findById(1L);
    }


    // 3. 옵션 추가
    @Test
    void addVoteOption_success() {
        // given
        Vote vote = Vote.builder()
                .title("투표")
                .isMultiVote(true)
                .voteStatus(VoteStatus.IN_PROGRESS)
                .build();

        when(voteRepository.findById(1L))
                .thenReturn(Optional.of(vote));

        AddVoteOptionRequestDto request =
                AddVoteOptionRequestDto.builder()
                        .content("새 옵션")
                        .build();

        // when
        VoteDetailResponseDto result =
                voteService.addVoteOption(request, 1L);

        // then
        assertThat(result.getOptions()).hasSize(1);
        assertThat(result.getOptions().get(0).getContent())
                .isEqualTo("새 옵션");
    }
    // 4. 옵션 삭제
    @Test
    @DisplayName("투표 옵션 삭제 테스트 - Repository의 deleteById가 호출되어야 한다")
    void deleteVoteOptionTest() {
        // 1. Given
        Long optionId = 10L;
        // doNothing: void 메서드가 호출될 때 아무 일도 일어나지 않도록 설정
        doNothing().when(voteOptionRepository).deleteById(optionId);

        // 2. When
        voteService.deleteVoteOption(optionId, optionId);

        // 3. Then: 실제로 해당 ID로 삭제 명령이 내려갔는지 확인
        verify(voteOptionRepository, times(1)).deleteById(optionId);
    }

    // 5. 투표 삭제
    @Test
    @DisplayName("투표 삭제 테스트 - Repository의 deleteById가 호출되어야 한다")
    void deleteVoteTest() {
        // 1. Given
        Long voteId = 1L;
        doNothing().when(voteRepository).deleteById(voteId);

        // 2. When
        voteService.deleteVote(voteId);

        // 3. Then: 실제로 해당 ID로 삭제 명령이 내려갔는지 확인
        verify(voteRepository, times(1)).deleteById(voteId);
    }

    // 6. 투표 종료
    @Test
    @DisplayName("투표 마감 테스트 - 상태가 CLOSED로 변경되어야 한다")
    void closeVoteSuccessTest() {
        // 1. Given: 진행 중인 투표 데이터 준비
        Vote openVote = Vote.builder()
                .voteId(100L)
                .title("마감될 투표")
                .voteStatus(VoteStatus.IN_PROGRESS) // 현재 진행 중
                .voteOptions(new ArrayList<>())
                .build();

        when(voteRepository.findById(100L)).thenReturn(Optional.of(openVote));

        // 2. When: 마감 메서드 실행
        VoteDetailResponseDto response = voteService.closeVote(100L);

        // 3. Then: 검증
        assertThat(response.getStatus()).isEqualTo(VoteStatus.CLOSED); // 상태 변경 확인
        assertThat(response.getVoteId()).isEqualTo(100L);

        // 더티 체킹에 의해 엔티티 상태가 변했는지 확인
        assertThat(openVote.getVoteStatus()).isEqualTo(VoteStatus.CLOSED);

        verify(voteRepository, times(1)).findById(100L);
    }

    @Test
    @DisplayName("투표 마감 실패 테스트 - 존재하지 않는 ID일 경우 예외가 발생한다")
    void closeVoteFailTest() {
        // 1. Given: 존재하지 않는 ID 설정
        when(voteRepository.findById(999L)).thenReturn(Optional.empty());

        // 2. When & 3. Then: 예외 발생 검증
        assertThatThrownBy(() -> voteService.closeVote(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("해당 투표를 찾을 수 없습니다.");
    }
}
