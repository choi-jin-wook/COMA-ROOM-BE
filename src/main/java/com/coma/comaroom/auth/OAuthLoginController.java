package com.coma.comaroom.auth;

import com.coma.comaroom.member.dto.response.LoginResponse;
import com.coma.comaroom.utils.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OAuthLoginController {

    private final OAuthLoginCodeService oauthLoginCodeService;

    @PostMapping("/api/auth/oauth/exchange")
    public ResponseEntity<Response<LoginResponse>> exchange(
            @Valid @RequestBody OAuthLoginCodeRequest request
    ) {
        LoginResponse loginResponse = oauthLoginCodeService.exchange(request.loginCode());
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(Response.ok(loginResponse, HttpStatus.OK));
    }
}
