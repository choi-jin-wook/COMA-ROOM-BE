package com.coma.comaroom.member.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Major {
    COMPUTER_INFO("컴퓨터정보공학부"),
    MEDIA_CONTENT("미디어기술콘텐츠학과"),
    INFO_COMM("정보통신전자공학부");

    private final String name;
}
