package com.coma.comaroom.vote.entity;

import com.coma.comaroom.utils.BaseEntity;
import com.coma.comaroom.vote.dto.request.UpdateVoteRequestDto;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "vote"
//    uniqueConstraints = {
//        @UniqueConstraint(
//            name = "uk_voter_option", // 제약 조건 이름
//            columnNames = {"voter_id", "vote_option_id"} // 중복을 막을 컬럼 조합
//        )
//    }
)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Vote extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long voteId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "is_multi_vote", nullable = false)
    private boolean isMultiVote; // true: 중복 선택 가능, false: 1인 1표

    @Enumerated(EnumType.STRING)
    @Column(name = "vote_status", nullable = false)
    private VoteStatus voteStatus; // PROGRESS, CLOSED

    @Column(name = "deadline", nullable = false)
    private LocalDateTime deadline;


    @OneToMany(mappedBy = "vote", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VoteOption> voteOptions = new ArrayList<>();

    public void addOption(VoteOption option) {
        voteOptions.add(option);
        option.setVote(this);
    }
    public void update(UpdateVoteRequestDto dto) {
        if (dto.getTitle() != null) this.title = dto.getTitle();
        if (dto.getIsMultiple() != null) this.isMultiVote = dto.getIsMultiple();
        if (dto.getDeadline() != null) this.deadline = dto.getDeadline();
    }

}
