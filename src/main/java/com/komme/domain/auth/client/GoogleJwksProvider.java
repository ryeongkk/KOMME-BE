package com.komme.domain.auth.client;

import com.komme.domain.auth.exception.AuthErrorStatus;
import com.komme.domain.auth.properties.GoogleProperties;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class GoogleJwksProvider extends AbstractOAuthJwksProvider {

    private static final String GOOGLE_JWKS_PATH = "/oauth2/v3/certs";

    // Google JWKS Provider 생성
    public GoogleJwksProvider(
            @Qualifier("googleApiWebClient") WebClient googleApiWebClient,
            GoogleProperties googleProperties
    ) {
        super(
                googleApiWebClient,
                GOOGLE_JWKS_PATH,
                googleProperties.getJwksCacheTtl(),
                AuthErrorStatus.INVALID_GOOGLE_IDENTITY_TOKEN,
                AuthErrorStatus.GOOGLE_SERVER_CONNECTION_FAILED
        );
    }
}
