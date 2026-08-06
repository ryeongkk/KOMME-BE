package com.komme.domain.tourapi.client;

import com.komme.common.exception.GeneralException;
import com.komme.domain.tourapi.cache.TourApiCacheSupport;
import com.komme.domain.tourapi.exception.TourApiErrorStatus;
import com.komme.domain.tourapi.properties.TourApiProperties;

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
class ConcentrationRateClientTests {

    private static final String CONCENTRATION_RATE_JSON = """
            {
              "response": {
                "header": {"resultCode":"0000","resultMsg":"OK"},
                "body": {
                  "items": {"item": [{
                    "baseYmd":"20260805",
                    "areaCd":"11",
                    "areaNm":"서울특별시",
                    "signguCd":"11440",
                    "signguNm":"마포구",
                    "tAtsNm":"테스트 관광지",
                    "cnctrRate":64.65
                  }]},
                  "numOfRows":1,"pageNo":1,"totalCount":1
                }
              }
            }
            """;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    // 정상 응답 파싱 및 tAtsNm 미지정 시 요청 파라미터에서 제외되는지 검증
    @Test
    void findConcentrationRatesParsesItemsAndOmitsTouristSpotNameWhenNull() {
        AtomicReference<String> requestedUri = new AtomicReference<>();
        WebClient webClient = WebClient.builder()
                .exchangeFunction(request -> {
                    requestedUri.set(request.url().toString());
                    return Mono.just(
                            ClientResponse.create(HttpStatus.OK)
                                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                    .body(CONCENTRATION_RATE_JSON)
                                    .build()
                    );
                })
                .build();
        ConcentrationRateClient client = createClient(webClient);

        List<ConcentrationRateItem> items = client.findConcentrationRates("11", "11440", null);

        assertThat(items).hasSize(1);
        assertThat(items.get(0).touristSpotName()).isEqualTo("테스트 관광지");
        assertThat(items.get(0).concentrationRate()).isEqualTo(64.65);
        assertThat(requestedUri.get()).doesNotContain("tAtsNm");
    }

    // tAtsNm 지정 시 요청 파라미터에 포함되는지 검증
    @Test
    void findConcentrationRatesIncludesTouristSpotNameWhenProvided() {
        AtomicReference<String> requestedUri = new AtomicReference<>();
        WebClient webClient = WebClient.builder()
                .exchangeFunction(request -> {
                    requestedUri.set(request.url().toString());
                    return Mono.just(
                            ClientResponse.create(HttpStatus.OK)
                                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                    .body(CONCENTRATION_RATE_JSON)
                                    .build()
                    );
                })
                .build();
        ConcentrationRateClient client = createClient(webClient);

        client.findConcentrationRates("11", "11440", "테스트 관광지");

        assertThat(requestedUri.get()).contains("tAtsNm");
    }

    // tAtsNm이 null이 아니어도 공백뿐이면 요청 파라미터에서 제외되는지 검증
    @Test
    void findConcentrationRatesOmitsBlankTouristSpotName() {
        AtomicReference<String> requestedUri = new AtomicReference<>();
        WebClient webClient = WebClient.builder()
                .exchangeFunction(request -> {
                    requestedUri.set(request.url().toString());
                    return Mono.just(
                            ClientResponse.create(HttpStatus.OK)
                                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                    .body(CONCENTRATION_RATE_JSON)
                                    .build()
                    );
                })
                .build();
        ConcentrationRateClient client = createClient(webClient);

        client.findConcentrationRates("11", "11440", "   ");

        assertThat(requestedUri.get()).doesNotContain("tAtsNm");
    }

    // 외부 API 연결 실패 시 GeneralException으로 변환되는지 검증
    @Test
    void findConcentrationRatesMapsConnectionFailure() {
        WebClient webClient = WebClient.builder()
                .exchangeFunction(request -> Mono.just(ClientResponse.create(HttpStatus.BAD_GATEWAY).build()))
                .build();
        ConcentrationRateClient client = createClient(webClient);

        assertThatThrownBy(() -> client.findConcentrationRates("11", "11440", null))
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getErrorStatus())
                .isEqualTo(TourApiErrorStatus.CONCENTRATION_RATE_CONNECTION_FAILED);
    }

    private ConcentrationRateClient createClient(WebClient webClient) {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(valueOperations.get(anyString())).thenReturn(null);

        return new ConcentrationRateClient(
                webClient,
                new TourApiQuerySupport(new TourApiProperties("service-key", "ETC", "KOMME")),
                new TourApiCacheSupport(redisTemplate)
        );
    }
}
