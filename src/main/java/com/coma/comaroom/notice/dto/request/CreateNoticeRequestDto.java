package com.coma.comaroom.notice.dto.request;

import com.coma.comaroom.member.entity.Member;
import com.coma.comaroom.notice.entity.Notice;
import com.coma.comaroom.notice.entity.NoticePriority;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateNoticeRequestDto {

    private String title;
    private String content;

    // 고정 여부 (선택)
    private Boolean pinned;

    // 숨김 여부 (선택)
    private Boolean hidden;

    private NoticePriority  noticePriority;

    public Notice toEntity(Member author) {
        return Notice.builder()
                .title(title)
                .content(content)
                // pinned/hidden은 선택 항목이므로 값이 없으면 false로 둔다
                .pinned(Boolean.TRUE.equals(pinned))
                .hidden(Boolean.TRUE.equals(hidden))
                .author(author)
                .noticePriority(noticePriority)
                .build();
    }
}
