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

    public CreateNoticeResponseDto createNotice(CreateNoticeRequestDto createNoticeRequestDto) {
        Member author = securityUtils.getCurrentMember();
        Notice newNotice = createNoticeRequestDto.toEntity(author);
        noticeRepository.save(newNotice);
        return CreateNoticeResponseDto.from(newNotice);

    }

    public void deleteNotice(Long noticeId) {
        noticeRepository.deleteById(noticeId);
    }

    public UpdateNoticeResponseDto updateNotice(Long noticeId, UpdateNoticeRequestDto updateNoticeRequestDto) {
        Notice notice = noticeRepository.findById(noticeId).orElseThrow(() -> new BusinessException(NoticeErrorCode.NOTICE_NOT_FOUND));
        notice.update(updateNoticeRequestDto);
        noticeRepository.saveAndFlush(notice);
        return UpdateNoticeResponseDto.from(notice);
    }

    public GetNoticeResponseDto getNotices(int page) {
        final int PAGE_SIZE = 10;
        Pageable pageable = PageRequest.of(page, PAGE_SIZE);
        List<Notice> pinnedNotices = noticeRepository.findByPinnedTrueAndHiddenFalse();
        Page<Notice> openedNoticePage = noticeRepository.findByPinnedFalseAndHiddenFalse(pageable);
        
        return GetNoticeResponseDto.of(pinnedNotices, openedNoticePage);
    }

    public void pinnedNotice(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId).orElseThrow(() -> new BusinessException(NoticeErrorCode.NOTICE_NOT_FOUND));
        if (!notice.isPinned()) {
            long pinnedCount = noticeRepository.countByPinnedTrueAndHiddenFalse();
            if (pinnedCount >= 3) {
                throw new BusinessException(NoticeErrorCode.EXCEEDED_PINNED_LIMIT);
            }
        }

        notice.updatePinned();
    }

    public void hiddenNotice(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId).orElseThrow(() -> new BusinessException(NoticeErrorCode.NOTICE_NOT_FOUND));
        notice.updateHidden();
    }
}
