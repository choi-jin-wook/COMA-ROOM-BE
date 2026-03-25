package com.coma.comaroom.event.service;

import com.coma.comaroom.BusinessException;
import com.coma.comaroom.event.EventPostError;
import com.coma.comaroom.event.dto.request.EventPostRequest;
import com.coma.comaroom.event.dto.request.EventPostStatusRequest;
import com.coma.comaroom.event.dto.response.EventPostResponse;
import com.coma.comaroom.event.entity.ApprovalStatus;
import com.coma.comaroom.event.entity.Event;
import com.coma.comaroom.event.entity.EventCategory;
import com.coma.comaroom.event.entity.EventPost;
import com.coma.comaroom.event.repository.EventPostRepository;
import com.coma.comaroom.member.AuthError;
import com.coma.comaroom.member.entity.Major;
import com.coma.comaroom.member.entity.Member;
import com.coma.comaroom.member.entity.Role;
import com.coma.comaroom.utils.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminEventPostServiceTest {

    @Mock private EventPostRepository eventPostRepository;
    @Mock private SecurityUtils securityUtils;

    @InjectMocks
    private AdminEventPostService adminEventPostService;

    private Member adminMember;
    private Member authorMember;
    private Member otherMember;
    private Event event;
    private EventPost eventPost;

    @BeforeEach
    void setUp() {
        adminMember = Member.builder()
                .memberId(1L)
                .studentId("20210001")
                .name("관리자")
                .password("encoded")
                .xp(0L)
                .role(Role.ADMIN)
                .major(Major.COMPUTER_INFO)
                .build();

        authorMember = Member.builder()
                .memberId(2L)
                .studentId("20210002")
                .name("작성자")
                .password("encoded")
                .xp(0L)
                .role(Role.USER)
                .major(Major.COMPUTER_INFO)
                .build();

        otherMember = Member.builder()
                .memberId(3L)
                .studentId("20210003")
                .name("타인")
                .password("encoded")
                .xp(0L)
                .role(Role.USER)
                .major(Major.COMPUTER_INFO)
                .build();

        event = Event.builder()
                .eventId(1L)
                .title("정기 모임")
                .eventDate(LocalDateTime.now().plusDays(1))
                .rewardXp(3L)
                .location("강의실 A")
                .eventCategory(EventCategory.REGULAR_MEETING)
                .host(authorMember)
                .build();

        eventPost = EventPost.builder()
                .postId(1)
                .title("후기 게시글")
                .approvalStatus(ApprovalStatus.PENDING)
                .author(authorMember)
                .event(event)
                .eventPhotos(new ArrayList<>())
                .build();
    }

    // ─────────────────────────────────────────────
    // updatePostStatus
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("게시글 상태 변경 성공 - 관리자 권한")
    void updatePostStatus_success() {
        EventPostStatusRequest request = new EventPostStatusRequest(ApprovalStatus.APPROVED);
        when(securityUtils.getCurrentMember()).thenReturn(adminMember);
        when(eventPostRepository.findById(1)).thenReturn(Optional.of(eventPost));

        EventPostResponse result = adminEventPostService.updatePostStatus(1, request);

        assertThat(result).isNotNull();
        assertThat(result.approvalStatus()).isEqualTo(ApprovalStatus.APPROVED.name());
    }

    @Test
    @DisplayName("게시글 상태 변경 실패 - 관리자 아닌 경우")
    void updatePostStatus_notAdmin() {
        EventPostStatusRequest request = new EventPostStatusRequest(ApprovalStatus.APPROVED);
        when(securityUtils.getCurrentMember()).thenReturn(authorMember);

        assertThatThrownBy(() -> adminEventPostService.updatePostStatus(1, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage(AuthError.NOT_ADMIN.getMessage());
    }

    @Test
    @DisplayName("게시글 상태 변경 실패 - 게시글 없음")
    void updatePostStatus_postNotFound() {
        EventPostStatusRequest request = new EventPostStatusRequest(ApprovalStatus.APPROVED);
        when(securityUtils.getCurrentMember()).thenReturn(adminMember);
        when(eventPostRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminEventPostService.updatePostStatus(99, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage(EventPostError.POST_NOT_FOUND.getMessage());
    }

    // ─────────────────────────────────────────────
    // deletePost
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("게시글 삭제 성공 - 작성자")
    void deletePost_success() {
        when(securityUtils.getCurrentMember()).thenReturn(authorMember);
        when(eventPostRepository.findById(1)).thenReturn(Optional.of(eventPost));

        assertThatNoException().isThrownBy(() -> adminEventPostService.deletePost(1));
        verify(eventPostRepository).delete(eventPost);
    }

    @Test
    @DisplayName("게시글 삭제 실패 - 작성자가 아닌 경우")
    void deletePost_unauthorized() {
        when(securityUtils.getCurrentMember()).thenReturn(otherMember);
        when(eventPostRepository.findById(1)).thenReturn(Optional.of(eventPost));

        assertThatThrownBy(() -> adminEventPostService.deletePost(1))
                .isInstanceOf(BusinessException.class)
                .hasMessage(EventPostError.UNAUTHORIZED_ACCESS.getMessage());
    }

    @Test
    @DisplayName("게시글 삭제 실패 - 게시글 없음")
    void deletePost_notFound() {
        when(securityUtils.getCurrentMember()).thenReturn(authorMember);
        when(eventPostRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminEventPostService.deletePost(99))
                .isInstanceOf(BusinessException.class)
                .hasMessage(EventPostError.POST_NOT_FOUND.getMessage());
    }

    // ─────────────────────────────────────────────
    // updatePost
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("게시글 수정 성공 - 작성자")
    void updatePost_success() {
        EventPostRequest request = new EventPostRequest("수정된 제목", 1L, null);
        when(securityUtils.getCurrentMember()).thenReturn(authorMember);
        when(eventPostRepository.findById(1)).thenReturn(Optional.of(eventPost));

        EventPostResponse result = adminEventPostService.updatePost(1, request);

        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo("수정된 제목");
    }

    @Test
    @DisplayName("게시글 수정 실패 - 작성자가 아닌 경우")
    void updatePost_unauthorized() {
        EventPostRequest request = new EventPostRequest("수정된 제목", 1L, null);
        when(securityUtils.getCurrentMember()).thenReturn(otherMember);
        when(eventPostRepository.findById(1)).thenReturn(Optional.of(eventPost));

        assertThatThrownBy(() -> adminEventPostService.updatePost(1, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage(EventPostError.UNAUTHORIZED_ACCESS.getMessage());
    }

    @Test
    @DisplayName("게시글 수정 실패 - 게시글 없음")
    void updatePost_notFound() {
        EventPostRequest request = new EventPostRequest("수정된 제목", 1L, null);
        when(securityUtils.getCurrentMember()).thenReturn(authorMember);
        when(eventPostRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminEventPostService.updatePost(99, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage(EventPostError.POST_NOT_FOUND.getMessage());
    }
}
