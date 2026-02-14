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
public class UpdateNoticeRequestDto {
    private String title;

    private String content;

    private NoticePriority noticePriority;

    private Boolean pinned;

    private Boolean hidden;
}
