package com.coma.comaroom.event.dto.response;

import com.coma.comaroom.event.entity.EventPhoto;
import com.coma.comaroom.event.entity.EventPost;

import java.time.LocalDateTime;
import java.util.List;

public record EventPostResponse(
        Integer postId,
        String title,
        String authorNickname,
        String approvalStatus,
        List<String> photoUrls,
        LocalDateTime createdAt
) {
    public static EventPostResponse from(EventPost post) {
        return new EventPostResponse(
                post.getPostId(),
                post.getTitle(),
                post.getAuthor() != null ? post.getAuthor().getName() : "탈퇴한 회원",
                post.getApprovalStatus().name(),
                post.getEventPhotos().stream()
                        .map(EventPhoto::getPhotoUrl)
                        .toList(),
                post.getCreatedAt()
        );
    }
}