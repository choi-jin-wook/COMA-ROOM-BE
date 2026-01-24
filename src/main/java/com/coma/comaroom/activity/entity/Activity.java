package com.coma.comaroom.activity.entity;

import com.coma.comaroom.utils.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "activity")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class Activity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "activity_id",  nullable = false)
    private Long ActivityId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "activity_type",  nullable = false)
    @Enumerated(EnumType.STRING)
    private ActivityType activityType;

    @Column(name = "activity_date", nullable = false)
    private LocalDateTime activityDate;

    @OneToMany(mappedBy = "activity", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ActivityParticipant> participants = new ArrayList<>();
}
