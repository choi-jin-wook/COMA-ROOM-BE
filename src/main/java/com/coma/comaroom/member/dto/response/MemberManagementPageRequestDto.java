package com.coma.comaroom.member.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberManagementPageRequestDto {
    private Long totalMember; // 전체사용자
    private Long activateMember; // 활성사용자 (일단은 전체 사용자로 넣기)
    private Long averageXp; // 전체 사용자의 평균 xp

    List<MemberInformationResponseDto>  memberInformationResponseDtos;
    private int currentPage;
    private int totalPages;
    private long totalElements;
}
