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

// 한국관광공사 국문 관광정보 서비스(KorService2) 클라이언트
@Component
public class KorServiceClient {

    private static final Duration LIST_CACHE_TTL = Duration.ofHours(6);
    private static final Duration DETAIL_CACHE_TTL = Duration.ofHours(12);
    private static final int DEFAULT_NUM_OF_ROWS = 100;
    private static final int DEFAULT_PAGE_NO = 1;

    private final WebClient korServiceApiWebClient;
    private final TourApiQuerySupport tourApiQuerySupport;
    private final TourApiCacheSupport tourApiCacheSupport;

    // 클라이언트가 사용할 KorService2 WebClient 빈을 지정
    public KorServiceClient(
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
        TourApiEnvelope<AreaBasedListItem> envelope = get(
                uriBuilder -> tourApiQuerySupport.withCommonParams(uriBuilder)
                        .path("/areaBasedList2")
                        .queryParam("areaCode", areaCode)
                        .queryParam("sigunguCode", sigunguCode)
                        .queryParam("contentTypeId", contentTypeId)
                        .queryParam("arrange", "A")
                        .queryParam("numOfRows", DEFAULT_NUM_OF_ROWS)
                        .queryParam("pageNo", DEFAULT_PAGE_NO)
                        .build(),
                new ParameterizedTypeReference<>() {
                }
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
        TourApiEnvelope<LocationBasedListItem> envelope = get(
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
                new ParameterizedTypeReference<>() {
                }
        );
        return tourApiQuerySupport.unwrapItems(envelope, TourApiErrorStatus.KOR_SERVICE_RESPONSE_INVALID);
    }

    // 키워드 검색조회 (searchKeyword2) - 캐시하지 않음(호출부에서 필요시 자체 캐시)
    public List<AreaBasedListItem> searchByKeyword(String keyword, String areaCode) {
        TourApiEnvelope<AreaBasedListItem> envelope = get(
                uriBuilder -> tourApiQuerySupport.withCommonParams(uriBuilder)
                        .path("/searchKeyword2")
                        .queryParam("keyword", keyword)
                        .queryParam("areaCode", areaCode)
                        .queryParam("arrange", "A")
                        .queryParam("numOfRows", DEFAULT_NUM_OF_ROWS)
                        .queryParam("pageNo", DEFAULT_PAGE_NO)
                        .build(),
                new ParameterizedTypeReference<>() {
                }
        );
        return tourApiQuerySupport.unwrapItems(envelope, TourApiErrorStatus.KOR_SERVICE_RESPONSE_INVALID);
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
        TourApiEnvelope<DetailCommonItem> envelope = get(
                uriBuilder -> tourApiQuerySupport.withCommonParams(uriBuilder)
                        .path("/detailCommon2")
                        .queryParam("contentId", contentId)
                        .queryParam("contentTypeId", contentTypeId)
                        .queryParam("defaultYN", "Y")
                        .queryParam("overviewYN", "Y")
                        .build(),
                new ParameterizedTypeReference<>() {
                }
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
        TourApiEnvelope<DetailIntroItem> envelope = get(
                uriBuilder -> tourApiQuerySupport.withCommonParams(uriBuilder)
                        .path("/detailIntro2")
                        .queryParam("contentId", contentId)
                        .queryParam("contentTypeId", contentTypeId)
                        .build(),
                new ParameterizedTypeReference<>() {
                }
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
        TourApiEnvelope<DetailImageItem> envelope = get(
                uriBuilder -> tourApiQuerySupport.withCommonParams(uriBuilder)
                        .path("/detailImage2")
                        .queryParam("contentId", contentId)
                        .queryParam("imageYN", "Y")
                        .build(),
                new ParameterizedTypeReference<>() {
                }
        );
        return tourApiQuerySupport.unwrapItems(envelope, TourApiErrorStatus.KOR_SERVICE_RESPONSE_INVALID);
    }

    // KorService2 공통 GET 호출 - 연결 실패는 GeneralException으로 변환
    private <T> TourApiEnvelope<T> get(
            Function<UriBuilder, URI> uriFunction,
            ParameterizedTypeReference<TourApiEnvelope<T>> responseType
    ) {
        try {
            return korServiceApiWebClient.get()
                    .uri(uriFunction::apply)
                    .retrieve()
                    .bodyToMono(responseType)
                    .block();
        } catch (WebClientException exception) {
            throw new GeneralException(TourApiErrorStatus.KOR_SERVICE_CONNECTION_FAILED, exception);
        }
    }
}
