package com.coma.comaroom.event.service;

import com.coma.comaroom.BusinessException;
import com.coma.comaroom.event.EventError;
import com.coma.comaroom.event.EventPostError;
import com.coma.comaroom.event.dto.request.EventPostRequest;
import com.coma.comaroom.event.dto.request.EventPostStatusRequest;
import com.coma.comaroom.event.dto.response.EventPostResponse;
import com.coma.comaroom.event.entity.ApprovalStatus;
import com.coma.comaroom.event.entity.Event;
import com.coma.comaroom.event.entity.EventPost;
import com.coma.comaroom.event.repository.EventPostRepository;
import com.coma.comaroom.event.repository.EventRepository;
import com.coma.comaroom.member.entity.Member;
import com.coma.comaroom.member.entity.Role;
import com.coma.comaroom.utils.SecurityUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@AllArgsConstructor
public class EventPostService {
    private final EventPostRepository eventPostRepository;
    private final EventRepository eventRepository;
    private final SecurityUtils securityUtils;

    // CREATE
    public EventPostResponse createPost(EventPostRequest request) {
        Member currentMember = securityUtils.getCurrentMember();
        Event event = eventRepository.findById(request.eventId())
                .orElseThrow(() -> new BusinessException(EventError.EVENT_NOT_FOUND));

        EventPost post = request.toEntity(currentMember, event);
        EventPost savedPost = eventPostRepository.save(post);
        return EventPostResponse.from(savedPost);
    }

    // READ (단건)
    public EventPostResponse getPost(Integer postId) {
        return eventPostRepository.findById(postId)
                .map(EventPostResponse::from)
                .orElseThrow(() -> new BusinessException(EventPostError.POST_NOT_FOUND));
    }

    // READ (전체 목록 - 페이징 처리 권장)
    public List<EventPostResponse> getAllPosts() {
        return eventPostRepository.findAll().stream()
                .map(EventPostResponse::from)
                .toList();
    }



    private void validateAuthor(EventPost post, Member member) {
        if (!post.getAuthor().getMemberId().equals(member.getMemberId())) {
            throw new BusinessException(EventPostError.UNAUTHORIZED_ACCESS);
        }
    }
}
