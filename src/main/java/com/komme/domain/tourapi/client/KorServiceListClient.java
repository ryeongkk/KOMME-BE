package com.komme.domain.tourapi.client;

import java.time.Duration;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.komme.domain.tourapi.cache.TourApiCacheSupport;
import com.komme.domain.tourapi.cache.TourApiRedisKeys;
import com.komme.domain.tourapi.exception.TourApiErrorStatus;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

// 한국관광공사 국문 관광정보 서비스(KorService2) 목록조회 클라이언트 (areaBasedList2/locationBasedList2/searchKeyword2)
@Component
public class KorServiceListClient {

    private static final Duration LIST_CACHE_TTL = Duration.ofHours(6);
    private static final int DEFAULT_NUM_OF_ROWS = 100;
    private static final int DEFAULT_PAGE_NO = 1;

    private final WebClient korServiceApiWebClient;
    private final TourApiQuerySupport tourApiQuerySupport;
    private final TourApiCacheSupport tourApiCacheSupport;

    public KorServiceListClient(
            @Qualifier("korServiceApiWebClient") WebClient korServiceApiWebClient,
            TourApiQuerySupport tourApiQuerySupport,
            TourApiCacheSupport tourApiCacheSupport
    ) {
        this.korServiceApiWebClient = korServiceApiWebClient;
        this.tourApiQuerySupport = tourApiQuerySupport;
        this.tourApiCacheSupport = tourApiCacheSupport;
    }

    // 지역기반 스팟 목록조회 (areaBasedList2)
    public List<AreaBasedListItem> findAreaBasedList(String areaCode, String sigunguCode, String contentTypeId) {
        return tourApiCacheSupport.getOrLoad(
                TourApiRedisKeys.korServiceAreaBasedList(areaCode, sigunguCode, contentTypeId),
                LIST_CACHE_TTL,
                new TypeReference<>() {
                },
                () -> callAreaBasedList(areaCode, sigunguCode, contentTypeId)
        );
    }

    private List<AreaBasedListItem> callAreaBasedList(String areaCode, String sigunguCode, String contentTypeId) {
        TourApiEnvelope<AreaBasedListItem> envelope = tourApiQuerySupport.get(
                "tourapi-area-based-list",
                korServiceApiWebClient,
                uriBuilder -> tourApiQuerySupport.withCommonParams(uriBuilder)
                        .path("/areaBasedList2")
                        .queryParam("areaCode", areaCode)
                        .queryParam("sigunguCode", sigunguCode)
                        .queryParam("contentTypeId", contentTypeId)
                        .queryParam("arrange", "A")
                        .queryParam("numOfRows", DEFAULT_NUM_OF_ROWS)
                        .queryParam("pageNo", DEFAULT_PAGE_NO)
                        .build(),
                new ParameterizedTypeReference<TourApiEnvelope<AreaBasedListItem>>() {
                },
                TourApiErrorStatus.KOR_SERVICE_CONNECTION_FAILED
        );
        return tourApiQuerySupport.unwrapItems(envelope, TourApiErrorStatus.KOR_SERVICE_RESPONSE_INVALID);
    }

    // 좌표기반 주변 스팟 목록조회 (locationBasedList2)
    public List<LocationBasedListItem> findLocationBasedList(
            String mapX,
            String mapY,
            String radiusMeters,
            String contentTypeId
    ) {
        return tourApiCacheSupport.getOrLoad(
                TourApiRedisKeys.korServiceLocationBasedList(mapX, mapY, radiusMeters, contentTypeId),
                LIST_CACHE_TTL,
                new TypeReference<>() {
                },
                () -> callLocationBasedList(mapX, mapY, radiusMeters, contentTypeId)
        );
    }

    private List<LocationBasedListItem> callLocationBasedList(
            String mapX,
            String mapY,
            String radiusMeters,
            String contentTypeId
    ) {
        TourApiEnvelope<LocationBasedListItem> envelope = tourApiQuerySupport.get(
                "tourapi-location-based-list",
                korServiceApiWebClient,
                uriBuilder -> tourApiQuerySupport.withCommonParams(uriBuilder)
                        .path("/locationBasedList2")
                        .queryParam("mapX", mapX)
                        .queryParam("mapY", mapY)
                        .queryParam("radius", radiusMeters)
                        .queryParam("contentTypeId", contentTypeId)
                        .queryParam("arrange", "E")
                        .queryParam("numOfRows", DEFAULT_NUM_OF_ROWS)
                        .queryParam("pageNo", DEFAULT_PAGE_NO)
                        .build(),
                new ParameterizedTypeReference<TourApiEnvelope<LocationBasedListItem>>() {
                },
                TourApiErrorStatus.KOR_SERVICE_CONNECTION_FAILED
        );
        return tourApiQuerySupport.unwrapItems(envelope, TourApiErrorStatus.KOR_SERVICE_RESPONSE_INVALID);
    }

    // 키워드 검색조회 (searchKeyword2) - 캐시하지 않음(호출부에서 필요시 자체 캐시)
    public List<AreaBasedListItem> searchByKeyword(String keyword, String areaCode) {
        TourApiEnvelope<AreaBasedListItem> envelope = tourApiQuerySupport.get(
                "tourapi-keyword-search",
                korServiceApiWebClient,
                uriBuilder -> tourApiQuerySupport.withCommonParams(uriBuilder)
                        .path("/searchKeyword2")
                        .queryParam("keyword", tourApiQuerySupport.encode(keyword))
                        .queryParam("areaCode", areaCode)
                        .queryParam("arrange", "A")
                        .queryParam("numOfRows", DEFAULT_NUM_OF_ROWS)
                        .queryParam("pageNo", DEFAULT_PAGE_NO)
                        .build(),
                new ParameterizedTypeReference<TourApiEnvelope<AreaBasedListItem>>() {
                },
                TourApiErrorStatus.KOR_SERVICE_CONNECTION_FAILED
        );
        return tourApiQuerySupport.unwrapItems(envelope, TourApiErrorStatus.KOR_SERVICE_RESPONSE_INVALID);
    }
}
