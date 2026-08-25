package com.komme.common.config;

import java.time.Duration;

import io.netty.channel.ChannelOption;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import reactor.netty.http.client.HttpClient;

@Configuration
public class WebClientConfig {

    // Discord Webhook WebClient 생성
    @Bean
    public WebClient discordAlertWebClient() {
        return createWebClient();
    }

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

    // Google OAuth 토큰 교환 WebClient 생성
    @Bean
    public WebClient googleOAuthWebClient() {
        return createWebClient("https://oauth2.googleapis.com");
    }

    // 한국관광공사 국문 관광정보 서비스(KorService2) WebClient 생성
    // 관광공사 서비스키가 base64(+, /, = 포함)라서 Spring의 자동 URI 인코딩이 '+'를 안전한 문자로 보고
    // 인코딩하지 않는 문제가 있다(서버는 '+'를 공백으로 해석해 SERVICE_KEY_IS_NOT_REGISTERED_ERROR 발생).
    // 그래서 관광공사 WebClient들은 인코딩을 아예 안 하는 모드로 만들고, 값은 TourApiQuerySupport/호출부에서 직접 인코딩한다.
    @Bean
    public WebClient korServiceApiWebClient() {
        return createTourApiWebClient("http://apis.data.go.kr/B551011/KorService2");
    }

    // 한국관광공사 관광지별 연관 관광지 서비스 WebClient 생성
    @Bean
    public WebClient relatedSpotApiWebClient() {
        return createTourApiWebClient("http://apis.data.go.kr/B551011/TarRlteTarService1");
    }

    // 한국관광공사 관광지 집중률 방문자 추이 예측 정보 서비스 WebClient 생성
    @Bean
    public WebClient concentrationRateApiWebClient() {
        return createTourApiWebClient("http://apis.data.go.kr/B551011/TatsCnctrRateService");
    }

    // 카카오 로컬 API WebClient 생성
    // 카카오 REST API 키는 hex 문자열이고 쿼리파라미터가 아니라 Authorization 헤더로 보내서 위 인코딩 문제와 무관하다.
    @Bean
    public WebClient kakaoLocalApiWebClient() {
        return createWebClient("https://dapi.kakao.com");
    }

    // 한국관광공사 영문 관광정보서비스 WebClient 생성
    // ⚠ base URL이 KorService2 네이밍 관례(EngService2) 추정치다 - data.go.kr Swagger로 실제 경로 확인 필요
    @Bean
    public WebClient engServiceApiWebClient() {
        return createTourApiWebClient("http://apis.data.go.kr/B551011/EngService2");
    }

    // 한국관광공사 일문 관광정보서비스 WebClient 생성 (⚠ base URL 추정치, 위와 동일)
    @Bean
    public WebClient jpnServiceApiWebClient() {
        return createTourApiWebClient("http://apis.data.go.kr/B551011/JpnService2");
    }

    // 한국관광공사 중문 간체 관광정보서비스 WebClient 생성 (⚠ base URL 추정치, 위와 동일)
    @Bean
    public WebClient chsServiceApiWebClient() {
        return createTourApiWebClient("http://apis.data.go.kr/B551011/ChsService2");
    }

    // 외부 OAuth/카카오 API WebClient 생성 (기본 URI 인코딩 사용)
    private WebClient createWebClient(String baseUrl) {
        return webClientBuilder()
                .baseUrl(baseUrl)
                .build();
    }

    // 기본 URI 인코딩 WebClient 생성
    private WebClient createWebClient() {
        return webClientBuilder()
                .build();
    }

    // 관광공사 API WebClient 생성 - URI 자동 인코딩을 끄고, 값은 호출부가 직접 인코딩해서 넘긴다는 전제
    private WebClient createTourApiWebClient(String baseUrl) {
        DefaultUriBuilderFactory uriBuilderFactory = new DefaultUriBuilderFactory(baseUrl);
        uriBuilderFactory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE);

        return webClientBuilder()
                .uriBuilderFactory(uriBuilderFactory)
                .build();
    }

    // 공통 WebClient Builder 생성
    private WebClient.Builder webClientBuilder() {
        return WebClient.builder()
                .clientConnector(clientConnector());
    }

    // 연결/응답 타임아웃이 설정된 공통 HTTP 커넥터 생성 기능
    private ReactorClientHttpConnector clientConnector() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3_000)
                .responseTimeout(Duration.ofSeconds(5));
        return new ReactorClientHttpConnector(httpClient);
    }
}
