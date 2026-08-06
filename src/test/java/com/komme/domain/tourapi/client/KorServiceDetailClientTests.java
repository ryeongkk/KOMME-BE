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
class KorServiceDetailClientTests {

    private static final String DETAIL_COMMON_JSON = """
            {
              "response": {
                "header": {"resultCode":"0000","resultMsg":"OK"},
                "body": {
                  "items": {"item": [{
                    "contentid":"126508",
                    "contenttypeid":"12",
                    "title":"테스트 스팟",
                    "overview":"소개 문구",
                    "homepage":"https://example.com",
                    "tel":"02-1234-5678",
                    "addr1":"서울 마포구"
                  }]},
                  "numOfRows":1,"pageNo":1,"totalCount":1
                }
              }
            }
            """;

    private static final String DETAIL_INTRO_JSON = """
            {
              "response": {
                "header": {"resultCode":"0000","resultMsg":"OK"},
                "body": {
                  "items": {"item": [{
                    "contentid":"126508",
                    "contenttypeid":"12",
                    "usetime":"09:00~18:00",
                    "restdate":"매주 월요일",
                    "infocenter":"02-1234-5678"
                  }]},
                  "numOfRows":1,"pageNo":1,"totalCount":1
                }
              }
            }
            """;

    private static final String DETAIL_IMAGE_JSON = """
            {
              "response": {
                "header": {"resultCode":"0000","resultMsg":"OK"},
                "body": {
                  "items": {"item": [{
                    "contentid":"126508",
                    "originimgurl":"https://example.com/full.jpg",
                    "smallimageurl":"https://example.com/thumb.jpg"
                  }]},
                  "numOfRows":1,"pageNo":1,"totalCount":1
                }
              }
            }
            """;

    // 공공데이터포털 API는 결과가 없을 때 items를 null로 내려준다(빈 배열이 아님)
    private static final String EMPTY_ITEMS_JSON = """
            {
              "response": {
                "header": {"resultCode":"0000","resultMsg":"OK"},
                "body": {
                  "items": null,
                  "numOfRows":0,"pageNo":1,"totalCount":0
                }
              }
            }
            """;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    // 공통정보 조회 정상 응답 파싱 검증
    @Test
    void findDetailCommonParsesItem() {
        KorServiceDetailClient client = createClient(createWebClient(HttpStatus.OK, DETAIL_COMMON_JSON));

        DetailCommonItem item = client.findDetailCommon("126508", "12");

        assertThat(item).isNotNull();
        assertThat(item.title()).isEqualTo("테스트 스팟");
        assertThat(item.overview()).isEqualTo("소개 문구");
    }

    // 공통정보 조회 결과가 비어있으면 null을 반환하는지 검증
    @Test
    void findDetailCommonReturnsNullWhenEmpty() {
        KorServiceDetailClient client = createClient(createWebClient(HttpStatus.OK, EMPTY_ITEMS_JSON));

        DetailCommonItem item = client.findDetailCommon("126508", "12");

        assertThat(item).isNull();
    }

    // 공통정보 조회 연결 실패 시 GeneralException으로 변환되는지 검증
    @Test
    void findDetailCommonMapsConnectionFailure() {
        KorServiceDetailClient client = createClient(createWebClient(HttpStatus.BAD_GATEWAY, ""));

        assertThatThrownBy(() -> client.findDetailCommon("126508", "12"))
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getErrorStatus())
                .isEqualTo(TourApiErrorStatus.KOR_SERVICE_CONNECTION_FAILED);
    }

    // 소개정보 조회 정상 응답 파싱 검증
    @Test
    void findDetailIntroParsesItem() {
        KorServiceDetailClient client = createClient(createWebClient(HttpStatus.OK, DETAIL_INTRO_JSON));

        DetailIntroItem item = client.findDetailIntro("126508", "12");

        assertThat(item).isNotNull();
        assertThat(item.useTime()).isEqualTo("09:00~18:00");
        assertThat(item.restDate()).isEqualTo("매주 월요일");
    }

    // 소개정보 조회 결과가 비어있으면 null을 반환하는지 검증
    @Test
    void findDetailIntroReturnsNullWhenEmpty() {
        KorServiceDetailClient client = createClient(createWebClient(HttpStatus.OK, EMPTY_ITEMS_JSON));

        DetailIntroItem item = client.findDetailIntro("126508", "12");

        assertThat(item).isNull();
    }

    // 소개정보 조회 연결 실패 시 GeneralException으로 변환되는지 검증
    @Test
    void findDetailIntroMapsConnectionFailure() {
        KorServiceDetailClient client = createClient(createWebClient(HttpStatus.BAD_GATEWAY, ""));

        assertThatThrownBy(() -> client.findDetailIntro("126508", "12"))
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getErrorStatus())
                .isEqualTo(TourApiErrorStatus.KOR_SERVICE_CONNECTION_FAILED);
    }

    // 이미지정보 조회 정상 응답 파싱 검증
    @Test
    void findDetailImagesParsesItems() {
        KorServiceDetailClient client = createClient(createWebClient(HttpStatus.OK, DETAIL_IMAGE_JSON));

        List<DetailImageItem> items = client.findDetailImages("126508");

        assertThat(items).hasSize(1);
        assertThat(items.get(0).originImageUrl()).isEqualTo("https://example.com/full.jpg");
        assertThat(items.get(0).thumbnailImageUrl()).isEqualTo("https://example.com/thumb.jpg");
    }

    // 이미지정보 조회 연결 실패 시 GeneralException으로 변환되는지 검증
    @Test
    void findDetailImagesMapsConnectionFailure() {
        KorServiceDetailClient client = createClient(createWebClient(HttpStatus.BAD_GATEWAY, ""));

        assertThatThrownBy(() -> client.findDetailImages("126508"))
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getErrorStatus())
                .isEqualTo(TourApiErrorStatus.KOR_SERVICE_CONNECTION_FAILED);
    }

    private KorServiceDetailClient createClient(WebClient webClient) {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(valueOperations.get(anyString())).thenReturn(null);

        return new KorServiceDetailClient(
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
