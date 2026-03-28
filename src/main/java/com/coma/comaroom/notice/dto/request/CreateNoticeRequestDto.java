package com.coma.comaroom.notice.dto.request;

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
}
