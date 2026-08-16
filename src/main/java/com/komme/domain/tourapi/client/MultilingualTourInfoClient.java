package com.komme.domain.tourapi.client;

import java.time.Duration;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.komme.common.exception.GeneralException;
import com.komme.domain.i18n.enums.Language;
import com.komme.domain.tourapi.cache.TourApiCacheSupport;
import com.komme.domain.tourapi.cache.TourApiRedisKeys;
import com.komme.domain.tourapi.exception.TourApiErrorStatus;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

// 한국관광공사 다국어 관광정보 서비스(언어별 별도 API - EngService2/JpnService2/ChsService2) 클라이언트
// ⚠ base URL과 마찬가지로 오퍼레이션 경로도 KorService2와 동일한 이름(detailCommon2)일 것으로 가정한 추정치다.
// 실제 호출 결과로 검증 필요.
@Component
public class MultilingualTourInfoClient {

    private static final Duration DETAIL_CACHE_TTL = Duration.ofHours(24);

    private final Map<Language, WebClient> webClientsByLanguage;
    private final TourApiQuerySupport tourApiQuerySupport;
    private final TourApiCacheSupport tourApiCacheSupport;

    public MultilingualTourInfoClient(
            @Qualifier("engServiceApiWebClient") WebClient engServiceApiWebClient,
            @Qualifier("jpnServiceApiWebClient") WebClient jpnServiceApiWebClient,
            @Qualifier("chsServiceApiWebClient") WebClient chsServiceApiWebClient,
            TourApiQuerySupport tourApiQuerySupport,
            TourApiCacheSupport tourApiCacheSupport
    ) {
        this.webClientsByLanguage = new EnumMap<>(Language.class);
        this.webClientsByLanguage.put(Language.ENGLISH, engServiceApiWebClient);
        this.webClientsByLanguage.put(Language.JAPANESE, jpnServiceApiWebClient);
        this.webClientsByLanguage.put(Language.CHINESE_SIMPLIFIED, chsServiceApiWebClient);
        this.tourApiQuerySupport = tourApiQuerySupport;
        this.tourApiCacheSupport = tourApiCacheSupport;
    }

    // 언어별 공통정보 조회 (detailCommon2 상당) - 지원하지 않는 언어면 예외
    public DetailCommonItem findDetailCommon(Language language, String contentId, String contentTypeId) {
        List<DetailCommonItem> items = tourApiCacheSupport.getOrLoad(
                TourApiRedisKeys.multilingualDetail(contentId, language.getCode()),
                DETAIL_CACHE_TTL,
                new TypeReference<>() {
                },
                () -> call(language, contentId, contentTypeId)
        );
        return items.isEmpty() ? null : items.get(0);
    }

    private List<DetailCommonItem> call(Language language, String contentId, String contentTypeId) {
        WebClient webClient = webClientsByLanguage.get(language);
        if (webClient == null) {
            throw new GeneralException(TourApiErrorStatus.MULTILINGUAL_RESPONSE_INVALID);
        }

        TourApiEnvelope<DetailCommonItem> envelope = tourApiQuerySupport.get(
                "tourapi-multilingual-detail-common",
                webClient,
                uriBuilder -> tourApiQuerySupport.withCommonParams(uriBuilder)
                        .path("/detailCommon2")
                        .queryParam("contentId", contentId)
                        .queryParam("contentTypeId", contentTypeId)
                        .queryParam("defaultYN", "Y")
                        .queryParam("overviewYN", "Y")
                        .build(),
                new ParameterizedTypeReference<TourApiEnvelope<DetailCommonItem>>() {
                },
                TourApiErrorStatus.MULTILINGUAL_CONNECTION_FAILED
        );
        return tourApiQuerySupport.unwrapItems(envelope, TourApiErrorStatus.MULTILINGUAL_RESPONSE_INVALID);
    }
}
