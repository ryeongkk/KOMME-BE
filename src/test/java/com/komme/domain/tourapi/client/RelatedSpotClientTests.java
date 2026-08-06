package com.komme.domain.tourapi.client;

import com.komme.common.exception.GeneralException;
import com.komme.domain.tourapi.cache.TourApiCacheSupport;
import com.komme.domain.tourapi.exception.TourApiErrorStatus;
import com.komme.domain.tourapi.properties.TourApiProperties;

import java.util.List;

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
class RelatedSpotClientTests {

    private static final String RELATED_SPOT_JSON = """
            {
              "response": {
                "header": {"resultCode":"0000","resultMsg":"OK"},
                "body": {
                  "items": {"item": [{
                    "contentid":"126508",
                    "title":"연관 스팟",
                    "addr1":"서울 마포구",
                    "mapx":"127.1",
                    "mapy":"37.1"
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

    // 지역기반 연관 관광지 조회 정상 응답 파싱 검증
    @Test
    void findByAreaParsesItems() {
        RelatedSpotClient client = createClient(createWebClient(HttpStatus.OK, RELATED_SPOT_JSON));

        List<RelatedSpotItem> items = client.findByArea("11", "11440");

        assertThat(items).hasSize(1);
        assertThat(items.get(0).title()).isEqualTo("연관 스팟");
    }

    // 지역기반 연관 관광지 조회 연결 실패 시 GeneralException으로 변환되는지 검증
    @Test
    void findByAreaMapsConnectionFailure() {
        RelatedSpotClient client = createClient(createWebClient(HttpStatus.BAD_GATEWAY, ""));

        assertThatThrownBy(() -> client.findByArea("11", "11440"))
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getErrorStatus())
                .isEqualTo(TourApiErrorStatus.RELATED_SPOT_CONNECTION_FAILED);
    }

    // 키워드기반 연관 관광지 조회 정상 응답 파싱 검증
    @Test
    void findByKeywordParsesItems() {
        RelatedSpotClient client = createClient(createWebClient(HttpStatus.OK, RELATED_SPOT_JSON));

        List<RelatedSpotItem> items = client.findByKeyword("성수동");

        assertThat(items).hasSize(1);
        assertThat(items.get(0).contentId()).isEqualTo("126508");
    }

    // 키워드기반 연관 관광지 조회 연결 실패 시 GeneralException으로 변환되는지 검증
    @Test
    void findByKeywordMapsConnectionFailure() {
        RelatedSpotClient client = createClient(createWebClient(HttpStatus.BAD_GATEWAY, ""));

        assertThatThrownBy(() -> client.findByKeyword("성수동"))
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getErrorStatus())
                .isEqualTo(TourApiErrorStatus.RELATED_SPOT_CONNECTION_FAILED);
    }

    private RelatedSpotClient createClient(WebClient webClient) {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(valueOperations.get(anyString())).thenReturn(null);

        return new RelatedSpotClient(
                webClient,
                new TourApiQuerySupport(new TourApiProperties("service-key", "ETC", "KOMME")),
                new TourApiCacheSupport(redisTemplate)
        );
    }

    private WebClient createWebClient(HttpStatus status, String body) {
        return WebClient.builder()
                .exchangeFunction(request -> Mono.just(
                        ClientResponse.create(status)
                                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                .body(body)
                                .build()
                ))
                .build();
    }
}
