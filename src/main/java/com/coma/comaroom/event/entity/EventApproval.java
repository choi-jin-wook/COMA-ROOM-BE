package com.coma.comaroom.event.entity;

import com.coma.comaroom.member.entity.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class EventApproval {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "approval_id")
    private Integer id;

    @Column(name = "qpproval_status")
    private ApprovalStatus approvalStatus;

    @Column(name = "approval_at")
    private Date approvalAt;

    @Column(name = "granted_xp")
    private Long grantedXp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id")
    private Member requester;
}
