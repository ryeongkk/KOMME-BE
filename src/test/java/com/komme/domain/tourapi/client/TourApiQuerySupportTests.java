package com.komme.domain.tourapi.client;

import java.util.List;

import com.komme.common.exception.GeneralException;
import com.komme.domain.tourapi.exception.TourApiErrorStatus;
import com.komme.domain.tourapi.properties.TourApiProperties;

import org.junit.jupiter.api.Test;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilder;

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
}
