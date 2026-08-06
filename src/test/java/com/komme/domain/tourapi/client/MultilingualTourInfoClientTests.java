package com.komme.domain.tourapi.client;

import com.komme.common.exception.GeneralException;
import com.komme.domain.i18n.enums.Language;
import com.komme.domain.tourapi.cache.TourApiCacheSupport;
import com.komme.domain.tourapi.exception.TourApiErrorStatus;
import com.komme.domain.tourapi.properties.TourApiProperties;

import java.lang.reflect.Field;
import java.util.Map;

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
class MultilingualTourInfoClientTests {

    private static final String DETAIL_COMMON_JSON = """
            {
              "response": {
                "header": {"resultCode":"0000","resultMsg":"OK"},
                "body": {
                  "items": {"item": [{
                    "contentid":"126508",
                    "contenttypeid":"12",
                    "title":"Test Spot",
                    "overview":"Overview text",
                    "homepage":"https://example.com",
                    "tel":"02-1234-5678",
                    "addr1":"Mapo-gu, Seoul"
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

    // 언어별 공통정보 조회 정상 응답 파싱 검증
    @Test
    void findDetailCommonParsesItem() {
        WebClient webClient = createWebClient(HttpStatus.OK, DETAIL_COMMON_JSON);
        MultilingualTourInfoClient client = createClient(webClient, webClient, webClient);

        DetailCommonItem item = client.findDetailCommon(Language.ENGLISH, "126508", "12");

        assertThat(item).isNotNull();
        assertThat(item.title()).isEqualTo("Test Spot");
    }

    // 결과가 비어있으면 null을 반환하는지 검증
    @Test
    void findDetailCommonReturnsNullWhenEmpty() {
        WebClient webClient = createWebClient(HttpStatus.OK, EMPTY_ITEMS_JSON);
        MultilingualTourInfoClient client = createClient(webClient, webClient, webClient);

        DetailCommonItem item = client.findDetailCommon(Language.ENGLISH, "126508", "12");

        assertThat(item).isNull();
    }

    // 연결 실패 시 GeneralException으로 변환되는지 검증
    @Test
    void findDetailCommonMapsConnectionFailure() {
        WebClient webClient = createWebClient(HttpStatus.BAD_GATEWAY, "");
        MultilingualTourInfoClient client = createClient(webClient, webClient, webClient);

        assertThatThrownBy(() -> client.findDetailCommon(Language.ENGLISH, "126508", "12"))
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getErrorStatus())
                .isEqualTo(TourApiErrorStatus.MULTILINGUAL_CONNECTION_FAILED);
    }

    // 언어마다 자기 언어에 해당하는 WebClient로 요청이 가는지 검증 (영/일/중 빈 혼선 없이 라우팅되는지)
    @Test
    void findDetailCommonRoutesToWebClientMatchingLanguage() {
        WebClient englishWebClient = createWebClient(HttpStatus.OK, DETAIL_COMMON_JSON.replace("Test Spot", "EN Spot"));
        WebClient japaneseWebClient = createWebClient(HttpStatus.OK, DETAIL_COMMON_JSON.replace("Test Spot", "JP Spot"));
        WebClient chineseWebClient = createWebClient(HttpStatus.OK, DETAIL_COMMON_JSON.replace("Test Spot", "CN Spot"));
        MultilingualTourInfoClient client = createClient(englishWebClient, japaneseWebClient, chineseWebClient);

        assertThat(client.findDetailCommon(Language.ENGLISH, "1", "12").title()).isEqualTo("EN Spot");
        assertThat(client.findDetailCommon(Language.JAPANESE, "2", "12").title()).isEqualTo("JP Spot");
        assertThat(client.findDetailCommon(Language.CHINESE_SIMPLIFIED, "3", "12").title()).isEqualTo("CN Spot");
    }

    // 아직 WebClient가 등록되지 않은 언어로 호출하면 MULTILINGUAL_RESPONSE_INVALID로 처리되는지 검증
    // (Language에 언어가 추가될 때를 대비한 방어 코드라, 지금 3개 언어만으로는 재현이 안 돼서 리플렉션으로 상황을 흉내낸다)
    @Test
    void findDetailCommonThrowsWhenLanguageHasNoRegisteredWebClient() throws Exception {
        WebClient webClient = createWebClient(HttpStatus.OK, DETAIL_COMMON_JSON);
        MultilingualTourInfoClient client = createClient(webClient, webClient, webClient);

        Field field = MultilingualTourInfoClient.class.getDeclaredField("webClientsByLanguage");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<Language, WebClient> webClientsByLanguage = (Map<Language, WebClient>) field.get(client);
        webClientsByLanguage.remove(Language.ENGLISH);

        assertThatThrownBy(() -> client.findDetailCommon(Language.ENGLISH, "126508", "12"))
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getErrorStatus())
                .isEqualTo(TourApiErrorStatus.MULTILINGUAL_RESPONSE_INVALID);
    }

    private MultilingualTourInfoClient createClient(WebClient englishWebClient, WebClient japaneseWebClient, WebClient chineseWebClient) {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(valueOperations.get(anyString())).thenReturn(null);

        return new MultilingualTourInfoClient(
                englishWebClient,
                japaneseWebClient,
                chineseWebClient,
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
