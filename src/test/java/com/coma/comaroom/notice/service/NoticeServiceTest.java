package com.coma.comaroom.notice.service;

import com.coma.comaroom.BusinessException;
import com.coma.comaroom.member.entity.Major;
import com.coma.comaroom.member.entity.Member;
import com.coma.comaroom.member.entity.Role;
import com.coma.comaroom.notice.dto.request.CreateNoticeRequestDto;
import com.coma.comaroom.notice.dto.request.UpdateNoticeRequestDto;
import com.coma.comaroom.notice.dto.response.CreateNoticeResponseDto;
import com.coma.comaroom.notice.dto.response.GetNoticeResponseDto;
import com.coma.comaroom.notice.dto.response.NoticeResponseDto;
import com.coma.comaroom.notice.dto.response.UpdateNoticeResponseDto;
import com.coma.comaroom.notice.entity.Notice;
import com.coma.comaroom.notice.entity.NoticePriority;
import com.coma.comaroom.notice.exception.NoticeErrorCode;
import com.coma.comaroom.notice.repository.NoticeRepository;
import com.coma.comaroom.utils.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NoticeServiceTest {

    @Mock private NoticeRepository noticeRepository;
    @Mock private SecurityUtils securityUtils;

    @InjectMocks
    private NoticeService noticeService;

    private Member member;
    private Notice notice;

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

        notice = Notice.builder()
                .noticeId(1L)
                .title("공지사항 제목")
                .content("공지사항 내용")
                .pinned(false)
                .hidden(false)
                .noticePriority(NoticePriority.NORMAL)
                .author(member)
                .build();
    }

    // ─────────────────────────────────────────────
    // createNotice
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("공지 생성 성공")
    void createNotice_success() {
        CreateNoticeRequestDto dto = CreateNoticeRequestDto.builder()
                .title("공지사항 제목")
                .content("공지사항 내용")
                .pinned(true)
                .noticePriority(NoticePriority.NORMAL)
                .build();
        when(securityUtils.getCurrentMember()).thenReturn(member);

        CreateNoticeResponseDto result = noticeService.createNotice(dto);

        assertThat(result.getTitle()).isEqualTo("공지사항 제목");
        assertThat(result.getContent()).isEqualTo("공지사항 내용");
        assertThat(result.isPinned()).isTrue();
        // hidden은 요청에 없었으므로 false로 채워진다
        assertThat(result.isHidden()).isFalse();
        assertThat(result.getAuthorName()).isEqualTo("작성자");

        ArgumentCaptor<Notice> captor = ArgumentCaptor.forClass(Notice.class);
        verify(noticeRepository).save(captor.capture());
        assertThat(captor.getValue().getAuthor()).isSameAs(member);
    }

    // ─────────────────────────────────────────────
    // deleteNotice
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("공지 삭제 성공")
    void deleteNotice_success() {
        assertThatNoException().isThrownBy(() -> noticeService.deleteNotice(1L));
        verify(noticeRepository).deleteById(1L);
    }

    // ─────────────────────────────────────────────
    // updateNotice
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("공지 수정 성공")
    void updateNotice_success() {
        UpdateNoticeRequestDto dto = UpdateNoticeRequestDto.builder()
                .title("수정된 제목")
                .build();
        when(noticeRepository.findById(1L)).thenReturn(Optional.of(notice));

        UpdateNoticeResponseDto result = noticeService.updateNotice(1L, dto);

        assertThat(result.getTitle()).isEqualTo("수정된 제목");
        // 요청에 없는 필드는 기존 값이 유지된다
        assertThat(result.getContent()).isEqualTo("공지사항 내용");
        verify(noticeRepository).saveAndFlush(notice);
    }

    @Test
    @DisplayName("공지 수정 실패 - 공지 없음")
    void updateNotice_notFound() {
        UpdateNoticeRequestDto dto = mock(UpdateNoticeRequestDto.class);
        when(noticeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> noticeService.updateNotice(99L, dto))
                .isInstanceOf(BusinessException.class)
                .hasMessage(NoticeErrorCode.NOTICE_NOT_FOUND.getMessage());
    }

    // ─────────────────────────────────────────────
    // getNotices
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("공지 목록 조회 성공")
    void getNotices_success() {
        when(noticeRepository.findByPinnedTrueAndHiddenFalse()).thenReturn(List.of());
        Page<Notice> noticePage = new PageImpl<>(List.of(notice));
        when(noticeRepository.findByPinnedFalseAndHiddenFalse(any())).thenReturn(noticePage);

        GetNoticeResponseDto result = noticeService.getNotices(0);

        assertThat(result.getPinnedNoticeCount()).isZero();
        assertThat(result.getOpenedNoticeCount()).isEqualTo(1L);
        assertThat(result.getTotalNoticeCount()).isEqualTo(1L);
        assertThat(result.getPinnedNoticeList()).isEmpty();
        assertThat(result.getOpenedNoticeList())
                .extracting(NoticeResponseDto::getNoticeTitle)
                .containsExactly("공지사항 제목");
        verify(noticeRepository).findByPinnedTrueAndHiddenFalse();
        verify(noticeRepository).findByPinnedFalseAndHiddenFalse(any());
    }

    // ─────────────────────────────────────────────
    // pinnedNotice
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("공지 고정 해제 성공 - 이미 고정된 공지")
    void pinnedNotice_unpinSuccess() {
        Notice pinnedNotice = Notice.builder()
                .noticeId(1L)
                .title("고정된 공지")
                .content("내용")
                .pinned(true)
                .hidden(false)
                .noticePriority(NoticePriority.NORMAL)
                .author(member)
                .build();

        when(noticeRepository.findById(1L)).thenReturn(Optional.of(pinnedNotice));

        assertThatNoException().isThrownBy(() -> noticeService.pinnedNotice(1L));
        assertThat(pinnedNotice.isPinned()).isFalse();
    }

    @Test
    @DisplayName("공지 고정 성공 - 현재 고정 수 3 미만")
    void pinnedNotice_pinSuccess() {
        when(noticeRepository.findById(1L)).thenReturn(Optional.of(notice));
        when(noticeRepository.countByPinnedTrueAndHiddenFalse()).thenReturn(2L);

        assertThatNoException().isThrownBy(() -> noticeService.pinnedNotice(1L));
        assertThat(notice.isPinned()).isTrue();
    }

    @Test
    @DisplayName("공지 고정 실패 - 최대 3개 초과")
    void pinnedNotice_exceededLimit() {
        when(noticeRepository.findById(1L)).thenReturn(Optional.of(notice));
        when(noticeRepository.countByPinnedTrueAndHiddenFalse()).thenReturn(3L);

        assertThatThrownBy(() -> noticeService.pinnedNotice(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessage(NoticeErrorCode.EXCEEDED_PINNED_LIMIT.getMessage());
    }

    @Test
    @DisplayName("공지 고정 실패 - 공지 없음")
    void pinnedNotice_notFound() {
        when(noticeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> noticeService.pinnedNotice(99L))
                .isInstanceOf(BusinessException.class)
                .hasMessage(NoticeErrorCode.NOTICE_NOT_FOUND.getMessage());
    }

    // ─────────────────────────────────────────────
    // hiddenNotice
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("공지 숨김 처리 성공")
    void hiddenNotice_success() {
        when(noticeRepository.findById(1L)).thenReturn(Optional.of(notice));

        assertThatNoException().isThrownBy(() -> noticeService.hiddenNotice(1L));
        assertThat(notice.isHidden()).isTrue();
    }

    @Test
    @DisplayName("공지 숨김 실패 - 공지 없음")
    void hiddenNotice_notFound() {
        when(noticeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> noticeService.hiddenNotice(99L))
                .isInstanceOf(BusinessException.class)
                .hasMessage(NoticeErrorCode.NOTICE_NOT_FOUND.getMessage());
    }
}
