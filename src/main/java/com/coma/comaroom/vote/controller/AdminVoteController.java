package com.coma.comaroom.vote.controller;

import com.coma.comaroom.utils.Response;
import com.coma.comaroom.vote.dto.AddVoteOptionRequestDto;
import com.coma.comaroom.vote.dto.request.CreateNewVoteRequestDto;
import com.coma.comaroom.vote.dto.request.UpdateVoteRequestDto;
import com.coma.comaroom.vote.dto.response.VoteDetailResponseDto;
import com.coma.comaroom.vote.service.AdminVoteService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/admin/votes")
public class AdminVoteController {
    private final AdminVoteService adminVoteService;

    // 1. 투표 생성 (포스트맨 테스트 완료)
    @PostMapping()
    public ResponseEntity<Response<VoteDetailResponseDto>> createNewVote(@RequestBody CreateNewVoteRequestDto createNewVoteRequestDto) {
        VoteDetailResponseDto voteDetailResponseDto = adminVoteService.createNewVote(createNewVoteRequestDto);
        return Response.ok(voteDetailResponseDto, HttpStatus.CREATED).toResponseEntity();
    }

    // 2. 투표 수정 (제목, 마감일, 중복여부) (포스트맨 테스트 완료)
    @PatchMapping("/{voteId}")
    public ResponseEntity<?> updateVote(@RequestBody UpdateVoteRequestDto updateVoteRequestDto, @PathVariable Long voteId) {
        VoteDetailResponseDto voteDetailResponseDto = adminVoteService.updateVote(updateVoteRequestDto, voteId);
        return Response.ok(voteDetailResponseDto, HttpStatus.OK).toResponseEntity();
    }


    // 3. 옵션 추가 (포스트맨 테스트 완료)
    @PostMapping("/{voteId}/options")
    public ResponseEntity<?> addVoteOption(@RequestBody AddVoteOptionRequestDto addVoteOptionRequestDto, @PathVariable Long voteId) {
        VoteDetailResponseDto voteDetailResponseDto = adminVoteService.addVoteOption(addVoteOptionRequestDto, voteId);
        return Response.ok(voteDetailResponseDto, HttpStatus.CREATED).toResponseEntity();
    }

    // 4. 옵션 삭제 (포스트맨 테스트 완료)
    @DeleteMapping("/{voteId}/options/{optionId}")
    public ResponseEntity<?> deleteVoteOption(@PathVariable Long optionId,  @PathVariable Long voteId) {
        adminVoteService.deleteVoteOption(optionId, voteId);
        return Response.ok(HttpStatus.NO_CONTENT).toResponseEntity();
    }

    // 5. 투표 삭제
    @DeleteMapping("/{voteId}")
    public ResponseEntity<?> deleteVote(@PathVariable Long voteId) {
        adminVoteService.deleteVote(voteId);
        return Response.ok(HttpStatus.NO_CONTENT).toResponseEntity();
    }

    // 6. 투표 종료 (포스트맨 테스트 완료)
    @PatchMapping("{voteId}/close")
    public ResponseEntity<?> closeVote(@PathVariable Long voteId) {
        VoteDetailResponseDto voteDetailResponseDto = adminVoteService.closeVote(voteId);
        return Response.ok(voteDetailResponseDto, HttpStatus.OK).toResponseEntity();
    }
}
