package com.coma.comaroom.notice;

import com.coma.comaroom.BusinessException;
import com.coma.comaroom.member.entity.Member;
import com.coma.comaroom.notice.dto.request.CreateNoticeRequestDto;
import com.coma.comaroom.notice.dto.request.UpdateNoticeRequestDto;
import com.coma.comaroom.notice.dto.response.CreateNoticeResponseDto;
import com.coma.comaroom.notice.dto.response.UpdateNoticeResponseDto;
import com.coma.comaroom.notice.entity.Notice;
import com.coma.comaroom.notice.exception.NoticeErrorCode;
import com.coma.comaroom.notice.repository.NoticeRepository;
import com.coma.comaroom.utils.ErrorCode;
import com.coma.comaroom.utils.SecurityUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class NoticeService {
    private final NoticeRepository noticeRepository;
    private final NoticeMapper noticeMapper;
    private final SecurityUtils securityUtils;

    public CreateNoticeResponseDto createNotice(CreateNoticeRequestDto createNoticeRequestDto) {
        Member author = securityUtils.getCurrentMember();
        Notice newNotice = noticeMapper.createNotice(createNoticeRequestDto, author);
        noticeRepository.save(newNotice);
        return noticeMapper.toCreateResponseDto(newNotice);

    }

    public void deleteNotice(Long noticeId) {
        noticeRepository.deleteById(noticeId);
    }

    public UpdateNoticeResponseDto updateNotice(Long noticeId, UpdateNoticeRequestDto updateNoticeRequestDto) {
        Notice notice = noticeRepository.findById(noticeId).orElseThrow(() -> new BusinessException(NoticeErrorCode.NOTICE_NOT_FOUND));
        notice.update(updateNoticeRequestDto);
        noticeRepository.saveAndFlush(notice);
        return noticeMapper.toUpdateResponseDto(notice);
    }
}
