package com.coma.comaroom.event.mapper;

import com.coma.comaroom.event.dto.request.EventPostRequest;
import com.coma.comaroom.event.entity.ApprovalStatus;
import com.coma.comaroom.event.entity.Event;
import com.coma.comaroom.event.entity.EventPhoto;
import com.coma.comaroom.event.entity.EventPost;
import com.coma.comaroom.member.entity.Member;
import com.coma.comaroom.member.entity.Role;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
public class EventPostMapper {
    public EventPost toEntity(EventPostRequest request, Member author, Event event) {
        // 1. 권한에 따른 승인 상태 결정
        ApprovalStatus status = (author.getRole() == Role.ADMIN)
                ? ApprovalStatus.APPROVED
                : ApprovalStatus.PENDING;

        // 2. EventPost 먼저 생성
        EventPost post = EventPost.builder()
                .title(request.title())
                .author(author)
                .event(event)
                .approvalStatus(status)
                .eventPhotos(new ArrayList<>())
                .build();

        // 3. 스트림으로 사진 엔티티 리스트 생성 및 추가
        List<EventPhoto> photos = Optional.ofNullable(request.photoUrls())
                .orElseGet(Collections::emptyList) // null이면 빈 리스트 반환
                .stream()
                .map(url -> EventPhoto.builder()
                        .photoUrl(url)
                        .eventPost(post) // 여기서 부모(post)를 주입
                        .build())
                .toList();

        post.getEventPhotos().addAll(photos); // 생성된 리스트를 통째로 추가

        return post;
    }
}
