package com.coma.comaroom;

import com.coma.comaroom.vote.dto.AddVoteOptionRequestDto;
import com.coma.comaroom.vote.dto.request.CreateNewVoteRequestDto;
import com.coma.comaroom.vote.dto.request.CreateVoteOptionRequestDto;
import com.coma.comaroom.vote.dto.response.VoteDetailResponseDto;
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
        for (int i=0;i<7;i++){
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


        VoteDetailResponseDto createNewVoteResponseDto = voteService.createNewVote(createNewVoteRequestDto);
        System.out.println(createNewVoteResponseDto);

        assertNotNull(voteRepository.findAll());
        assertNotNull(voteOptionRepository.findAll());


        voteOptionRepository.deleteAll();
        voteRepository.deleteAll();
    }


    @Test
    void addVoteOption() {

        voteOptionRepository.deleteAll();
        voteRepository.deleteAll();
        List<CreateVoteOptionRequestDto> createVoteOptionRequestDtos = new ArrayList<>();
        for (int i=0;i<7;i++){
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


        VoteDetailResponseDto createNewVoteResponseDto = voteService.createNewVote(createNewVoteRequestDto);

        AddVoteOptionRequestDto addVoteOptionRequestDto = AddVoteOptionRequestDto.builder()
                .voteId(createNewVoteResponseDto.getVoteId())
                .content("for test")
                .build();


        VoteDetailResponseDto voteDetailResponseDto = voteService.addVoteOption(addVoteOptionRequestDto);

        System.out.println(voteDetailResponseDto);

        assertNotEquals(createNewVoteRequestDto.getOptions().size(), voteOptionRepository.findAll().size());
    }
}
