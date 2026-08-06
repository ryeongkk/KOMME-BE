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
