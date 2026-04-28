package com.coma.comaroom.member.entity;

import com.coma.comaroom.event.entity.*;
import com.coma.comaroom.notice.entity.Notice;
import com.coma.comaroom.study.entity.Study;
import com.coma.comaroom.utils.BaseEntity;
import com.coma.comaroom.vote.entity.VoteResult;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "member")
@SQLRestriction("status = 'ACTIVE'")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class Member extends BaseEntity {
    @Id
    @Column(name = "member_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId;

    @Column(name = "student_id", nullable = false, unique = true)
    private String studentId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "xp", nullable = false)
    private Long xp;

    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "major", nullable = false)
    @Enumerated(EnumType.STRING)
    private Major major;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private MemberStatus status = MemberStatus.ACTIVE;

    @OneToMany(mappedBy = "author", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Notice> notices = new ArrayList<>();


//    // Member 입장에서 "승인한 ActivityApproval 목록"
//    @OneToMany(mappedBy = "approver", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
//    private List<ActivityApproval> approvedActivities = new ArrayList<>();
//
//    // Member 입장에서 "참여한 ActivityApproval 목록"
//    @OneToMany(mappedBy = "participantMember", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<ActivityApproval> participatedActivities = new ArrayList<>();

    @OneToMany(mappedBy = "voter", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<VoteResult> voteResults = new ArrayList<>();

    @OneToMany(mappedBy = "host", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Event> events = new ArrayList<>();

    @OneToMany(mappedBy = "author", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<EventPost> eventPosts = new ArrayList<>();

    @OneToMany(mappedBy = "requester", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<EventApproval> eventApprovals = new ArrayList<>();

    @OneToMany(mappedBy = "participantMember", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<EventParticipant> eventParticipants = new ArrayList<>();

    @OneToMany(mappedBy = "studyManager", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Study> studyManagers = new ArrayList<>();

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
