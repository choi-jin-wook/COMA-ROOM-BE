package com.coma.comaroom.member.controller;

import com.coma.comaroom.event.dto.*;
import com.coma.comaroom.event.entity.ApprovalStatus;
import com.coma.comaroom.member.service.AdminMemberService;
import com.coma.comaroom.utils.Response;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/admin/xp")
public class AdminMemberController {
    private final AdminMemberService adminMemberService;
    // 1. xp지급 요청 (포스트맨 테스트 완료)
    @PostMapping()
    public ResponseEntity<?> requestXpProvision(@RequestBody XpPetitionRequestDto xpPetitionRequestDto) {
        XpPetitionResponseDto xpPetitionResponseDto = adminMemberService.requestProvisionXp(xpPetitionRequestDto);
        return Response.ok(xpPetitionResponseDto, HttpStatus.CREATED).toResponseEntity();
    }


    // 2. xp지급 승인 거절 (포스트맨 테스트 완료)
    @PatchMapping("/{requestId}")
    public ResponseEntity<Response<Void>> decideProvision(
            @RequestBody ProvisionApprovalRequestDto provisionApprovalRequestDto,
            @PathVariable Long requestId) {
        adminMemberService.decideProvision(provisionApprovalRequestDto, requestId);
        return Response.ok(HttpStatus.OK).toResponseEntity();
    }

    // 3. xp 메인페이지 (포스트맨 테스트 완료)
    @GetMapping()
    public ResponseEntity<Response<XpManagementMainResponseDto>> getXpMainPage(
            @RequestParam(required = false) ApprovalStatus status,
            @RequestParam(defaultValue = "1") Long page) {
        XpManagementMainResponseDto xpManagementMainResponseDto = adminMemberService.getXpManagementMainData(status, page - 1);
        return Response.ok(xpManagementMainResponseDto, HttpStatus.OK).toResponseEntity();
    }

    // 4. xp 지급 (포스트맨 테스트 완료)
    @PostMapping("/provision")
    public ResponseEntity<?> provisionXp(@RequestBody XpProvisionRequestDto xpProvisionRequestDto) {
        adminMemberService.provisionXp(xpProvisionRequestDto);
        return Response.ok(HttpStatus.OK).toResponseEntity();
    }
}