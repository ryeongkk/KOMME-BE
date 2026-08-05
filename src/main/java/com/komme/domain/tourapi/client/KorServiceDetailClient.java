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

// 한국관광공사 국문 관광정보 서비스(KorService2) 상세조회 클라이언트 (detailCommon2/detailIntro2/detailImage2)
@Component
public class KorServiceDetailClient {

    private static final Duration DETAIL_CACHE_TTL = Duration.ofHours(12);

    private final WebClient korServiceApiWebClient;
    private final TourApiQuerySupport tourApiQuerySupport;
    private final TourApiCacheSupport tourApiCacheSupport;

    public KorServiceDetailClient(
            @Qualifier("korServiceApiWebClient") WebClient korServiceApiWebClient,
            TourApiQuerySupport tourApiQuerySupport,
            TourApiCacheSupport tourApiCacheSupport
    ) {
        this.korServiceApiWebClient = korServiceApiWebClient;
        this.tourApiQuerySupport = tourApiQuerySupport;
        this.tourApiCacheSupport = tourApiCacheSupport;
    }

    // 공통정보 조회 (detailCommon2) - 영업시간 외 개요/연락처 등
    public DetailCommonItem findDetailCommon(String contentId, String contentTypeId) {
        List<DetailCommonItem> items = tourApiCacheSupport.getOrLoad(
                TourApiRedisKeys.korServiceDetailCommon(contentId),
                DETAIL_CACHE_TTL,
                new TypeReference<>() {
                },
                () -> callDetailCommon(contentId, contentTypeId)
        );
        return items.isEmpty() ? null : items.get(0);
    }

    private List<DetailCommonItem> callDetailCommon(String contentId, String contentTypeId) {
        TourApiEnvelope<DetailCommonItem> envelope = tourApiQuerySupport.get(
                korServiceApiWebClient,
                uriBuilder -> tourApiQuerySupport.withCommonParams(uriBuilder)
                        .path("/detailCommon2")
                        .queryParam("contentId", contentId)
                        .queryParam("contentTypeId", contentTypeId)
                        .queryParam("defaultYN", "Y")
                        .queryParam("overviewYN", "Y")
                        .build(),
                new ParameterizedTypeReference<TourApiEnvelope<DetailCommonItem>>() {
                },
                TourApiErrorStatus.KOR_SERVICE_CONNECTION_FAILED
        );
        return tourApiQuerySupport.unwrapItems(envelope, TourApiErrorStatus.KOR_SERVICE_RESPONSE_INVALID);
    }

    // 소개정보 조회 (detailIntro2) - 영업시간/휴무일 등 (contentTypeId별 필드 상이, DetailIntroItem 주석 참고)
    public DetailIntroItem findDetailIntro(String contentId, String contentTypeId) {
        List<DetailIntroItem> items = tourApiCacheSupport.getOrLoad(
                TourApiRedisKeys.korServiceDetailIntro(contentId),
                DETAIL_CACHE_TTL,
                new TypeReference<>() {
                },
                () -> callDetailIntro(contentId, contentTypeId)
        );
        return items.isEmpty() ? null : items.get(0);
    }

    private List<DetailIntroItem> callDetailIntro(String contentId, String contentTypeId) {
        TourApiEnvelope<DetailIntroItem> envelope = tourApiQuerySupport.get(
                korServiceApiWebClient,
                uriBuilder -> tourApiQuerySupport.withCommonParams(uriBuilder)
                        .path("/detailIntro2")
                        .queryParam("contentId", contentId)
                        .queryParam("contentTypeId", contentTypeId)
                        .build(),
                new ParameterizedTypeReference<TourApiEnvelope<DetailIntroItem>>() {
                },
                TourApiErrorStatus.KOR_SERVICE_CONNECTION_FAILED
        );
        return tourApiQuerySupport.unwrapItems(envelope, TourApiErrorStatus.KOR_SERVICE_RESPONSE_INVALID);
    }

    // 이미지정보 조회 (detailImage2)
    public List<DetailImageItem> findDetailImages(String contentId) {
        return tourApiCacheSupport.getOrLoad(
                TourApiRedisKeys.korServiceDetailImages(contentId),
                DETAIL_CACHE_TTL,
                new TypeReference<>() {
                },
                () -> callDetailImages(contentId)
        );
    }

    private List<DetailImageItem> callDetailImages(String contentId) {
        TourApiEnvelope<DetailImageItem> envelope = tourApiQuerySupport.get(
                korServiceApiWebClient,
                uriBuilder -> tourApiQuerySupport.withCommonParams(uriBuilder)
                        .path("/detailImage2")
                        .queryParam("contentId", contentId)
                        .queryParam("imageYN", "Y")
                        .build(),
                new ParameterizedTypeReference<TourApiEnvelope<DetailImageItem>>() {
                },
                TourApiErrorStatus.KOR_SERVICE_CONNECTION_FAILED
        );
        return tourApiQuerySupport.unwrapItems(envelope, TourApiErrorStatus.KOR_SERVICE_RESPONSE_INVALID);
    }
}
