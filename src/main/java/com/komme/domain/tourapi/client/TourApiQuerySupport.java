package com.komme.domain.tourapi.client;

import java.util.List;

import com.komme.common.base.status.BaseStatus;
import com.komme.common.exception.GeneralException;
import com.komme.domain.tourapi.properties.TourApiProperties;

import org.springframework.stereotype.Component;
import org.springframework.web.util.UriBuilder;

import lombok.RequiredArgsConstructor;

// 관광공사 OpenAPI 공통 요청 파라미터 부착 및 공통 응답 봉투 언패킹 지원
@Component
@RequiredArgsConstructor
public class TourApiQuerySupport {

    private static final String JSON_RESPONSE_TYPE = "json";

    private final TourApiProperties tourApiProperties;

    // serviceKey/MobileOS/MobileApp/_type 등 모든 오퍼레이션에 공통으로 필요한 요청 파라미터 부착
    public UriBuilder withCommonParams(UriBuilder uriBuilder) {
        return uriBuilder
                .queryParam("serviceKey", tourApiProperties.getServiceKey())
                .queryParam("MobileOS", tourApiProperties.getMobileOs())
                .queryParam("MobileApp", tourApiProperties.getMobileApp())
                .queryParam("_type", JSON_RESPONSE_TYPE);
    }

    // 공통 응답 봉투에서 item 목록만 꺼내고, 실패 응답이면 지정된 상태로 예외 발생
    public <T> List<T> unwrapItems(TourApiEnvelope<T> envelope, BaseStatus invalidResponseStatus) {
        if (envelope == null || envelope.response() == null) {
            throw new GeneralException(invalidResponseStatus);
        }

        TourApiEnvelope.Header header = envelope.response().header();
        if (header == null || !header.isSuccess()) {
            throw new GeneralException(invalidResponseStatus);
        }

        TourApiEnvelope.Body<T> body = envelope.response().body();
        if (body == null || body.items() == null || body.items().item() == null) {
            return List.of();
        }

        return body.items().item();
    }
}
