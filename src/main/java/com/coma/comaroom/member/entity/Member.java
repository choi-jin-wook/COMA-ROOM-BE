package com.coma.comaroom.member.entity;

import com.coma.comaroom.activity.entity.ActivityApproval;
import com.coma.comaroom.event.entity.Event;
import com.coma.comaroom.event.entity.EventApproval;
import com.coma.comaroom.event.entity.EventPost;
import com.coma.comaroom.notice.entity.Notice;
import com.coma.comaroom.utils.BaseEntity;
import com.coma.comaroom.vote.entity.VoteResult;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "member")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class Member extends BaseEntity {
    @Id
    @Column(name = "member_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId;

    @Column(name = "student_id", nullable = false)
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

    @OneToMany(mappedBy = "author",  fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Notice> notices = new ArrayList<>();


    // Member 입장에서 “승인한 ActivityApproval 목록”
    @OneToMany(mappedBy = "approver", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ActivityApproval> approvedActivities = new ArrayList<>();

    // Member 입장에서 “참여한 ActivityApproval 목록”
    @OneToMany(mappedBy = "participantMember", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ActivityApproval> participatedActivities = new ArrayList<>();

    @OneToMany(mappedBy = "voter", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<VoteResult> voteResults = new ArrayList<>();

    @OneToMany(mappedBy = "host", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Event> events = new ArrayList<>();

    @OneToMany(mappedBy = "author", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<EventPost> eventPosts = new ArrayList<>();

    @OneToMany(mappedBy = "requester", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<EventApproval> eventApprovals = new ArrayList<>();
}