package com.coma.comaroom.vote.service;

import com.coma.comaroom.vote.dto.request.CreateNewVoteRequestDto;
import com.coma.comaroom.vote.dto.request.CreateVoteOptionRequestDto;
import com.coma.comaroom.vote.dto.response.CreateNewVoteResponseDto;
import com.coma.comaroom.vote.dto.response.CreateVoteOptionResponseDto;
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
    public CreateNewVoteResponseDto createNewVote(CreateNewVoteRequestDto createNewVoteRequestDto) {
        Vote newVote = Vote.builder()
                .title(createNewVoteRequestDto.getTitle())
                .isMultiVote(createNewVoteRequestDto.getIsMulti())
                .voteStatus(VoteStatus.IN_PROGRESS)
                .build();

        voteRepository.save(newVote);

        List<VoteOption> options = new ArrayList<>();

        List<CreateVoteOptionResponseDto> createVoteOptionResponseDtos = new ArrayList<>();

        for (CreateVoteOptionRequestDto createVoteOptionRequest : createNewVoteRequestDto.getOptions()) {
            VoteOption voteOption = VoteOption.builder()
                    .content(createVoteOptionRequest.getContent())
                    .vote(newVote)
                    .build();

            voteOptionRepository.save(voteOption);

            CreateVoteOptionResponseDto createVoteOptionResponseDto = CreateVoteOptionResponseDto.builder()
                    .voteOptionId(voteOption.getVoteOptionId())
                    .content(createVoteOptionRequest.getContent())
                    .build();

            createVoteOptionResponseDtos.add(createVoteOptionResponseDto);
        }

        CreateNewVoteResponseDto createNewVoteResponseDto = CreateNewVoteResponseDto.builder()
                .voteId(newVote.getVoteId())
                .title(newVote.getTitle())
                .isMultiple(newVote.isMultiVote())
                .options(createVoteOptionResponseDtos)
                .status(newVote.getVoteStatus())
                .build();

        return createNewVoteResponseDto;
    }

    // 2. 투표 수정

    // 3. 옵션 추가


    // 4. 투표 삭제
    // 5. 투표 종료
}
