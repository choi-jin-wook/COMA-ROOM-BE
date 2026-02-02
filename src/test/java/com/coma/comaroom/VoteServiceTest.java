package com.coma.comaroom;

import com.coma.comaroom.vote.dto.request.CreateNewVoteRequestDto;
import com.coma.comaroom.vote.dto.request.CreateVoteOptionRequestDto;
import com.coma.comaroom.vote.dto.response.CreateNewVoteResponseDto;
import com.coma.comaroom.vote.repository.VoteOptionRepository;
import com.coma.comaroom.vote.repository.VoteRepository;
import com.coma.comaroom.vote.service.VoteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;


import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class VoteServiceTest {

    @Autowired
    private VoteService voteService;

    @Autowired
    VoteOptionRepository voteOptionRepository;

    @Autowired
    VoteRepository voteRepository;

    @Test
    public void createNewVoteTest(){

        voteOptionRepository.deleteAll();
        voteRepository.deleteAll();
        List<CreateVoteOptionRequestDto> createVoteOptionRequestDtos = new ArrayList<>();
        for (int i=0;i<3;i++){
            CreateVoteOptionRequestDto createVoteOptionRequestDto = CreateVoteOptionRequestDto.builder()
                    .content("option " + i)
                    .build();

            createVoteOptionRequestDtos.add(createVoteOptionRequestDto);
        }

        CreateNewVoteRequestDto createNewVoteRequestDto = CreateNewVoteRequestDto.builder()
                .title("title")
                .isMulti(true)
                .options(createVoteOptionRequestDtos)
                .build();


        CreateNewVoteResponseDto createNewVoteResponseDto = voteService.createNewVote(createNewVoteRequestDto);
        System.out.println(createNewVoteResponseDto);

        assertNotNull(voteRepository.findAll());
        assertNotNull(voteOptionRepository.findAll());
    }

}
