package com.komme.domain.auth.client;

import com.komme.common.exception.GeneralException;
import com.komme.domain.auth.exception.AuthErrorStatus;
import com.komme.domain.auth.properties.GoogleProperties;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GoogleJwksProviderTests {

    private static final String KEY_ID = "google-key-id";
    private static final String JWKS_JSON = """
            {
              "keys": [
                {
                  "kty": "RSA",
                  "kid": "google-key-id",
                  "use": "sig",
                  "alg": "RS256",
                  "n": "modulus",
                  "e": "AQAB"
                }
              ]
            }
            """;

    // Google JWKS TTL 캐시 재사용 검증
    @Test
    void getSigningJwkReusesCachedJwks() {
        AtomicInteger requestCount = new AtomicInteger();
        GoogleJwksProvider provider = createProvider(createSuccessWebClient(requestCount));

        OAuthJwk firstKey = provider.getSigningJwk(KEY_ID);
        OAuthJwk secondKey = provider.getSigningJwk(KEY_ID);

        assertThat(firstKey).isEqualTo(secondKey);
        assertThat(requestCount).hasValue(1);
    }

    // Google JWKS 서버 연결 실패 오류 변환 검증
    @Test
    void getSigningJwkMapsGoogleServerFailure() {
        WebClient webClient = WebClient.builder()
                .exchangeFunction(request -> Mono.just(
                        ClientResponse.create(HttpStatus.BAD_GATEWAY).build()
                ))
                .build();
        GoogleJwksProvider provider = createProvider(webClient);

        assertThatThrownBy(() -> provider.getSigningJwk(KEY_ID))
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getErrorStatus())
                .isEqualTo(AuthErrorStatus.GOOGLE_SERVER_CONNECTION_FAILED);
    }

    // 캐시에 다른 keyId가 요청되면 캐시를 다시 조회하고, 갱신 후에도 못 찾으면 INVALID_TOKEN으로 처리되는지 검증
    @Test
    void getSigningJwkRefetchesAndThrowsWhenKeyIdNotInCacheOrRefreshedJwks() {
        AtomicInteger requestCount = new AtomicInteger();
        GoogleJwksProvider provider = createProvider(createSuccessWebClient(requestCount));
        provider.getSigningJwk(KEY_ID); // 캐시 채우기

        assertThatThrownBy(() -> provider.getSigningJwk("other-key-id"))
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getErrorStatus())
                .isEqualTo(AuthErrorStatus.INVALID_GOOGLE_IDENTITY_TOKEN);
        assertThat(requestCount).hasValue(2); // 캐시에 없어서 다시 조회했어야 한다
    }

    // 캐시 TTL이 지나면 캐시를 재사용하지 않고 다시 조회하는지 검증
    @Test
    void getSigningJwkRefetchesAfterCacheExpires() throws InterruptedException {
        AtomicInteger requestCount = new AtomicInteger();
        GoogleJwksProvider provider = createProvider(createSuccessWebClient(requestCount), Duration.ofMillis(1));

        provider.getSigningJwk(KEY_ID);
        Thread.sleep(10); // TTL(1ms)이 확실히 지나도록 최소 지연
        provider.getSigningJwk(KEY_ID);

        assertThat(requestCount).hasValue(2);
    }

    // 응답 자체에 바디가 없으면(response == null) 서버 연결 실패로 처리되는지 검증
    @Test
    void getSigningJwkThrowsWhenResponseHasNoBody() {
        WebClient webClient = WebClient.builder()
                .exchangeFunction(request -> Mono.just(ClientResponse.create(HttpStatus.NO_CONTENT).build()))
                .build();
        GoogleJwksProvider provider = createProvider(webClient);

        assertThatThrownBy(() -> provider.getSigningJwk(KEY_ID))
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getErrorStatus())
                .isEqualTo(AuthErrorStatus.GOOGLE_SERVER_CONNECTION_FAILED);
    }

    // 응답 바디가 비어있거나 깨진 JSON이라 디코딩 자체가 실패해도 서버 연결 실패로 처리되는지 검증 (DecodingException)
    @Test
    void getSigningJwkThrowsWhenResponseBodyFailsToDecode() {
        GoogleJwksProvider provider = createProvider(createWebClient(""));

        assertThatThrownBy(() -> provider.getSigningJwk(KEY_ID))
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getErrorStatus())
                .isEqualTo(AuthErrorStatus.GOOGLE_SERVER_CONNECTION_FAILED);
    }

    // keys 필드 자체가 없으면(null) 서버 연결 실패로 처리되는지 검증
    @Test
    void getSigningJwkThrowsWhenKeysFieldIsMissing() {
        GoogleJwksProvider provider = createProvider(createWebClient("{}"));

        assertThatThrownBy(() -> provider.getSigningJwk(KEY_ID))
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getErrorStatus())
                .isEqualTo(AuthErrorStatus.GOOGLE_SERVER_CONNECTION_FAILED);
    }

    // keys 배열이 비어있으면 서버 연결 실패로 처리되는지 검증
    @Test
    void getSigningJwkThrowsWhenKeysFieldIsEmpty() {
        GoogleJwksProvider provider = createProvider(createWebClient("{\"keys\": []}"));

        assertThatThrownBy(() -> provider.getSigningJwk(KEY_ID))
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getErrorStatus())
                .isEqualTo(AuthErrorStatus.GOOGLE_SERVER_CONNECTION_FAILED);
    }

    // keyId가 null이면(예: JWT 헤더에 kid 클레임이 없는 경우) 매칭되는 키가 없어 INVALID_TOKEN으로 처리되는지 검증
    @Test
    void getSigningJwkThrowsWhenKeyIdIsNull() {
        GoogleJwksProvider provider = createProvider(createSuccessWebClient(new AtomicInteger()));

        assertThatThrownBy(() -> provider.getSigningJwk(null))
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getErrorStatus())
                .isEqualTo(AuthErrorStatus.INVALID_GOOGLE_IDENTITY_TOKEN);
    }

    // 테스트 Google JWKS Provider 생성 (기본 TTL 1시간)
    private GoogleJwksProvider createProvider(WebClient webClient) {
        return createProvider(webClient, Duration.ofHours(1));
    }

    // 테스트 Google JWKS Provider 생성 (TTL 지정)
    private GoogleJwksProvider createProvider(WebClient webClient, Duration cacheTtl) {
        return new GoogleJwksProvider(
                webClient,
                new GoogleProperties(
                        "google-client-id",
                        "google-client-secret",
                        cacheTtl
                )
        );
    }

    // 정상 Google JWKS 응답 WebClient 생성
    private WebClient createSuccessWebClient(AtomicInteger requestCount) {
        return WebClient.builder()
                .exchangeFunction(request -> {
                    requestCount.incrementAndGet();
                    ClientResponse response = ClientResponse.create(HttpStatus.OK)
                            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .body(JWKS_JSON)
                            .build();
                    return Mono.just(response);
                })
                .build();
    }

    // 임의의 응답 바디를 주는 WebClient 생성
    private WebClient createWebClient(String body) {
        return WebClient.builder()
                .exchangeFunction(request -> Mono.just(
                        ClientResponse.create(HttpStatus.OK)
                                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                .body(body)
                                .build()
                ))
                .build();
    }
}
