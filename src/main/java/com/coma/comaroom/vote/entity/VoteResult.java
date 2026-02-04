package com.coma.comaroom.vote.entity;

import com.coma.comaroom.member.entity.Member;
import com.coma.comaroom.utils.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "vote_result",
        uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_voter_option", // 제약 조건 이름
                columnNames = {"voter_id", "vote_option_id"} // 중복을 막을 컬럼 조합
            )
        }
)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class VoteResult extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long voteResultId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voter_id", nullable = false)
    private Member voter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vote_option_id", nullable = false)
    private VoteOption voteOption;
}
