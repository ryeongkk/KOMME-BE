package com.komme.domain.auth.client;

import com.komme.domain.auth.exception.AuthErrorStatus;
import com.komme.domain.auth.properties.AppleProperties;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class AppleJwksProvider extends AbstractOAuthJwksProvider {

    private static final String APPLE_JWKS_PATH = "/auth/keys";

    // Apple JWKS Provider 생성
    public AppleJwksProvider(
            @Qualifier("appleApiWebClient") WebClient appleApiWebClient,
            AppleProperties appleProperties
    ) {
        super(
                appleApiWebClient,
                APPLE_JWKS_PATH,
                appleProperties.getJwksCacheTtl(),
                AuthErrorStatus.INVALID_APPLE_IDENTITY_TOKEN,
                AuthErrorStatus.APPLE_SERVER_CONNECTION_FAILED
        );
    }
}
