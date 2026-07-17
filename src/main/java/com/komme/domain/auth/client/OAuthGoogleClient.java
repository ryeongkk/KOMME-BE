package com.komme.domain.auth.client;

import com.komme.domain.auth.exception.AuthErrorStatus;
import com.komme.domain.auth.properties.GoogleProperties;

import java.util.Set;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OAuthGoogleClient {

    private static final String GOOGLE_ISSUER = "https://accounts.google.com";
    private static final String LEGACY_GOOGLE_ISSUER = "accounts.google.com";

    private final GoogleJwksProvider googleJwksProvider;
    private final GoogleProperties googleProperties;
    private final OAuthIdentityTokenVerifier identityTokenVerifier;

    // Google identity token 검증 및 사용자 정보 조회 기능
    public OAuthIdentity verifyIdentityToken(String identityToken) {
        return identityTokenVerifier.verify(
                identityToken,
                googleJwksProvider,
                googleProperties.getClientId(),
                Set.of(GOOGLE_ISSUER, LEGACY_GOOGLE_ISSUER),
                AuthErrorStatus.INVALID_GOOGLE_IDENTITY_TOKEN
        );
    }
}
