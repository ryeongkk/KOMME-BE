package com.komme.domain.auth.service;

import com.komme.domain.auth.client.OAuthAppleClient;
import com.komme.domain.auth.client.OAuthAppleClient.AppleIdentity;
import com.komme.domain.auth.client.OAuthGoogleClient;
import com.komme.domain.auth.client.OAuthGoogleClient.GoogleIdentity;
import com.komme.domain.auth.dto.request.OAuthAppleLoginRequest;
import com.komme.domain.auth.dto.request.OAuthGoogleLoginRequest;
import com.komme.domain.auth.dto.response.LoginResponse;
import com.komme.domain.auth.enums.Provider;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OAuthService {

    private final OAuthAppleClient oAuthAppleClient;
    private final OAuthGoogleClient oAuthGoogleClient;
    private final OAuthAccountService oAuthAccountService;
    private final AuthTokenService authTokenService;

    // Apple identity token 로그인 흐름 조율 기능
    public LoginResponse loginWithApple(OAuthAppleLoginRequest request) {
        AppleIdentity identity = oAuthAppleClient.verifyIdentityToken(request.identityToken());
        Long userId = oAuthAccountService.resolveUser(
                Provider.APPLE,
                identity.subject(),
                identity.email()
        ).getId();
        return authTokenService.issueLoginTokens(userId);
    }

    // Google identity token 로그인 흐름 조율 기능
    public LoginResponse loginWithGoogle(OAuthGoogleLoginRequest request) {
        GoogleIdentity identity = oAuthGoogleClient.verifyIdentityToken(request.idToken());
        Long userId = oAuthAccountService.resolveUser(
                Provider.GOOGLE,
                identity.subject(),
                identity.email()
        ).getId();
        return authTokenService.issueLoginTokens(userId);
    }
}
