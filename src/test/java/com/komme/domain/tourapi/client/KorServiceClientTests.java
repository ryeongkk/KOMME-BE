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
class KorServiceClientTests {

    private static final String AREA_BASED_LIST_JSON = """
            {
              "response": {
                "header": {"resultCode":"0000","resultMsg":"OK"},
                "body": {
                  "items": {"item": [{
                    "contentid":"126508",
                    "contenttypeid":"12",
                    "title":"테스트 스팟",
                    "addr1":"서울 마포구",
                    "areacode":"11",
                    "sigungucode":"11440",
                    "cat1":"A01",
                    "cat2":"A0101",
                    "cat3":"A01010100",
                    "mapx":"127.1",
                    "mapy":"37.1",
                    "firstimage":"img1",
                    "firstimage2":"img2"
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

    // 지역기반 목록조회 정상 응답 파싱 검증
    @Test
    void findAreaBasedListParsesItems() {
        KorServiceClient client = createClient(createWebClient(HttpStatus.OK, AREA_BASED_LIST_JSON));

        List<AreaBasedListItem> items = client.findAreaBasedList("11", "11440", "12");

        assertThat(items).hasSize(1);
        assertThat(items.get(0).contentId()).isEqualTo("126508");
        assertThat(items.get(0).title()).isEqualTo("테스트 스팟");
    }

    // 외부 API 연결 실패(비정상 HTTP 상태) 시 GeneralException으로 변환되는지 검증
    @Test
    void findAreaBasedListMapsConnectionFailure() {
        KorServiceClient client = createClient(createWebClient(HttpStatus.BAD_GATEWAY, ""));

        assertThatThrownBy(() -> client.findAreaBasedList("11", "11440", "12"))
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getErrorStatus())
                .isEqualTo(TourApiErrorStatus.KOR_SERVICE_CONNECTION_FAILED);
    }

    private KorServiceClient createClient(WebClient webClient) {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(valueOperations.get(anyString())).thenReturn(null);

        return new KorServiceClient(
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
