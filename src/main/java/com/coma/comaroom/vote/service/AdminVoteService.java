package com.coma.comaroom.vote.service;

import com.coma.comaroom.utils.SecurityUtils;
import com.coma.comaroom.vote.component.VoteMapper;
import com.coma.comaroom.vote.dto.AddVoteOptionRequestDto;
import com.coma.comaroom.vote.dto.request.CreateNewVoteRequestDto;
import com.coma.comaroom.vote.dto.request.UpdateVoteRequestDto;
import com.coma.comaroom.vote.dto.response.VoteDetailResponseDto;
import com.coma.comaroom.vote.entity.Vote;
import com.coma.comaroom.vote.entity.VoteOption;
import com.coma.comaroom.vote.entity.VoteStatus;
import com.coma.comaroom.vote.repository.VoteOptionRepository;
import com.coma.comaroom.vote.repository.VoteRepository;
import com.coma.comaroom.vote.repository.VoteResultRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@AllArgsConstructor
public class AdminVoteService {
    private final VoteRepository voteRepository;
    private final VoteOptionRepository voteOptionRepository;
    private final VoteResultRepository voteResultRepository;

    private final VoteMapper voteMapper;
    private final SecurityUtils securityUtils;

    // 1. 투표 생성 (커밋 완료)
    public VoteDetailResponseDto createNewVote(CreateNewVoteRequestDto dto) {
        // 엔티티 객체로 변환
        Vote vote = Vote.builder()
                .title(dto.getTitle())
                .isMultiVote(dto.getIsMultiple())
                .voteStatus(VoteStatus.IN_PROGRESS)
                .deadline(dto.getDeadline())
                .build();

        // 옵션 추가
        dto.getOptions().stream()
                .map(optionDto -> VoteOption.builder()
                        .content(optionDto.getContent())
                        .build())
                .forEach(vote::addOption);


        // 저장
        voteRepository.save(vote);
        return voteMapper.toDetailDto(vote);
    }


    // 2. 투표 수정 (구현 완료)
    public VoteDetailResponseDto updateVote(UpdateVoteRequestDto updateVoteRequestDto, Long voteId) {
        Vote vote = voteRepository.findById(voteId).orElseThrow(() -> new EntityNotFoundException());
        vote.update(updateVoteRequestDto);



        return voteMapper.toDetailDto(vote);
    }

    // 3. 옵션 추가 (커밋 완료)
    public VoteDetailResponseDto addVoteOption(AddVoteOptionRequestDto addVoteOptionRequestDto, Long voteId) {
        Vote vote = voteRepository.findById(voteId).orElseThrow(() -> new EntityNotFoundException());
        vote.addOption(
                VoteOption.builder()
                        .content(addVoteOptionRequestDto.getContent())
                        .build()
        );
        voteRepository.saveAndFlush(vote);

        return voteMapper.toDetailDto(vote);
    }


    // 4. 옵션 삭제 (구현완료)
    public void deleteVoteOption(Long voteOptionId, Long optionId) {
        voteOptionRepository.deleteById(voteOptionId);
    }

    // 5. 투표  삭제 (구현 완료)
    public void deleteVote(Long voteId) {
        voteRepository.deleteById(voteId);
    }

    // 5. 투표 종료 (구현 완료)
    public VoteDetailResponseDto closeVote(Long voteId) {
        Vote vote = voteRepository.findById(voteId)
                .orElseThrow(() -> new EntityNotFoundException("해당 투표를 찾을 수 없습니다."));

        // 2. 상태 변경 (Dirty Checking으로 반영됨)
        vote.close();

        // 3. 변수 선언 없이 즉시 리턴
        return voteMapper.toDetailDto(vote);
    }


}
