package com.komme.domain.course.client;

import com.komme.common.exception.GeneralException;
import com.komme.domain.course.properties.KakaoLocalProperties;
import com.komme.domain.tourapi.cache.TourApiCacheSupport;
import com.komme.domain.tourapi.exception.TourApiErrorStatus;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class KakaoLocalClientTests {

    private static final String SEARCH_RESPONSE_JSON = """
            {
              "documents": [{
                "place_name":"성수동 카페",
                "address_name":"서울 성동구 성수동2가",
                "road_address_name":"서울 성동구 성수이로 1",
                "category_name":"음식점 > 카페",
                "category_group_code":"CE7",
                "x":"127.055",
                "y":"37.544"
              }],
              "meta": {"total_count":1,"pageable_count":1,"is_end":true}
            }
            """;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    // 정상 응답 파싱 및 Authorization 헤더가 KakaoAK 형식으로 전송되는지 검증
    @Test
    void searchByKeywordParsesItemsAndSendsAuthorizationHeader() {
        AtomicReference<String> authorizationHeader = new AtomicReference<>();
        WebClient webClient = WebClient.builder()
                .exchangeFunction(request -> {
                    authorizationHeader.set(request.headers().getFirst(HttpHeaders.AUTHORIZATION));
                    return Mono.just(
                            ClientResponse.create(HttpStatus.OK)
                                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                    .body(SEARCH_RESPONSE_JSON)
                                    .build()
                    );
                })
                .build();
        KakaoLocalClient client = createClient(webClient);

        List<KakaoPlaceDocument> documents = client.searchByKeyword("성수동");

        assertThat(documents).hasSize(1);
        assertThat(documents.get(0).placeName()).isEqualTo("성수동 카페");
        assertThat(authorizationHeader.get()).isEqualTo("KakaoAK kakao-rest-api-key");
    }

    // 외부 API 연결 실패 시 GeneralException으로 변환되는지 검증
    @Test
    void searchByKeywordMapsConnectionFailure() {
        WebClient webClient = WebClient.builder()
                .exchangeFunction(request -> Mono.just(ClientResponse.create(HttpStatus.BAD_GATEWAY).build()))
                .build();
        KakaoLocalClient client = createClient(webClient);

        assertThatThrownBy(() -> client.searchByKeyword("성수동"))
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getErrorStatus())
                .isEqualTo(TourApiErrorStatus.KAKAO_LOCAL_CONNECTION_FAILED);
    }

    private KakaoLocalClient createClient(WebClient webClient) {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(valueOperations.get(anyString())).thenReturn(null);

        return new KakaoLocalClient(
                webClient,
                new KakaoLocalProperties("kakao-rest-api-key"),
                new TourApiCacheSupport(redisTemplate)
        );
    }
}
