package com.coma.comaroom.member.dto.response;

import com.coma.comaroom.member.entity.Role;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private String message;
    private Role role;
}