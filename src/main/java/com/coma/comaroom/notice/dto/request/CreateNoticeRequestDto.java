package com.coma.comaroom.notice.dto.request;

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
    private boolean pinned;

    // 숨김 여부 (선택)
    private boolean hidden;
}
