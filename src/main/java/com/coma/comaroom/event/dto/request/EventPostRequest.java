package com.coma.comaroom.event.dto.request;

import com.coma.comaroom.event.entity.ApprovalStatus;
import com.coma.comaroom.event.entity.Event;
import com.coma.comaroom.event.entity.EventPhoto;
import com.coma.comaroom.event.entity.EventPost;
import com.coma.comaroom.member.entity.Member;
import com.coma.comaroom.member.entity.Role;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public record EventPostRequest(
            String title,
            Long eventId,
            List<String> photoUrls
    ) {
    public EventPost toEntity(Member author, Event event) {
        ApprovalStatus status = (author.getRole() == Role.ADMIN)
                ? ApprovalStatus.APPROVED
                : ApprovalStatus.PENDING;

        EventPost post = EventPost.builder()
                .title(title)
                .author(author)
                .event(event)
                .approvalStatus(status)
                .eventPhotos(new ArrayList<>())
                .build();

        List<EventPhoto> photos = Optional.ofNullable(photoUrls)
                .orElseGet(Collections::emptyList)
                .stream()
                .map(url -> EventPhoto.builder()
                        .photoUrl(url)
                        .eventPost(post)
                        .build())
                .toList();

        post.getEventPhotos().addAll(photos);

        return post;
    }
}

