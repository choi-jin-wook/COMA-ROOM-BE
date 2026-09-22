package com.coma.comaroom.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NaverUserResponse(
        String resultcode,
        String message,
        Response response
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Response(
            String id,
            String name,
            String email,
            String mobile
    ) {
    }
}
