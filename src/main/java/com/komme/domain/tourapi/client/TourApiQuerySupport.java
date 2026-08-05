package com.komme.domain.tourapi.client;

import java.net.URI;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import com.komme.common.base.status.BaseStatus;
import com.komme.common.exception.GeneralException;
import com.komme.domain.tourapi.properties.TourApiProperties;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriBuilder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// 관광공사 OpenAPI 공통 요청 파라미터 부착, 공통 응답 봉투 언패킹, 외부 API GET 호출 공통 처리를 지원
// (카카오 로컬 API처럼 관광공사 API가 아닌 클라이언트도 GET 호출 자체는 이 헬퍼를 공유해서 쓴다)
@Slf4j
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

    // 외부 API 공통 GET 호출 - 연결 실패는 지정된 상태로 GeneralException 변환
    public <T> T get(
            WebClient webClient,
            Function<UriBuilder, URI> uriFunction,
            ParameterizedTypeReference<T> responseType,
            BaseStatus connectionFailedStatus
    ) {
        return get(webClient, uriFunction, responseType, connectionFailedStatus, headers -> {
        });
    }

    // 요청 헤더 커스터마이징이 필요한 경우(예: 카카오 Authorization 헤더)의 외부 API 공통 GET 호출
    public <T> T get(
            WebClient webClient,
            Function<UriBuilder, URI> uriFunction,
            ParameterizedTypeReference<T> responseType,
            BaseStatus connectionFailedStatus,
            Consumer<HttpHeaders> headersCustomizer
    ) {
        try {
            return webClient.get()
                    .uri(uriFunction::apply)
                    .headers(headersCustomizer)
                    .retrieve()
                    .bodyToMono(responseType)
                    .block();
        } catch (WebClientException exception) {
            // WebClientException의 메시지/toString은 요청 URI(서비스키 쿼리파라미터 포함)를 그대로 담고 있어
            // 절대 로그에 찍지 않는다 - 원인 예외를 cause로도 넘기지 않고, 안전한 필드만 따로 로그로 남긴다.
            logConnectionFailure(exception);
            throw new GeneralException(connectionFailedStatus);
        }
    }

    // 서비스키가 담긴 원본 예외 메시지를 노출하지 않고, 안전한 필드만 골라 로그로 남기는 기능
    private void logConnectionFailure(WebClientException exception) {
        if (exception instanceof WebClientResponseException responseException) {
            log.warn("[*] TourApi 외부 호출 실패 - httpStatus={}", responseException.getStatusCode());
        } else {
            log.warn("[*] TourApi 외부 호출 실패 - {}", exception.getClass().getSimpleName());
        }
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
