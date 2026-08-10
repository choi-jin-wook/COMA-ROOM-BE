package com.coma.comaroom.event.service;

import com.coma.comaroom.BusinessException;
import com.coma.comaroom.event.EventPostError;
import com.coma.comaroom.event.dto.request.EventPostRequest;
import com.coma.comaroom.event.dto.request.EventPostStatusRequest;
import com.coma.comaroom.event.dto.response.EventPostResponse;
import com.coma.comaroom.event.entity.EventPost;
import com.coma.comaroom.event.repository.EventPostRepository;
import com.coma.comaroom.member.entity.Member;
import com.coma.comaroom.utils.SecurityUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@AllArgsConstructor
public class AdminEventPostService {
    private EventPostRepository eventPostRepository;
    private SecurityUtils securityUtils;

    // 관리자 권한 확인은 SecurityConfig의 "/api/admin/**" hasRole("ADMIN")에서 처리한다.
    public EventPostResponse updatePostStatus(Integer postId, EventPostStatusRequest request) {
        // 1. 게시글 조회
        EventPost post = eventPostRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(EventPostError.POST_NOT_FOUND));

        // 2. 상태 업데이트 (도메인 메서드 호출)
        post.updateStatus(request.approvalStatus());

        return EventPostResponse.from(post);
    }


    // DELETE
    public void deletePost(Integer postId) {
        Member currentMember = securityUtils.getCurrentMember();
        EventPost post = eventPostRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(EventPostError.POST_NOT_FOUND));

        validateAuthor(post, currentMember);
        eventPostRepository.delete(post);
    }

    public EventPostResponse updatePost(Integer postId, EventPostRequest request) {
        Member currentMember = securityUtils.getCurrentMember();
        EventPost post = eventPostRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(EventPostError.POST_NOT_FOUND));

        // 권한 확인 (작성자만 수정 가능)
        validateAuthor(post, currentMember);

        post.update(request);

        return EventPostResponse.from(post);
    }


    private void validateAuthor(EventPost post, Member member) {
        if (!post.getAuthor().getMemberId().equals(member.getMemberId())) {
            throw new BusinessException(EventPostError.UNAUTHORIZED_ACCESS);
        }
    }
}
