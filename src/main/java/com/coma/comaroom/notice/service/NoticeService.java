package com.coma.comaroom.notice.service;

import com.coma.comaroom.BusinessException;
import com.coma.comaroom.member.entity.Member;
import com.coma.comaroom.notice.dto.request.CreateNoticeRequestDto;
import com.coma.comaroom.notice.dto.request.UpdateNoticeRequestDto;
import com.coma.comaroom.notice.dto.response.CreateNoticeResponseDto;
import com.coma.comaroom.notice.dto.response.GetNoticeResponseDto;
import com.coma.comaroom.notice.dto.response.UpdateNoticeResponseDto;
import com.coma.comaroom.notice.entity.Notice;
import com.coma.comaroom.notice.exception.NoticeErrorCode;
import com.coma.comaroom.notice.repository.NoticeRepository;
import com.coma.comaroom.utils.SecurityUtils;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@AllArgsConstructor
public class NoticeService {
    private final NoticeRepository noticeRepository;
    private final SecurityUtils securityUtils;

    public GetNoticeResponseDto getNotices(int page) {
        final int PAGE_SIZE = 10;
        Pageable pageable = PageRequest.of(page, PAGE_SIZE);
        List<Notice> pinnedNotices = noticeRepository.findByPinnedTrueAndHiddenFalse();
        Page<Notice> openedNoticePage = noticeRepository.findByPinnedFalseAndHiddenFalse(pageable);
        
        return GetNoticeResponseDto.of(pinnedNotices, openedNoticePage);
    }

}
