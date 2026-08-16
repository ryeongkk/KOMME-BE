package com.komme.domain.tourapi.client;

import java.net.URI;
import java.util.List;

import com.komme.common.exception.GeneralException;
import com.komme.domain.tourapi.exception.TourApiErrorStatus;
import com.komme.domain.tourapi.properties.TourApiProperties;

import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TourApiQuerySupportTests {

    private final TourApiQuerySupport support = new TourApiQuerySupport(
            new TourApiProperties("service-key", "ETC", "KOMME")
    );

    // 공통 파라미터가 정확히 부착되는지 검증
    @Test
    void withCommonParamsAttachesServiceKeyMobileOsMobileAppAndType() {
        UriBuilder uriBuilder = new DefaultUriBuilderFactory().uriString("http://example.com");

        String uri = support.withCommonParams(uriBuilder).build().toString();

        assertThat(uri)
                .contains("serviceKey=service-key")
                .contains("MobileOS=ETC")
                .contains("MobileApp=KOMME")
                .contains("_type=json");
    }

    // 서비스키에 '+'가 있어도(base64) 인코딩 없음 모드(WebClientConfig의 관광공사 WebClient 설정)에서
    // 공백으로 깨지지 않고 %2B로 살아남는지 검증 - SERVICE_KEY_IS_NOT_REGISTERED_ERROR 재발 방지
    @Test
    void withCommonParamsEncodesServiceKeyContainingPlusForNoEncodingWebClient() {
        TourApiQuerySupport supportWithPlusKey = new TourApiQuerySupport(
                new TourApiProperties("jWlN+WMj+abcd==", "ETC", "KOMME")
        );
        DefaultUriBuilderFactory noEncodingFactory = new DefaultUriBuilderFactory("http://example.com");
        noEncodingFactory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE);
        UriBuilder uriBuilder = noEncodingFactory.uriString("");

        String rawQuery = supportWithPlusKey.withCommonParams(uriBuilder).build().getRawQuery();

        assertThat(rawQuery).contains("serviceKey=jWlN%2BWMj%2Babcd%3D%3D");
    }

    // 쿼리파라미터 값 인코딩 기능이 특수문자를 UTF-8로 퍼센트 인코딩하는지 검증
    @Test
    void encodeUrlEncodesValue() {
        assertThat(support.encode("jWlN+WMj/abcd==")).isEqualTo("jWlN%2BWMj%2Fabcd%3D%3D");
        assertThat(support.encode("강남역")).isEqualTo("%EA%B0%95%EB%82%A8%EC%97%AD");
    }

    // 정상 응답에서 item 목록을 그대로 반환하는지 검증
    @Test
    void unwrapItemsReturnsItemsOnSuccess() {
        TourApiEnvelope<String> envelope = new TourApiEnvelope<>(
                new TourApiEnvelope.Response<>(
                        new TourApiEnvelope.Header("0000", "OK"),
                        new TourApiEnvelope.Body<>(
                                new TourApiEnvelope.Items<>(List.of("a", "b")),
                                2, 1, 2
                        )
                )
        );

        List<String> items = support.unwrapItems(envelope, TourApiErrorStatus.KOR_SERVICE_RESPONSE_INVALID);

        assertThat(items).containsExactly("a", "b");
    }

    // 결과가 없을 때(items가 null) 빈 리스트를 반환하는지 검증
    @Test
    void unwrapItemsReturnsEmptyListWhenNoResults() {
        TourApiEnvelope<String> envelope = new TourApiEnvelope<>(
                new TourApiEnvelope.Response<>(
                        new TourApiEnvelope.Header("0000", "OK"),
                        new TourApiEnvelope.Body<>(null, 0, 1, 0)
                )
        );

        List<String> items = support.unwrapItems(envelope, TourApiErrorStatus.KOR_SERVICE_RESPONSE_INVALID);

        assertThat(items).isEmpty();
    }

    // 실패 코드 응답이면 예외로 변환하는지 검증
    @Test
    void unwrapItemsThrowsWhenHeaderIsNotSuccess() {
        TourApiEnvelope<String> envelope = new TourApiEnvelope<>(
                new TourApiEnvelope.Response<>(
                        new TourApiEnvelope.Header("99", "UNKNOWN_ERROR"),
                        null
                )
        );

        assertThatThrownBy(() -> support.unwrapItems(envelope, TourApiErrorStatus.KOR_SERVICE_RESPONSE_INVALID))
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getErrorStatus())
                .isEqualTo(TourApiErrorStatus.KOR_SERVICE_RESPONSE_INVALID);
    }

    // 응답 자체가 null이면 예외로 변환하는지 검증
    @Test
    void unwrapItemsThrowsWhenEnvelopeIsNull() {
        assertThatThrownBy(() -> support.unwrapItems(null, TourApiErrorStatus.KOR_SERVICE_RESPONSE_INVALID))
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getErrorStatus())
                .isEqualTo(TourApiErrorStatus.KOR_SERVICE_RESPONSE_INVALID);
    }

    // envelope은 있지만 response가 null이면 예외로 변환하는지 검증
    @Test
    void unwrapItemsThrowsWhenResponseIsNull() {
        TourApiEnvelope<String> envelope = new TourApiEnvelope<>(null);

        assertThatThrownBy(() -> support.unwrapItems(envelope, TourApiErrorStatus.KOR_SERVICE_RESPONSE_INVALID))
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getErrorStatus())
                .isEqualTo(TourApiErrorStatus.KOR_SERVICE_RESPONSE_INVALID);
    }

    // header 자체가 null이면 예외로 변환하는지 검증
    @Test
    void unwrapItemsThrowsWhenHeaderIsNull() {
        TourApiEnvelope<String> envelope = new TourApiEnvelope<>(
                new TourApiEnvelope.Response<>(null, null)
        );

        assertThatThrownBy(() -> support.unwrapItems(envelope, TourApiErrorStatus.KOR_SERVICE_RESPONSE_INVALID))
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getErrorStatus())
                .isEqualTo(TourApiErrorStatus.KOR_SERVICE_RESPONSE_INVALID);
    }

    // body 자체가 null이면 빈 리스트를 반환하는지 검증
    @Test
    void unwrapItemsReturnsEmptyListWhenBodyIsNull() {
        TourApiEnvelope<String> envelope = new TourApiEnvelope<>(
                new TourApiEnvelope.Response<>(new TourApiEnvelope.Header("0000", "OK"), null)
        );

        List<String> items = support.unwrapItems(envelope, TourApiErrorStatus.KOR_SERVICE_RESPONSE_INVALID);

        assertThat(items).isEmpty();
    }

    // items 객체는 있지만 그 안의 item 목록 자체가 null이면 빈 리스트를 반환하는지 검증
    @Test
    void unwrapItemsReturnsEmptyListWhenItemListIsNull() {
        TourApiEnvelope<String> envelope = new TourApiEnvelope<>(
                new TourApiEnvelope.Response<>(
                        new TourApiEnvelope.Header("0000", "OK"),
                        new TourApiEnvelope.Body<>(new TourApiEnvelope.Items<>(null), 0, 1, 0)
                )
        );

        List<String> items = support.unwrapItems(envelope, TourApiErrorStatus.KOR_SERVICE_RESPONSE_INVALID);

        assertThat(items).isEmpty();
    }

    // HTTP 응답 자체를 못 받는 연결 레벨 실패(WebClientResponseException이 아닌 경우)도 GeneralException으로 변환되는지 검증
    @Test
    void getMapsNonResponseConnectionFailure() {
        WebClient webClient = WebClient.builder()
                .exchangeFunction(request -> Mono.error(new WebClientRequestException(
                        new RuntimeException("connection refused"),
                        HttpMethod.GET,
                        URI.create("http://example.com"),
                        new HttpHeaders()
                )))
                .build();

        assertThatThrownBy(() -> support.get(
                "test-caller",
                webClient,
                uriBuilder -> uriBuilder.build(),
                new ParameterizedTypeReference<String>() {
                },
                TourApiErrorStatus.KOR_SERVICE_CONNECTION_FAILED
        ))
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getErrorStatus())
                .isEqualTo(TourApiErrorStatus.KOR_SERVICE_CONNECTION_FAILED);
    }

    // HTTP 응답은 받았지만 실패 상태 코드(WebClientResponseException)인 경우도 GeneralException으로 변환되는지 검증
    @Test
    void getMapsResponseErrorWithBody() {
        WebClient webClient = WebClient.builder()
                .exchangeFunction(request -> Mono.just(
                        ClientResponse.create(HttpStatus.FORBIDDEN)
                                .header("Content-Type", "application/json")
                                .body("{\"errorType\":\"NotAuthorizedError\"}")
                                .build()
                ))
                .build();

        assertThatThrownBy(() -> support.get(
                "test-caller",
                webClient,
                uriBuilder -> uriBuilder.build(),
                new ParameterizedTypeReference<String>() {
                },
                TourApiErrorStatus.KOR_SERVICE_CONNECTION_FAILED
        ))
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getErrorStatus())
                .isEqualTo(TourApiErrorStatus.KOR_SERVICE_CONNECTION_FAILED);
    }
}
