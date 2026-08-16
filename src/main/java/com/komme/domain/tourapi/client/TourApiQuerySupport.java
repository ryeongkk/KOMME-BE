package com.komme.domain.tourapi.client;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import com.komme.common.base.status.BaseStatus;
import com.komme.common.exception.GeneralException;
import com.komme.domain.tourapi.properties.TourApiProperties;

import org.springframework.core.NestedExceptionUtils;
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
    private static final int RESPONSE_BODY_LOG_MAX_LENGTH = 500;

    private final TourApiProperties tourApiProperties;

    // serviceKey/MobileOS/MobileApp/_type 등 모든 오퍼레이션에 공통으로 필요한 요청 파라미터 부착
    // 관광공사 WebClient는 URI 자동 인코딩을 꺼뒀기 때문에(WebClientConfig 참고) 값을 직접 인코딩해서 넣는다 -
    // serviceKey는 base64(+, /, = 포함)라 Spring 기본 인코더가 '+'를 놓치는 문제가 있었다.
    public UriBuilder withCommonParams(UriBuilder uriBuilder) {
        return uriBuilder
                .queryParam("serviceKey", encode(tourApiProperties.getServiceKey()))
                .queryParam("MobileOS", encode(tourApiProperties.getMobileOs()))
                .queryParam("MobileApp", encode(tourApiProperties.getMobileApp()))
                .queryParam("_type", JSON_RESPONSE_TYPE);
    }

    // 관광공사 WebClient(인코딩 없음 모드)에 넣을 쿼리파라미터 값을 UTF-8로 URL 인코딩하는 기능
    public String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    // 외부 API 공통 GET 호출 - 연결 실패는 지정된 상태로 GeneralException 변환
    // callerLabel은 이 헬퍼를 공유하는 여러 클라이언트/오퍼레이션 중 어디서 실패했는지 로그로 구분하기 위한 식별자다.
    public <T> T get(
            String callerLabel,
            WebClient webClient,
            Function<UriBuilder, URI> uriFunction,
            ParameterizedTypeReference<T> responseType,
            BaseStatus connectionFailedStatus
    ) {
        return get(callerLabel, webClient, uriFunction, responseType, connectionFailedStatus, headers -> {
        });
    }

    // 요청 헤더 커스터마이징이 필요한 경우(예: 카카오 Authorization 헤더)의 외부 API 공통 GET 호출
    public <T> T get(
            String callerLabel,
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
            logConnectionFailure(callerLabel, exception);
            throw new GeneralException(connectionFailedStatus);
        }
    }

    // 서비스키가 담긴 원본 예외 메시지를 노출하지 않고, 안전한 필드만 골라 로그로 남기는 기능
    private void logConnectionFailure(String callerLabel, WebClientException exception) {
        if (exception instanceof WebClientResponseException responseException) {
            // 응답 바디는 원칙적으로 상대 서버(카카오/관광공사)가 돌려준 응답이라 서비스키를 담고 있지 않지만,
            // 게이트웨이/프록시 에러 페이지가 요청 URL을 그대로 반사하는 경우가 있어 방어적으로 마스킹한다.
            log.warn(
                    "[*] TourApi 외부 호출 실패 - caller={}, httpStatus={}, body={}",
                    callerLabel,
                    responseException.getStatusCode(),
                    truncate(redactServiceKey(responseException.getResponseBodyAsString()))
            );
        } else {
            // 최하위 원인(NestedExceptionUtils)은 ConnectException/SocketTimeoutException 등 순수 I/O 예외라
            // 요청 URI를 담고 있지 않다 - WebClientException 자체의 메시지 대신 이쪽만 골라 로그로 남긴다.
            Throwable rootCause = NestedExceptionUtils.getMostSpecificCause(exception);
            log.warn(
                    "[*] TourApi 외부 호출 실패 - caller={}, cause={}: {}",
                    callerLabel,
                    rootCause.getClass().getSimpleName(),
                    rootCause.getMessage()
            );
        }
    }

    // 응답 바디에 원본/URL 인코딩된 서비스키가 그대로 반사되어 있으면 마스킹하고, 개행도 제거하는 기능
    // (로그 위조/여러 줄 스팸 방지 겸) - package-private, 같은 패키지 테스트에서 직접 검증한다.
    String redactServiceKey(String responseBody) {
        if (responseBody == null) {
            return null;
        }

        String serviceKey = tourApiProperties.getServiceKey();
        String redacted = responseBody;
        if (serviceKey != null && !serviceKey.isBlank()) {
            redacted = redacted.replace(serviceKey, "[REDACTED]")
                    .replace(encode(serviceKey), "[REDACTED]");
        }
        return redacted.replaceAll("[\\r\\n]+", " ");
    }

    // 로그가 에러 페이지 전체(HTML 등)로 도배되지 않도록 응답 바디를 앞부분만 잘라내는 기능
    private String truncate(String responseBody) {
        if (responseBody == null || responseBody.length() <= RESPONSE_BODY_LOG_MAX_LENGTH) {
            return responseBody;
        }
        return responseBody.substring(0, RESPONSE_BODY_LOG_MAX_LENGTH) + "...(truncated)";
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
