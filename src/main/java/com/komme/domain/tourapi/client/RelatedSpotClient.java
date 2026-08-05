package com.komme.domain.tourapi.client;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.function.Function;

import com.fasterxml.jackson.core.type.TypeReference;
import com.komme.common.exception.GeneralException;
import com.komme.domain.tourapi.cache.TourApiCacheSupport;
import com.komme.domain.tourapi.cache.TourApiRedisKeys;
import com.komme.domain.tourapi.exception.TourApiErrorStatus;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.util.UriBuilder;

// 한국관광공사 관광지별 연관 관광지 서비스(TarRlteTarService1) 클라이언트
// ⚠ 오퍼레이션 경로/요청 파라미터명을 매뉴얼로 확인하지 못해 KorService2 계열 네이밍 관례를 따라 추정 작성했다.
// 실제 호출해보고 404/파라미터 오류가 나면 data.go.kr Swagger에서 정확한 경로명을 확인해 수정해야 한다.
@Component
public class RelatedSpotClient {

    private static final Duration RELATED_SPOT_CACHE_TTL = Duration.ofHours(24);

    private final WebClient relatedSpotApiWebClient;
    private final TourApiQuerySupport tourApiQuerySupport;
    private final TourApiCacheSupport tourApiCacheSupport;

    public RelatedSpotClient(
            @Qualifier("relatedSpotApiWebClient") WebClient relatedSpotApiWebClient,
            TourApiQuerySupport tourApiQuerySupport,
            TourApiCacheSupport tourApiCacheSupport
    ) {
        this.relatedSpotApiWebClient = relatedSpotApiWebClient;
        this.tourApiQuerySupport = tourApiQuerySupport;
        this.tourApiCacheSupport = tourApiCacheSupport;
    }

    // 지역기반 연관 관광지 목록조회 (경로명 추정: areaBasedList1)
    public List<RelatedSpotItem> findByArea(String areaCode, String sigunguCode) {
        return tourApiCacheSupport.getOrLoad(
                TourApiRedisKeys.relatedSpot(areaCode + ":" + sigunguCode),
                RELATED_SPOT_CACHE_TTL,
                new TypeReference<>() {
                },
                () -> callByArea(areaCode, sigunguCode)
        );
    }

    private List<RelatedSpotItem> callByArea(String areaCode, String sigunguCode) {
        TourApiEnvelope<RelatedSpotItem> envelope = get(
                uriBuilder -> tourApiQuerySupport.withCommonParams(uriBuilder)
                        .path("/areaBasedList1")
                        .queryParam("areaCode", areaCode)
                        .queryParam("sigunguCode", sigunguCode)
                        .queryParam("numOfRows", 50)
                        .queryParam("pageNo", 1)
                        .build(),
                new ParameterizedTypeReference<>() {
                }
        );
        return tourApiQuerySupport.unwrapItems(envelope, TourApiErrorStatus.RELATED_SPOT_RESPONSE_INVALID);
    }

    // 키워드기반 연관 관광지 목록조회 (경로명 추정: searchKeyword1)
    public List<RelatedSpotItem> findByKeyword(String keyword) {
        return tourApiCacheSupport.getOrLoad(
                TourApiRedisKeys.relatedSpot("keyword:" + keyword),
                RELATED_SPOT_CACHE_TTL,
                new TypeReference<>() {
                },
                () -> callByKeyword(keyword)
        );
    }

    private List<RelatedSpotItem> callByKeyword(String keyword) {
        TourApiEnvelope<RelatedSpotItem> envelope = get(
                uriBuilder -> tourApiQuerySupport.withCommonParams(uriBuilder)
                        .path("/searchKeyword1")
                        .queryParam("keyword", keyword)
                        .queryParam("numOfRows", 50)
                        .queryParam("pageNo", 1)
                        .build(),
                new ParameterizedTypeReference<>() {
                }
        );
        return tourApiQuerySupport.unwrapItems(envelope, TourApiErrorStatus.RELATED_SPOT_RESPONSE_INVALID);
    }

    private <T> TourApiEnvelope<T> get(
            Function<UriBuilder, URI> uriFunction,
            ParameterizedTypeReference<TourApiEnvelope<T>> responseType
    ) {
        try {
            return relatedSpotApiWebClient.get()
                    .uri(uriFunction::apply)
                    .retrieve()
                    .bodyToMono(responseType)
                    .block();
        } catch (WebClientException exception) {
            throw new GeneralException(TourApiErrorStatus.RELATED_SPOT_CONNECTION_FAILED, exception);
        }
    }
}
