package com.coma.comaroom.event.entity;


import com.coma.comaroom.BusinessException;
import com.coma.comaroom.event.EventPostError;
import com.coma.comaroom.event.dto.request.EventPostRequest;
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
    private List<EventPhoto> eventPhotos;


    public void update(EventPostRequest request) {
        // 1. 제목 업데이트 및 검증
        if (request.title() != null) {
            if (request.title().isBlank()) {
                throw new BusinessException(EventPostError.INVALID_TITLE);
            }
            this.title = request.title();
        }

        // 2. 사진 리스트 업데이트 (Stream 사용)
        if (request.photoUrls() != null) {
            this.eventPhotos.clear(); // 기존 연관 관계 제거 (orphanRemoval=true 필요)

            // URL 스트림을 엔티티 리스트로 변환하여 추가
            List<EventPhoto> newPhotos = request.photoUrls().stream()
                    .map(url -> EventPhoto.builder()
                            .photoUrl(url)
                            .eventPost(this)
                            .build())
                    .toList();

            this.eventPhotos.addAll(newPhotos);
        }
    }


    // 승인 상태 변경 도메인 메서드
    public void updateStatus(ApprovalStatus status) {
        if (status == null) {
            throw new BusinessException(EventPostError.INVALID_STATUS); // 에러 코드 추가 필요
        }
        this.approvalStatus = status;
    }
}
