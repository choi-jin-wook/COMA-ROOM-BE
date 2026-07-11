package com.coma.comaroom.notice.mapper;

import com.coma.comaroom.member.entity.Member;
import com.coma.comaroom.notice.dto.request.CreateNoticeRequestDto;
import com.coma.comaroom.notice.dto.response.CreateNoticeResponseDto;
import com.coma.comaroom.notice.dto.response.GetNoticeResponseDto;
import com.coma.comaroom.notice.dto.response.NoticeResponseDto;
import com.coma.comaroom.notice.dto.response.UpdateNoticeResponseDto;
import com.coma.comaroom.notice.entity.Notice;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NoticeMapper {
    public Notice createNotice(CreateNoticeRequestDto createNoticeRequestDto, Member author) {
        Notice notice = Notice.builder()
                .title(createNoticeRequestDto.getTitle())
                .content(createNoticeRequestDto.getContent())
                .pinned(createNoticeRequestDto.getPinned())
                .hidden(createNoticeRequestDto.getHidden())
                .author(author)
                .noticePriority(createNoticeRequestDto.getNoticePriority())
                .build();

        return notice;
    }


    public CreateNoticeResponseDto toCreateResponseDto(Notice notice) {
        CreateNoticeResponseDto createNoticeResponseDto = CreateNoticeResponseDto.builder()
                .noticeId(notice.getNoticeId())
                .title(notice.getTitle())
                .content(notice.getTitle())
                .pinned(notice.isPinned())
                .hidden(notice.isHidden())
                .authorId(notice.getAuthor() != null ? notice.getAuthor().getMemberId() : null)
                .authorName(notice.getAuthor() != null ? notice.getAuthor().getName() : "탈퇴한 회원")
                .createdAt(notice.getCreatedAt())
                .updatedAt(notice.getUpdatedAt())
                .noticePriority(notice.getNoticePriority())
                .build();

        return createNoticeResponseDto;
    }

    public UpdateNoticeResponseDto toUpdateResponseDto(Notice notice) {
        UpdateNoticeResponseDto updateNoticeResponseDto = UpdateNoticeResponseDto.builder()
                .noticeId(notice.getNoticeId())
                .title(notice.getTitle())
                .content(notice.getTitle())
                .pinned(notice.isPinned())
                .hidden(notice.isHidden())
                .authorId(notice.getAuthor() != null ? notice.getAuthor().getMemberId() : null)
                .authorName(notice.getAuthor() != null ? notice.getAuthor().getName() : "탈퇴한 회원")
                .createdAt(notice.getCreatedAt())
                .updatedAt(notice.getUpdatedAt())
                .noticePriority(notice.getNoticePriority())
                .build();

        return updateNoticeResponseDto;
    }


    public GetNoticeResponseDto getNoticeResponseDtoMapper(List<Notice> pinnedNotices, Page<Notice> openedNoticePage) {
        // 1. 개별 엔티티를 NoticeResponseDto로 변환
        List<NoticeResponseDto> pinnedDtoList = pinnedNotices.stream()
                .map(this::convertToResponseDto)
                .toList();

        List<NoticeResponseDto> openedDtoList = openedNoticePage.getContent().stream()
                .map(this::convertToResponseDto)
                .toList();

        // 2. 카운트 계산
        long pinnedCount = pinnedNotices.size();
        long openedCount = openedNoticePage.getTotalElements(); // 전체 페이지의 총 개수
        long totalCount = pinnedCount + openedCount;

        // 3. 최종 DTO 생성 (Builder 패턴 사용 가정)
        return GetNoticeResponseDto.builder()
                .totalNoticeCount(totalCount)
                .pinnedNoticeCount(pinnedCount)
                .openedNoticeCount(openedCount)
                .pinnedNoticeList(pinnedDtoList)
                .openedNoticeList(openedDtoList)
                .build();
    }

    // 개별 엔티티 -> Dto 변환 로직 (가독성을 위해 분리)
    private NoticeResponseDto convertToResponseDto(Notice notice) {
        return NoticeResponseDto.builder()
                .noticeId(notice.getNoticeId())
                .noticeTitle(notice.getTitle())
                .noticeContent(notice.getContent())
                .noticePriority(notice.getNoticePriority())
                .createdAt(notice.getCreatedAt())
                .build();
    }
}