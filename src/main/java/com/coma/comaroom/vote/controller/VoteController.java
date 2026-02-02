package com.coma.comaroom.vote.controller;

import com.coma.comaroom.vote.dto.request.CreateNewVoteRequestDto;
import com.coma.comaroom.vote.dto.response.CreateNewVoteResponseDto;
import com.coma.comaroom.vote.service.VoteService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<?> CreateNewVote(@RequestBody CreateNewVoteRequestDto createNewVoteRequestDto) {
        CreateNewVoteResponseDto createNewVoteResponseDto = voteService.createNewVote(createNewVoteRequestDto);
        return new ResponseEntity<>(createNewVoteResponseDto, HttpStatus.CREATED);

    }

    // 2. 투표 수정
    // 3. 옵션 추가
    // 4. 투표 삭제
    // 5. 투표 종료
}
