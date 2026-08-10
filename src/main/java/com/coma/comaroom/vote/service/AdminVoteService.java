package com.coma.comaroom.vote.service;

import com.coma.comaroom.utils.SecurityUtils;
import com.coma.comaroom.vote.dto.AddVoteOptionRequestDto;
import com.coma.comaroom.vote.dto.request.CreateNewVoteRequestDto;
import com.coma.comaroom.vote.dto.request.UpdateVoteRequestDto;
import com.coma.comaroom.vote.dto.response.VoteDetailResponseDto;
import com.coma.comaroom.vote.entity.Vote;
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

    private final SecurityUtils securityUtils;

    // 1. 투표 생성 (커밋 완료)
    public VoteDetailResponseDto createNewVote(CreateNewVoteRequestDto dto) {
        // 엔티티 객체로 변환 (옵션 포함)
        Vote vote = dto.toEntity();

        // 저장
        voteRepository.save(vote);
        return VoteDetailResponseDto.from(vote);
    }


    // 2. 투표 수정 (구현 완료)
    public VoteDetailResponseDto updateVote(UpdateVoteRequestDto updateVoteRequestDto, Long voteId) {
        Vote vote = voteRepository.findById(voteId).orElseThrow(() -> new EntityNotFoundException());
        vote.update(updateVoteRequestDto);



        return VoteDetailResponseDto.from(vote);
    }

    // 3. 옵션 추가 (커밋 완료)
    public VoteDetailResponseDto addVoteOption(AddVoteOptionRequestDto addVoteOptionRequestDto, Long voteId) {
        Vote vote = voteRepository.findById(voteId).orElseThrow(() -> new EntityNotFoundException());
        vote.addOption(addVoteOptionRequestDto.toEntity());
        voteRepository.saveAndFlush(vote);

        return VoteDetailResponseDto.from(vote);
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
        return VoteDetailResponseDto.from(vote);
    }


}
