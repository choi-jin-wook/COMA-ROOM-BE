package com.coma.comaroom.event.service;

import com.coma.comaroom.BusinessException;
import com.coma.comaroom.event.EventError;
import com.coma.comaroom.event.EventPostError;
import com.coma.comaroom.event.dto.request.EventPostRequest;
import com.coma.comaroom.event.dto.response.EventPostResponse;
import com.coma.comaroom.event.entity.ApprovalStatus;
import com.coma.comaroom.event.entity.Event;
import com.coma.comaroom.event.entity.EventCategory;
import com.coma.comaroom.event.entity.EventPost;
import com.coma.comaroom.event.repository.EventPostRepository;
import com.coma.comaroom.event.repository.EventRepository;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventPostServiceTest {

    @Mock private EventPostRepository eventPostRepository;
    @Mock private EventRepository eventRepository;
    @Mock private SecurityUtils securityUtils;

    @InjectMocks
    private EventPostService eventPostService;

    private Member member;
    private Event event;
    private EventPost eventPost;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .memberId(1L)
                .studentId("20210001")
                .name("작성자")
                .password("encoded")
                .xp(0L)
                .role(Role.USER)
                .major(Major.COMPUTER_INFO)
                .build();

        event = Event.builder()
                .eventId(1L)
                .title("정기 모임")
                .eventDate(java.time.LocalDateTime.now().plusDays(1))
                .rewardXp(3L)
                .location("강의실 A")
                .eventCategory(EventCategory.REGULAR_MEETING)
                .host(member)
                .build();

        eventPost = EventPost.builder()
                .postId(1)
                .title("후기 게시글")
                .approvalStatus(ApprovalStatus.PENDING)
                .author(member)
                .event(event)
                .eventPhotos(new ArrayList<>())
                .build();
    }

    // ─────────────────────────────────────────────
    // createPost
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("게시글 생성 성공")
    void createPost_success() {
        EventPostRequest request = new EventPostRequest("후기 게시글", 1L, List.of());
        when(securityUtils.getCurrentMember()).thenReturn(member);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(eventPostRepository.save(any(EventPost.class))).thenReturn(eventPost);

        EventPostResponse result = eventPostService.createPost(request);

        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo("후기 게시글");
        verify(eventPostRepository).save(any(EventPost.class));
    }

    @Test
    @DisplayName("게시글 생성 실패 - 이벤트 없음")
    void createPost_eventNotFound() {
        EventPostRequest request = new EventPostRequest("후기 게시글", 99L, List.of());
        when(securityUtils.getCurrentMember()).thenReturn(member);
        when(eventRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventPostService.createPost(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage(EventError.EVENT_NOT_FOUND.getMessage());
    }

    // ─────────────────────────────────────────────
    // getPost
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("게시글 단건 조회 성공")
    void getPost_success() {
        when(eventPostRepository.findById(1)).thenReturn(Optional.of(eventPost));

        EventPostResponse result = eventPostService.getPost(1);

        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo("후기 게시글");
    }

    @Test
    @DisplayName("게시글 단건 조회 실패 - 게시글 없음")
    void getPost_notFound() {
        when(eventPostRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventPostService.getPost(99))
                .isInstanceOf(BusinessException.class)
                .hasMessage(EventPostError.POST_NOT_FOUND.getMessage());
    }

    // ─────────────────────────────────────────────
    // getAllPosts
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("게시글 전체 조회 성공")
    void getAllPosts_success() {
        Pageable pageable = PageRequest.of(0, 10);
        when(eventPostRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(eventPost)));

        Page<EventPostResponse> result = eventPostService.getAllPosts(pageable);

        assertThat(result).hasSize(1);
        assertThat(result.getContent().get(0).title()).isEqualTo("후기 게시글");
    }

    @Test
    @DisplayName("게시글 전체 조회 - 게시글 없음")
    void getAllPosts_empty() {
        Pageable pageable = PageRequest.of(0, 10);
        when(eventPostRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of()));

        Page<EventPostResponse> result = eventPostService.getAllPosts(pageable);

        assertThat(result).isEmpty();
    }
}
