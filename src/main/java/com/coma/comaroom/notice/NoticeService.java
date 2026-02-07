package com.coma.comaroom.notice;

import com.coma.comaroom.member.entity.Member;
import com.coma.comaroom.notice.dto.request.CreateNoticeRequestDto;
import com.coma.comaroom.notice.dto.response.CreateNoticeResponseDto;
import com.coma.comaroom.notice.entity.Notice;
import com.coma.comaroom.notice.repository.NoticeRepository;
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
        CreateNoticeResponseDto createNoticeResponseDto = noticeMapper.toCreateResponseDto(newNotice);
        return createNoticeResponseDto;

    }

    public void deleteNotice(Long noticeId) {
        noticeRepository.deleteById(noticeId);
    }
}
