package com.komme.common.config;

import java.time.Duration;

import io.netty.channel.ChannelOption;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Configuration
public class WebClientConfig {

    // Apple API WebClient 생성
    @Bean
    public WebClient appleApiWebClient() {
        return createWebClient("https://appleid.apple.com");
    }

    // Google API WebClient 생성
    @Bean
    public WebClient googleApiWebClient() {
        return createWebClient("https://www.googleapis.com");
    }

    // 외부 OAuth API WebClient 생성
    private WebClient createWebClient(String baseUrl) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3_000)
                .responseTimeout(Duration.ofSeconds(5));

        return WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
