package com.coma.comaroom.notice.entity;

import com.coma.comaroom.member.entity.Member;
import com.coma.comaroom.notice.dto.request.CreateNoticeRequestDto;
import com.coma.comaroom.notice.dto.request.UpdateNoticeRequestDto;
import com.coma.comaroom.utils.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "notice")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Notice extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notice_id")
    private Long noticeId;

    @Column(name = "title",  nullable = false)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    // 공지 고정 여부
    @Column(name = "is_pinned", nullable = false)
    private boolean pinned;

    // 공지 숨김 여부
    @Column(name = "is_hidden", nullable = false)
    private boolean hidden;

    // 긴급 일반 중요
    @Column(name = "notice_priority", nullable = false)
    @Enumerated(EnumType.STRING)
    NoticePriority noticePriority;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member author;


    public void update(UpdateNoticeRequestDto requestDto) {
        if (requestDto.getTitle() != null) this.title = requestDto.getTitle();
        if (requestDto.getContent() != null) this.content = requestDto.getContent();
        if (requestDto.getNoticePriority() != null) this.noticePriority = requestDto.getNoticePriority();

        // Boolean 객체 타입일 경우 null 체크 가능
        if (requestDto.getPinned() != null) this.pinned = requestDto.getPinned();
        if (requestDto.getHidden() != null) this.hidden = requestDto.getHidden();
    }

    public void updatePinned() {
        this.pinned = !this.pinned;
    }

    public void updateHidden() {
        this.hidden = !this.hidden;
    }
}
