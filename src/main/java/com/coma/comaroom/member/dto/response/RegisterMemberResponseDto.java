package com.coma.comaroom.member.dto.response;

import com.coma.comaroom.member.dto.request.RegisterMemberRequestDto;
import com.coma.comaroom.member.entity.Major;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RegisterMemberResponseDto {
    private String studentId;
    private String name;
    private Major major;

    public static RegisterMemberResponseDto from(RegisterMemberRequestDto request) {
        return new RegisterMemberResponseDto(request.getStudentId(), request.getName(), request.getMajor());
    }
}
