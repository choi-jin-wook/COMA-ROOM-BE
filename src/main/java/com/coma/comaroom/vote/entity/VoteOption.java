package com.coma.comaroom.vote.entity;

import com.coma.comaroom.utils.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "vote_option")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class VoteOption extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long voteOptionId;

    @Column(name = "content", nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vote_id",  nullable = false)
    private Vote vote;

    @OneToMany(mappedBy = "voteOption", cascade = CascadeType.ALL)
    private List<VoteResult> voteResults = new ArrayList<>();
}
