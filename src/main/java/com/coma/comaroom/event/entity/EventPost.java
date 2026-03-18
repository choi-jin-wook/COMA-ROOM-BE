package com.coma.comaroom.event.entity;


import com.coma.comaroom.BusinessException;
import com.coma.comaroom.event.EventPostError;
import com.coma.comaroom.member.entity.Member;
import com.coma.comaroom.utils.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "event_photo_post")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class EventPost extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer postId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "approval_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private ApprovalStatus approvalStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private Member author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event", nullable = false)
    private Event event;

    @OneToMany(mappedBy = "eventPost", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<EventPhoto> eventPhotos = new ArrayList<>();


    public void updatePhotos(List<String> newPhotoUrls) {
        this.eventPhotos.clear(); // 기존 사진 연결 해제 (orphanRemoval로 인해 DB에서도 삭제됨)
        if (newPhotoUrls != null) {
            newPhotoUrls.forEach(url -> {
                this.eventPhotos.add(EventPhoto.builder()
                        .photoUrl(url)
                        .eventPost(this)
                        .build());
            });
        }
    }

    public void updateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new BusinessException(EventPostError.INVALID_TITLE);
        }
        this.title = title;
    }


    // 승인 상태 변경 도메인 메서드
    public void updateStatus(ApprovalStatus status) {
        if (status == null) {
            throw new BusinessException(EventPostError.INVALID_STATUS); // 에러 코드 추가 필요
        }
        this.approvalStatus = status;
    }
}
