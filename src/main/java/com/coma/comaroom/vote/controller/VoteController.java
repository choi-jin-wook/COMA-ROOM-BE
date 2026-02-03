package com.coma.comaroom.vote.controller;

import com.coma.comaroom.vote.dto.AddVoteOptionRequestDto;
import com.coma.comaroom.vote.dto.request.CreateNewVoteRequestDto;
import com.coma.comaroom.vote.dto.request.UpdateVoteRequestDto;
import com.coma.comaroom.vote.dto.response.VoteDetailResponseDto;
import com.coma.comaroom.vote.service.VoteService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
public class VoteController {
    private final VoteService voteService;

    // - 사용자
    // 1. 전체 투표 조회
    // 2. 투표 상세 조회

    // 3. 진행중인 투표 목록
    // 4. 종료된 투표 목록
    // 5. 투표 참여

    // - 관리자
    // 1. 투표 생성
    @PostMapping()
    public ResponseEntity<VoteDetailResponseDto> CreateNewVote(@RequestBody CreateNewVoteRequestDto createNewVoteRequestDto) {
        VoteDetailResponseDto voteDetailResponseDto = voteService.createNewVote(createNewVoteRequestDto);
        return new ResponseEntity<>(voteDetailResponseDto, HttpStatus.CREATED);

    }

    // 2. 투표 수정 (제목, 마감일, 중복여부)
    @PatchMapping()
    public ResponseEntity<?> updateVote(@RequestBody UpdateVoteRequestDto updateVoteRequestDto) {
        VoteDetailResponseDto voteDetailResponseDto = voteService.updateVote(updateVoteRequestDto);
        return new ResponseEntity<>(voteDetailResponseDto, HttpStatus.OK);
    }


    // 3. 옵션 추가
    @PostMapping("/test")
    public ResponseEntity<?> addVoteOption(@RequestBody AddVoteOptionRequestDto addVoteOptionRequestDto) {
        VoteDetailResponseDto voteDetailResponseDto = voteService.addVoteOption(addVoteOptionRequestDto);
        return new ResponseEntity<>(voteDetailResponseDto, HttpStatus.OK);
    }

    // 4. 옵션 삭제
    @DeleteMapping()
    public ResponseEntity<?> deleteVoteOption(@RequestParam Long voteOptionId) {
        voteService.deleteVoteOption(voteOptionId);
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    // 5. 투표 삭제
    @DeleteMapping("/test")
    public ResponseEntity<?> deleteVote(@RequestParam Long voteId) {
        voteService.deleteVote(voteId);
        return new ResponseEntity<>(HttpStatus.OK);
//        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    // 6. 투표 종료
    @PatchMapping()
    public ResponseEntity<?> closeVote(@RequestParam Long voteId) {
        VoteDetailResponseDto voteDetailResponseDto = voteService.closeVote(voteId);
        return new ResponseEntity<>(voteDetailResponseDto, HttpStatus.OK);
    }
}
