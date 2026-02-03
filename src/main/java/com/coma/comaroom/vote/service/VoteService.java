package com.coma.comaroom.vote.service;

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
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    // 1. 투표 생성 (커밋 완료)
    public VoteDetailResponseDto createNewVote(CreateNewVoteRequestDto dto) {

        Vote vote = Vote.builder()
                .title(dto.getTitle())
                .isMultiVote(dto.getIsMultiple())
                .voteStatus(VoteStatus.IN_PROGRESS)
                .deadline(dto.getDeadline())
                .build();

        for (CreateVoteOptionRequestDto optionDto : dto.getOptions()) {
            vote.addOption(
                    VoteOption.builder()
                            .content(optionDto.getContent())
                            .build()
            );
        }

        voteRepository.save(vote);

        List<VoteOptionDetailResponseDto> optionResponses = vote.getVoteOptions().stream()
                        .map(o -> VoteOptionDetailResponseDto.builder()
                                .voteOptionId(o.getVoteOptionId())
                                .content(o.getContent())
                                .build())
                        .toList();

        return VoteDetailResponseDto.builder()
                .voteId(vote.getVoteId())
                .title(vote.getTitle())
                .isMultiple(vote.isMultiVote())
                .status(vote.getVoteStatus())
                .options(optionResponses)
                .build();
    }


    // 2. 투표 수정 (구현 완료)
    public VoteDetailResponseDto updateVote(UpdateVoteRequestDto updateVoteRequestDto) {
        Vote vote = voteRepository.findById(updateVoteRequestDto.getVoteId()).orElseThrow(() -> new EntityNotFoundException());
        vote.update(updateVoteRequestDto);

        List<VoteOptionDetailResponseDto> voteOptionDetailResponseDtos =
                vote.getVoteOptions().stream()
                        .map(voteOption ->
                                VoteOptionDetailResponseDto.builder()
                                        .voteOptionId(voteOption.getVoteOptionId())
                                        .content(voteOption.getContent())
                                        .build()
                        )
                        .collect(Collectors.toList());


        VoteDetailResponseDto voteDetailResponseDto = VoteDetailResponseDto.builder()
                .voteId(vote.getVoteId())
                .title(vote.getTitle())
                .isMultiple(vote.isMultiVote())
                .status(vote.getVoteStatus())
                .options(voteOptionDetailResponseDtos)
                .build();

        return voteDetailResponseDto;


    }

    // 3. 옵션 추가 (커밋 완료)
    public VoteDetailResponseDto addVoteOption(AddVoteOptionRequestDto addVoteOptionRequestDto) {
        Vote vote = voteRepository.findById(addVoteOptionRequestDto.getVoteId())
                .orElseThrow(EntityNotFoundException::new);

        vote.addOption(
                VoteOption.builder()
                        .content(addVoteOptionRequestDto.getContent())
                        .build()
        );


        List<VoteOption> options = voteOptionRepository.findByVote(vote);


        List<VoteOptionDetailResponseDto> voteOptionDetailResponseDtos =
                options.stream()
                        .map(voteOption ->
                                VoteOptionDetailResponseDto.builder()
                                        .voteOptionId(voteOption.getVoteOptionId())
                                        .content(voteOption.getContent())
                                        .build()
                        )
                        .collect(Collectors.toList());


        VoteDetailResponseDto voteDetailResponseDto = VoteDetailResponseDto.builder()
                .voteId(vote.getVoteId())
                .title(vote.getTitle())
                .isMultiple(vote.isMultiVote())
                .status(vote.getVoteStatus())
                .options(voteOptionDetailResponseDtos)
                .build();

        return voteDetailResponseDto;
    }


    // 4. 옵션 삭제 (구현완료)
    public void deleteVoteOption(Long voteOptionId) {
        voteOptionRepository.deleteById(voteOptionId);
    }

    // 5. 투표  삭제 (구현 완료)
    public void deleteVote(Long voteId) {
        voteRepository.deleteById(voteId);
    }

    // 5. 투표 종료 (구현 완료)
    public VoteDetailResponseDto closeVote(Long voteId) {
        Vote vote = voteRepository.findById(voteId).orElse(null);
        vote.setVoteStatus(VoteStatus.CLOSED);

        List<VoteOption> voteOptions = vote.getVoteOptions();
        List<VoteOptionDetailResponseDto> voteOptionDetailResponseDtos =
                vote.getVoteOptions().stream()
                        .map(voteOption ->
                                VoteOptionDetailResponseDto.builder()
                                        .voteOptionId(voteOption.getVoteOptionId())
                                        .content(voteOption.getContent())
                                        .build()
                        )
                        .toList();


        VoteDetailResponseDto voteDetailResponseDto = VoteDetailResponseDto.builder()
                .voteId(vote.getVoteId())
                .title(vote.getTitle())
                .isMultiple(vote.isMultiVote())
                .status(vote.getVoteStatus())
                .options(voteOptionDetailResponseDtos)
                .build();

        return voteDetailResponseDto;
    }
}
