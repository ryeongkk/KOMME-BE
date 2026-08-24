package com.komme.common.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;

class WebClientConfigTests {

    private final WebClientConfig webClientConfig = new WebClientConfig();

    // 기본 WebClient Builder Bean 생성 검증
    @Test
    void webClientBuilderCreatesBuilder() {
        WebClient.Builder webClientBuilder = webClientConfig.webClientBuilder();

        assertThat(webClientBuilder).isNotNull();
    }

    // Apple API WebClient Bean 생성 검증
    @Test
    void appleApiWebClientCreatesWebClient() {
        WebClient webClient = webClientConfig.appleApiWebClient();

        assertThat(webClient).isNotNull();
    }

    // Google API WebClient Bean 생성 검증
    @Test
    void googleApiWebClientCreatesWebClient() {
        WebClient webClient = webClientConfig.googleApiWebClient();

        assertThat(webClient).isNotNull();
    }

    // OAuth API WebClient Bean 분리 생성 검증
    @Test
    void oAuthApiWebClientsAreCreatedSeparately() {
        WebClient appleWebClient = webClientConfig.appleApiWebClient();
        WebClient googleWebClient = webClientConfig.googleApiWebClient();

        assertThat(appleWebClient).isNotSameAs(googleWebClient);
    }
}
