package com.coma.comaroom.vote.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateVoteRequestDto {
    private Long voteId;
    private String title;
    private Boolean isMultiple;
    private LocalDateTime deadline;
}
