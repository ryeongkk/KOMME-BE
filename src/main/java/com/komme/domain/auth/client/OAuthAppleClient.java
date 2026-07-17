package com.komme.domain.auth.client;

import com.komme.domain.auth.exception.AuthErrorStatus;
import com.komme.domain.auth.properties.AppleProperties;

import java.util.Set;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OAuthAppleClient {

    private static final String APPLE_ISSUER = "https://appleid.apple.com";

    private final AppleJwksProvider appleJwksProvider;
    private final AppleProperties appleProperties;
    private final OAuthIdentityTokenVerifier identityTokenVerifier;

    // Apple identity token 검증 및 사용자 정보 조회 기능
    public OAuthIdentity verifyIdentityToken(String identityToken) {
        return identityTokenVerifier.verify(
                identityToken,
                appleJwksProvider,
                appleProperties.getClientId(),
                Set.of(APPLE_ISSUER),
                AuthErrorStatus.INVALID_APPLE_IDENTITY_TOKEN
        );
    }
}
