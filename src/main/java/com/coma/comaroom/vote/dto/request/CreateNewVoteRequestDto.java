package com.coma.comaroom.vote.dto.request;

import com.coma.comaroom.vote.entity.Vote;
import com.coma.comaroom.vote.entity.VoteStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateNewVoteRequestDto {
    @NotBlank(message = "투표 제목은 필수 입력 항목입니다.")
    private String title;

    @NotNull(message = "투표 타입(SINGLE, MULTI)을 선택해주세요.")
    private Boolean isMultiple;


    @NotEmpty(message = "최소 하나 이상의 투표 선택지가 필요합니다.")
    private List<CreateVoteOptionRequestDto> options;

    @NotEmpty(message = "마감일은 필수입니다")
    private LocalDateTime deadline;

    public Vote toEntity() {
        Vote vote = Vote.builder()
                .title(title)
                .isMultiVote(isMultiple)
                .voteStatus(VoteStatus.IN_PROGRESS)
                .deadline(deadline)
                .build();

        Optional.ofNullable(options)
                .orElseGet(Collections::emptyList)
                .stream()
                .map(CreateVoteOptionRequestDto::toEntity)
                .forEach(vote::addOption);

        return vote;
    }
}
