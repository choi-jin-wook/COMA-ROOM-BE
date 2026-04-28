package com.coma.comaroom.vote.controller;

import com.coma.comaroom.utils.Response;
import com.coma.comaroom.vote.dto.AddVoteOptionRequestDto;
import com.coma.comaroom.vote.dto.request.CreateNewVoteRequestDto;
import com.coma.comaroom.vote.dto.request.ParticipateVoteRequestDto;
import com.coma.comaroom.vote.dto.request.UpdateVoteRequestDto;
import com.coma.comaroom.vote.dto.response.VoteDetailResponseDto;
import com.coma.comaroom.vote.entity.VoteStatus;
import com.coma.comaroom.vote.service.VoteService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/vote")
public class VoteController {
    private final VoteService voteService;

    // - 사용자
    // 1. 진행중인 투표 목록 (뭔가 꼬롬함)
    @GetMapping("/votes")
    public ResponseEntity<Response<List<VoteDetailResponseDto>>> voteDashboard(@RequestParam(defaultValue = "1", required = false) Integer page, @RequestParam VoteStatus status) {
        List<VoteDetailResponseDto> voteDetailResponseDtoList = voteService.voteDashboard(page - 1, status);
        return Response.ok(voteDetailResponseDtoList, HttpStatus.OK).toResponseEntity();
    }

    // 2. 투표 참여 (포스트맨 테스트 완료)
    @PostMapping("/votes/{voteId}/participate")
    public ResponseEntity<?> participateVote(@PathVariable Long voteId, @RequestBody ParticipateVoteRequestDto participateVoteRequestDto) {
        VoteDetailResponseDto voteDetailResponseDto = voteService.participateVote(participateVoteRequestDto, voteId);
        return Response.ok(voteDetailResponseDto, HttpStatus.OK).toResponseEntity();

    }

    // 3. 투표 취소
    @DeleteMapping("/votes/{voteId}/participate")
    public ResponseEntity<?> cancelVote(@PathVariable Long voteId) {
        voteService.cancelVote(voteId);
        return Response.ok(null, HttpStatus.OK).toResponseEntity();
    }

    // - 관리자

}
