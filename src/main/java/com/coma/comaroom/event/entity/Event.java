package com.coma.comaroom.event.entity;

import com.coma.comaroom.event.dto.request.EventRequest;
import com.coma.comaroom.member.entity.Member;
import com.coma.comaroom.utils.BaseEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "event")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class Event extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long eventId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;

    @Column(name = "reward_xp", nullable = false)
    private Long rewardXp;

    @Column(name = "location", nullable = false)
    private String location;

    @Column(name = "event_category", nullable = false)
    @Enumerated(EnumType.STRING)
    private EventCategory eventCategory;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id", nullable = false)
    private Member host;

    @OneToMany(mappedBy = "event", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<EventParticipant> eventParticipants = new ArrayList<>();

    @OneToMany(mappedBy = "event", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<EventPost> eventPosts = new ArrayList<>();

    public void addParticipant(Member member) {
        EventParticipant participant = EventParticipant.builder()
                .event(this)
                .participantMember(member)
                .build();

        eventParticipants.add(participant);
    }

    public void update(EventRequest request) {
        if (request.title() != null) {
            this.title = request.title();
        }
        if (request.eventDate() != null) {
            this.eventDate = request.eventDate();
        }
        if (request.location() != null) {
            this.location = request.location();
        }
        if (request.eventCategory() != null) {
            this.eventCategory = request.eventCategory();
        }

        // rewardXp는 요청값이 있으면 그 값으로, 없으면 카테고리의 기본값으로 업데이트
        if (request.rewardXp() != null) {
            this.rewardXp = request.rewardXp();
        } else if (request.eventCategory() != null) {
            this.rewardXp = request.eventCategory().getDefaultXp();
        }
    }

//    @OneToMany(mappedBy = "event", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
//    private List<EventApproval> eventApprovals = new ArrayList<>();
}
