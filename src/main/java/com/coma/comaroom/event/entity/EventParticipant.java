package com.coma.comaroom.event.entity;

import com.coma.comaroom.member.entity.Member;
import com.coma.comaroom.utils.BaseEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import lombok.*;

@Entity
@Table(name = "event_participant")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class EventParticipant extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long eventParticipantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id",  nullable = false)
    private Event event;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_member_id", nullable = false)
    private Member participantMember;
}
