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

    // 한국관광공사 국문 관광정보 서비스(KorService2) WebClient 생성
    @Bean
    public WebClient korServiceApiWebClient() {
        return createWebClient("http://apis.data.go.kr/B551011/KorService2");
    }

    // 한국관광공사 관광지별 연관 관광지 서비스 WebClient 생성
    @Bean
    public WebClient relatedSpotApiWebClient() {
        return createWebClient("http://apis.data.go.kr/B551011/TarRlteTarService1");
    }

    // 한국관광공사 관광지 집중률 방문자 추이 예측 정보 서비스 WebClient 생성
    @Bean
    public WebClient concentrationRateApiWebClient() {
        return createWebClient("http://apis.data.go.kr/B551011/TatsCnctrRateService");
    }

    // 카카오 로컬 API WebClient 생성
    @Bean
    public WebClient kakaoLocalApiWebClient() {
        return createWebClient("https://dapi.kakao.com");
    }

    // 외부 OAuth/관광공사/카카오 API WebClient 생성
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
