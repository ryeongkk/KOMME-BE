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
import org.springframework.web.util.UriBuilder;

// 한국관광공사 관광지 집중률 방문자 추이 예측 정보 서비스(TatsCnctrRateService) 클라이언트
// 응답은 기준일로부터 향후 30일치 날짜별 집중률이며, 원본 데이터가 일 1회 갱신되므로 캐시 TTL도 24시간으로 맞춘다.
@Component
public class ConcentrationRateClient {

    private static final Duration CONCENTRATION_RATE_CACHE_TTL = Duration.ofHours(24);

    private final WebClient concentrationRateApiWebClient;
    private final TourApiQuerySupport tourApiQuerySupport;
    private final TourApiCacheSupport tourApiCacheSupport;

    public ConcentrationRateClient(
            @Qualifier("concentrationRateApiWebClient") WebClient concentrationRateApiWebClient,
            TourApiQuerySupport tourApiQuerySupport,
            TourApiCacheSupport tourApiCacheSupport
    ) {
        this.concentrationRateApiWebClient = concentrationRateApiWebClient;
        this.tourApiQuerySupport = tourApiQuerySupport;
        this.tourApiCacheSupport = tourApiCacheSupport;
    }

    // 관광지 집중률 정보 목록조회 (tatsCnctrRatedList) - touristSpotName은 null이면 해당 시군구 전체 조회
    public List<ConcentrationRateItem> findConcentrationRates(
            String areaCode,
            String sigunguCode,
            String touristSpotName
    ) {
        return tourApiCacheSupport.getOrLoad(
                TourApiRedisKeys.concentrationRate(areaCode, sigunguCode, touristSpotName),
                CONCENTRATION_RATE_CACHE_TTL,
                new TypeReference<>() {
                },
                () -> call(areaCode, sigunguCode, touristSpotName)
        );
    }

    private List<ConcentrationRateItem> call(String areaCode, String sigunguCode, String touristSpotName) {
        TourApiEnvelope<ConcentrationRateItem> envelope = tourApiQuerySupport.get(
                "tourapi-concentration-rate",
                concentrationRateApiWebClient,
                uriBuilder -> {
                    UriBuilder withCommonParams = tourApiQuerySupport.withCommonParams(uriBuilder)
                            .path("/tatsCnctrRatedList")
                            .queryParam("areaCd", areaCode)
                            .queryParam("signguCd", sigunguCode)
                            .queryParam("numOfRows", 30)
                            .queryParam("pageNo", 1);
                    if (touristSpotName != null && !touristSpotName.isBlank()) {
                        withCommonParams = withCommonParams.queryParam("tAtsNm", touristSpotName);
                    }
                    return withCommonParams.build();
                },
                new ParameterizedTypeReference<TourApiEnvelope<ConcentrationRateItem>>() {
                },
                TourApiErrorStatus.CONCENTRATION_RATE_CONNECTION_FAILED
        );
        return tourApiQuerySupport.unwrapItems(envelope, TourApiErrorStatus.CONCENTRATION_RATE_RESPONSE_INVALID);
    }
}
