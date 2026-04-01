package com.coma.comaroom.member.controller;

import com.coma.comaroom.event.dto.*;
import com.coma.comaroom.event.entity.ApprovalStatus;
import com.coma.comaroom.member.dto.response.XpManagementPageResponseDto;
import com.coma.comaroom.member.service.AdminMemberService;
import com.coma.comaroom.utils.Response;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/admin/member")
public class AdminMemberController {
    private final AdminMemberService adminMemberService;
    // xp 관리 페이지
    @GetMapping("ask-xp")
    public ResponseEntity<?> xpManagementPage(@RequestParam(defaultValue = "0") int page) {
        XpManagementPageResponseDto xpManagementPageResponseDto = adminMemberService.xpManagementPage(page);
        return Response.ok(xpManagementPageResponseDto, HttpStatus.OK).toResponseEntity();
    }


    // 2. xp지급 승인 거절 (포스트맨 테스트 완료)
    @PatchMapping("/ask-xp/status/{requestId}")
    public ResponseEntity<Response<Void>> decideProvision(
            @RequestBody ProvisionApprovalRequestDto provisionApprovalRequestDto,
            @PathVariable Long requestId) {
        adminMemberService.decideProvision(provisionApprovalRequestDto, requestId);
        return Response.ok(HttpStatus.OK).toResponseEntity();
    }


    // 3. xp 지급 (포스트맨 테스트 완료)
    @PostMapping("/provide-xp")
    public ResponseEntity<?> provideXp(@RequestBody XpProvisionRequestDto xpProvisionRequestDto) {
        adminMemberService.provisionXp(xpProvisionRequestDto);
        return Response.ok(HttpStatus.OK).toResponseEntity();
    }
}