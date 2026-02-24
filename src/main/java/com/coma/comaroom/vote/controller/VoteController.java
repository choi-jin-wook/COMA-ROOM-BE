package com.coma.comaroom.vote.controller;

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
@RequestMapping("/api")
public class VoteController {
    private final VoteService voteService;

    // - 사용자
    // 1. 진행중인 투표 목록 (뭔가 꼬롬함)
    @GetMapping("/votes")
    public ResponseEntity<List<VoteDetailResponseDto>> voteDashboard(@RequestParam(defaultValue = "1", required = false) Integer page, @RequestParam VoteStatus status) {
        List<VoteDetailResponseDto> voteDetailResponseDtoList = voteService.voteDashboard(page - 1, status);
        return new ResponseEntity<>(voteDetailResponseDtoList, HttpStatus.OK);
    }

    // 2. 투표 참여 (포스트맨 테스트 완료)
    @PostMapping("/votes/{voteId}/participate")
    public ResponseEntity<?> participateVote(@PathVariable Long voteId, @RequestBody ParticipateVoteRequestDto participateVoteRequestDto) {
        VoteDetailResponseDto voteDetailResponseDto = voteService.participateVote(participateVoteRequestDto, voteId);
        return new ResponseEntity<>(voteDetailResponseDto, HttpStatus.OK);

    }

    // - 관리자
    // 1. 투표 생성 (포스트맨 테스트 완료)
    @PostMapping("/admin/votes")
    public ResponseEntity<VoteDetailResponseDto> createNewVote(@RequestBody CreateNewVoteRequestDto createNewVoteRequestDto) {
        VoteDetailResponseDto voteDetailResponseDto = voteService.createNewVote(createNewVoteRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(voteDetailResponseDto);
    }

    // 2. 투표 수정 (제목, 마감일, 중복여부) (포스트맨 테스트 완료)
    @PatchMapping("/admin/votes/{voteId}")
    public ResponseEntity<?> updateVote(@RequestBody UpdateVoteRequestDto updateVoteRequestDto, @PathVariable Long voteId) {
        VoteDetailResponseDto voteDetailResponseDto = voteService.updateVote(updateVoteRequestDto, voteId);
        return ResponseEntity.status(HttpStatus.OK).body(voteDetailResponseDto);
    }


    // 3. 옵션 추가 (포스트맨 테스트 완료)
    @PostMapping("/admin/votes/{voteId}/options")
    public ResponseEntity<?> addVoteOption(@RequestBody AddVoteOptionRequestDto addVoteOptionRequestDto,  @PathVariable Long voteId) {
        VoteDetailResponseDto voteDetailResponseDto = voteService.addVoteOption(addVoteOptionRequestDto, voteId);
        return ResponseEntity.status(HttpStatus.CREATED).body(voteDetailResponseDto);
    }

    // 4. 옵션 삭제 (포스트맨 테스트 완료)
    @DeleteMapping("/admin/votes/{voteId}/options/{optionId}")
    public ResponseEntity<?> deleteVoteOption(@PathVariable Long optionId,  @PathVariable Long voteId) {
        voteService.deleteVoteOption(optionId, voteId);
        return ResponseEntity.noContent().build();
    }

    // 5. 투표 삭제
    @DeleteMapping("/admin/votes/{voteId}")
    public ResponseEntity<?> deleteVote(@PathVariable Long voteId) {
        voteService.deleteVote(voteId);
        return ResponseEntity.noContent().build();
    }

    // 6. 투표 종료 (포스트맨 테스트 완료)
    @PatchMapping("/admin/votes/{voteId}/close")
    public ResponseEntity<?> closeVote(@PathVariable Long voteId) {
        VoteDetailResponseDto voteDetailResponseDto = voteService.closeVote(voteId);
        return ResponseEntity.ok(voteDetailResponseDto);
    }
}
