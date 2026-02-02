package com.coma.comaroom.vote.service;

import com.coma.comaroom.vote.dto.AddVoteOptionRequestDto;
import com.coma.comaroom.vote.dto.request.CreateNewVoteRequestDto;
import com.coma.comaroom.vote.dto.request.CreateVoteOptionRequestDto;
import com.coma.comaroom.vote.dto.response.VoteDetailResponseDto;
import com.coma.comaroom.vote.dto.response.VoteOptionDetailResponseDto;
import com.coma.comaroom.vote.entity.Vote;
import com.coma.comaroom.vote.entity.VoteOption;
import com.coma.comaroom.vote.entity.VoteStatus;
import com.coma.comaroom.vote.repository.VoteOptionRepository;
import com.coma.comaroom.vote.repository.VoteRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Transactional
public class VoteService {
    private final VoteRepository voteRepository;
    private final VoteOptionRepository voteOptionRepository;
    // - 사용자
    // 1. 전체 투표 조회
    // 2. 투표 상세 조회
    // 3. 진행중인 투표 목록
    // 4. 종료된 투표 목록
    // 5. 투표 참여


    // - 관리자
    // 1. 투표 생성
    public VoteDetailResponseDto createNewVote(CreateNewVoteRequestDto createNewVoteRequestDto) {
        Vote newVote = Vote.builder()
                .title(createNewVoteRequestDto.getTitle())
                .isMultiVote(createNewVoteRequestDto.getIsMulti())
                .voteStatus(VoteStatus.IN_PROGRESS)
                .build();

        voteRepository.save(newVote);

        List<VoteOption> options = new ArrayList<>();

        List<VoteOptionDetailResponseDto> voteOptionDetailResponseDtos = new ArrayList<>();

        for (CreateVoteOptionRequestDto createVoteOptionRequest : createNewVoteRequestDto.getOptions()) {
            VoteOption voteOption = VoteOption.builder()
                    .content(createVoteOptionRequest.getContent())
                    .vote(newVote)
                    .build();

            voteOptionRepository.save(voteOption);

            VoteOptionDetailResponseDto createVoteOptionResponseDto = VoteOptionDetailResponseDto.builder()
                    .voteOptionId(voteOption.getVoteOptionId())
                    .content(createVoteOptionRequest.getContent())
                    .build();

            voteOptionDetailResponseDtos.add(createVoteOptionResponseDto);
        }

        VoteDetailResponseDto createNewVoteResponseDto = VoteDetailResponseDto.builder()
                .voteId(newVote.getVoteId())
                .title(newVote.getTitle())
                .isMultiple(newVote.isMultiVote())
                .options(voteOptionDetailResponseDtos)
                .status(newVote.getVoteStatus())
                .build();

        return createNewVoteResponseDto;
    }



    // 2. 투표 수정

    // 3. 옵션 추가
    public VoteDetailResponseDto addVoteOption(AddVoteOptionRequestDto addVoteOptionRequestDto) {
        Vote vote = voteRepository.findById(addVoteOptionRequestDto.getVoteId()).orElse(null);

        VoteOption newVoteOption = VoteOption.builder()
                .content(addVoteOptionRequestDto.getContent())
                .vote(vote)
                .build();

        voteOptionRepository.save(newVoteOption);

        List<VoteOption> options = voteOptionRepository.findByVote(vote);

        List<VoteOptionDetailResponseDto>  voteOptionDetailResponseDtos = new ArrayList<>();

        for (VoteOption voteOption : options) {
            VoteOptionDetailResponseDto voteOptionDetailResponseDto = VoteOptionDetailResponseDto.builder()
                    .voteOptionId(voteOption.getVoteOptionId())
                    .content(addVoteOptionRequestDto.getContent())
                    .build();

            voteOptionDetailResponseDtos.add(voteOptionDetailResponseDto);
        }

        VoteDetailResponseDto voteDetailResponseDto = VoteDetailResponseDto.builder()
                .voteId(vote.getVoteId())
                .title(vote.getTitle())
                .isMultiple(vote.isMultiVote())
                .status(vote.getVoteStatus())
                .options(voteOptionDetailResponseDtos)
                .build();

        return voteDetailResponseDto;
    }

    // 4. 투표 삭제
    // 5. 투표 종료
}
