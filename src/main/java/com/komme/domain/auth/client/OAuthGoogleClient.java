package com.komme.domain.auth.client;

import com.komme.common.exception.GeneralException;
import com.komme.domain.auth.exception.AuthErrorStatus;
import com.komme.domain.auth.properties.GoogleProperties;

import java.util.Set;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;

import reactor.core.publisher.Mono;

@Component
public class OAuthGoogleClient {

    private static final String GOOGLE_ISSUER = "https://accounts.google.com";
    private static final String LEGACY_GOOGLE_ISSUER = "accounts.google.com";
    private static final String AUTHORIZATION_CODE_GRANT_TYPE = "authorization_code";

    private final WebClient googleOAuthWebClient;
    private final GoogleJwksProvider googleJwksProvider;
    private final GoogleProperties googleProperties;
    private final OAuthIdentityTokenVerifier identityTokenVerifier;

    // Google OAuth 클라이언트 생성
    public OAuthGoogleClient(
            @Qualifier("googleOAuthWebClient") WebClient googleOAuthWebClient,
            GoogleJwksProvider googleJwksProvider,
            GoogleProperties googleProperties,
            OAuthIdentityTokenVerifier identityTokenVerifier
    ) {
        this.googleOAuthWebClient = googleOAuthWebClient;
        this.googleJwksProvider = googleJwksProvider;
        this.googleProperties = googleProperties;
        this.identityTokenVerifier = identityTokenVerifier;
    }

    // Google authorization code 교환 및 사용자 정보 조회 기능
    public OAuthIdentity verifyAuthorizationCode(String code) {
        GoogleTokenResponse tokenResponse = exchangeAuthorizationCode(code);
        validateTokenResponse(tokenResponse);
        return identityTokenVerifier.verify(
                tokenResponse.idToken(),
                googleJwksProvider,
                googleProperties.getClientId(),
                Set.of(GOOGLE_ISSUER, LEGACY_GOOGLE_ISSUER),
                AuthErrorStatus.INVALID_GOOGLE_IDENTITY_TOKEN
        );
    }

    // Google authorization code 토큰 교환 기능
    private GoogleTokenResponse exchangeAuthorizationCode(String code) {
        try {
            return googleOAuthWebClient.post()
                    .uri("/token")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData("code", code)
                            .with("client_id", googleProperties.getClientId())
                            .with("client_secret", googleProperties.getClientSecret())
                            .with("redirect_uri", googleProperties.getRedirectUri())
                            .with("grant_type", AUTHORIZATION_CODE_GRANT_TYPE))
                    .retrieve()
                    .onStatus(
                            HttpStatusCode::is4xxClientError,
                            response -> response.bodyToMono(GoogleTokenErrorResponse.class)
                                    .defaultIfEmpty(new GoogleTokenErrorResponse(null))
                                    .map(this::mapClientError)
                                    .flatMap(Mono::error)
                    )
                    .onStatus(HttpStatusCode::is5xxServerError, response -> Mono.error(
                            new GeneralException(AuthErrorStatus.GOOGLE_SERVER_CONNECTION_FAILED)
                    ))
                    .bodyToMono(GoogleTokenResponse.class)
                    .block();
        } catch (GeneralException exception) {
            throw exception;
        } catch (WebClientException exception) {
            throw new GeneralException(
                    AuthErrorStatus.GOOGLE_SERVER_CONNECTION_FAILED,
                    exception
            );
        }
    }

    // Google 토큰 응답 필수값 검증 기능
    private void validateTokenResponse(GoogleTokenResponse tokenResponse) {
        if (tokenResponse == null || tokenResponse.idToken() == null
                || tokenResponse.idToken().isBlank()) {
            throw new GeneralException(AuthErrorStatus.GOOGLE_SERVER_CONNECTION_FAILED);
        }
    }

    // Google 토큰 교환 4xx 오류 매핑 기능
    private GeneralException mapClientError(GoogleTokenErrorResponse errorResponse) {
        String error = errorResponse.error();
        if ("invalid_client".equals(error) || "unauthorized_client".equals(error)) {
            return new GeneralException(AuthErrorStatus.GOOGLE_SERVER_CONNECTION_FAILED);
        }
        return new GeneralException(AuthErrorStatus.INVALID_GOOGLE_AUTH_CODE);
    }
}
