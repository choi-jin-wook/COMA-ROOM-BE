package com.coma.comaroom.event.entity;

import com.coma.comaroom.member.entity.Member;
import com.coma.comaroom.utils.BaseEntity;
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
public class EventApproval extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "approval_id")
    private Long id;

    @Column(name = "approval_status")
    @Enumerated(EnumType.STRING)
    private ApprovalStatus approvalStatus;

    @Column(name = "approval_at")
    private Date approvalAt;

    @Column(name = "granted_xp")
    private Long grantedXp;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "event_id")
//    private Event event;
    @Column(name = "reason")
    private String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id")
    private Member requester;

    public static EventApproval requestXpApproval(Member requester, String reason, Long grantedXp) {
        return EventApproval.builder()
                .requester(requester)
                .reason(reason)
                .grantedXp(grantedXp)
                .approvalStatus(ApprovalStatus.PENDING)
                .approvalAt(null)
                .build();
    }
}
