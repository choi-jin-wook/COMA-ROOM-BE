package com.coma.comaroom.auth;

import jakarta.validation.constraints.NotBlank;

public record OAuthLoginCodeRequest(
        @NotBlank String loginCode
) {
}
