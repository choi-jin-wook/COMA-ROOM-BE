package com.coma.comaroom.event.dto;


import com.coma.comaroom.event.entity.EventCategory;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateAttendanceCheckRequestDto {
    private String eventTitle;              // 행사명
    private EventCategory eventCategory;   // 활동 종류 (ENUM)
    private Integer expirationTime;        // 유효시간 (분 단위)
    private String location;

    /**
     * XP 부여(Provision) 요청 DTO
     * 롬복 적용 버전 / Validation 제거
     */
//    @Data
//    @Builder
//    @NoArgsConstructor
//    @AllArgsConstructor
//    public static class XpProvisionRequestDto {
//
//        private String studentId;
//
//        // 부여된 XP 수량
//        private Integer provisionAmount;
//
//        // 지급 사유
//        private String provisionReason;
//
//    }
}
