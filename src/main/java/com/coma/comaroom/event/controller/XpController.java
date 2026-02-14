package com.coma.comaroom.event.controller;

import com.coma.comaroom.event.dto.*;
import com.coma.comaroom.event.entity.ApprovalStatus;
import com.coma.comaroom.event.service.XpService;
import com.coma.comaroom.utils.Response;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/admin/xp")
public class XpController {
    private final XpService xpService;
    // 1. xp지급 요청 (포스트맨 테스트 완료)
    @PostMapping()
    public ResponseEntity<?> requestXpProvision(@RequestBody XpPetitionRequestDto xpPetitionRequestDto) {
        XpPetitionResponseDto xpPetitionResponseDto = xpService.requestProvisionXp(xpPetitionRequestDto);

        return Response.ok(xpPetitionResponseDto, HttpStatus.CREATED).toResponseEntity();
    }


    // 2. xp지급 승인 거절 (포스트맨 테스트 완료)
    @PatchMapping("/{requestId}")
    public ResponseEntity<Response<Void>> decideProvision(@RequestBody ProvisionApprovalRequestDto provisionApprovalRequestDto, @PathVariable Long requestId) {
        xpService.decideProvision(provisionApprovalRequestDto, requestId);
        return Response.ok(HttpStatus.OK).toResponseEntity();
    }

    // 3. xp 메인페이지 (포스트맨 테스트 완료)
    @GetMapping()
    public ResponseEntity<Response<XpManagementMainResponseDto>> getXpMainPage(
            @RequestParam(required = false) ApprovalStatus status, // 파라미터 없으면 null
            @RequestParam(defaultValue = "1") Long page) {

        // status가 null이면 서비스단에서 '전체 조회' 로직으로 처리하면 됩니다.
        XpManagementMainResponseDto xpManagementMainResponseDto = xpService.getXpManagementMainData(status, page - 1);
        return Response.ok(xpManagementMainResponseDto, HttpStatus.OK).toResponseEntity();
    }

    // 4. xp 지급 (포스트맨 테스트 완료)
    @PostMapping("/provision")
    public ResponseEntity<?> provisionXp(@RequestBody XpProvisionRequestDto xpProvisionRequestDto) {
        xpService.provisionXp(xpProvisionRequestDto);
        return Response.ok(HttpStatus.OK).toResponseEntity();
    }
}