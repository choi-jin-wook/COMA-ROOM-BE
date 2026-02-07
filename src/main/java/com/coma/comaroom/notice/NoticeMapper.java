package com.coma.comaroom.notice;

import com.coma.comaroom.member.entity.Member;
import com.coma.comaroom.notice.dto.request.CreateNoticeRequestDto;
import com.coma.comaroom.notice.dto.response.CreateNoticeResponseDto;
import com.coma.comaroom.notice.entity.Notice;
import org.springframework.stereotype.Component;

@Component
public class NoticeMapper {
    public Notice createNotice(CreateNoticeRequestDto createNoticeRequestDto, Member author) {
        Notice notice = Notice.builder()
                .title(createNoticeRequestDto.getTitle())
                .content(createNoticeRequestDto.getContent())
                .pinned(createNoticeRequestDto.isPinned())
                .hidden(createNoticeRequestDto.isHidden())
                .author(author)
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
                .authorId(notice.getAuthor().getMemberId())
                .authorName(notice.getAuthor().getName())
                .createdAt(notice.getCreatedAt())
                .updatedAt(notice.getUpdatedAt())
                .build();

        return createNoticeResponseDto;
    }

}
