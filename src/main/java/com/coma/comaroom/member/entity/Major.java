package com.coma.comaroom.member.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Major {
    FREE_MAJOR("자유전공학부"),

    KOREAN_LANGUAGE_AND_LITERATURE("국어국문학과"),
    PHILOSOPHY("철학과"),
    KOREAN_HISTORY("국사학과"),
    ENGLISH_LANGUAGE_AND_LITERATURE("영어영문학부"),
    CHINESE_LANGUAGE_AND_CULTURE("중국언어문화학과"),
    JAPANESE_LANGUAGE_AND_LITERATURE("일어일본문학과"),
    FRENCH_LANGUAGE_AND_LITERATURE("프랑스어문학과"),
    SOCIAL_WELFARE("사회복지학과"),
    PSYCHOLOGY("심리학과"),
    SOCIOLOGY("사회학과"),
    BUSINESS_ADMINISTRATION("경영학과"),
    ACCOUNTING("회계학과"),
    INTERNATIONAL_STUDIES("국제학부"),
    LAW("법학과"),
    ECONOMICS("경제학과"),
    PUBLIC_ADMINISTRATION("행정학과"),
    CHILD_STUDIES("아동학과"),

    CHEMISTRY("화학과"),
    MATHEMATICS("수학과"),
    PHYSICS("물리학과"),
    SPACE_DESIGN_AND_CONSUMER("공간디자인·소비자학과"),
    FASHION("의류학과"),
    FOOD_AND_NUTRITION("식품영양학과"),
    COMPUTER_INFO("컴퓨터정보공학부"),
    MEDIA_TECH_CONTENT("미디어기술콘텐츠학과"),
    INFORMATION_COMMUNICATION_ELECTRONICS("정보통신전자공학부"),
    BIOTECHNOLOGY("생명공학과"),
    ENERGY_ENVIRONMENT_ENGINEERING("에너지환경공학과"),
    BIOMEDICAL_CHEMICAL_ENGINEERING("바이오메디컬화학공학과"),
    BIOMEDICAL_SCIENCE("의생명과학과"),
    ARTIFICIAL_INTELLIGENCE("인공지능학과"),
    DATA_SCIENCE("데이터사이언스학과"),
    BIOMEDICAL_SOFTWARE("바이오메디컬소프트웨어학과"),

    SPECIAL_EDUCATION("특수교육과"),
    BIOLOGICS_ENGINEERING("바이오로직스공학부"),
    AI_BIOMEDICAL_ENGINEERING("AI의공학과"),

    GLOBAL_BUSINESS("글로벌경영학과"),
    IT_FINANCE("IT파이낸스학과"),

    MUSIC("음악과"),

    PHARMACY("약학과"),
    MEDICINE_PRE("의예과"),
    NURSING("간호학과"),

    THEOLOGY("신학과");




    private final String major;
}
