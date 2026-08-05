package com.komme.domain.course.client;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.function.Function;

import com.fasterxml.jackson.core.type.TypeReference;
import com.komme.common.exception.GeneralException;
import com.komme.domain.course.properties.KakaoLocalProperties;
import com.komme.domain.tourapi.cache.TourApiCacheSupport;
import com.komme.domain.tourapi.cache.TourApiRedisKeys;
import com.komme.domain.tourapi.exception.TourApiErrorStatus;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.util.UriBuilder;

// 카카오 로컬 API 클라이언트 - 1단계 위치 검색(자유 텍스트/콜로키얼 지명) 전담
// 서울/부산으로 결과를 좁히는 필터링은 이 클라이언트가 아니라 course 서비스 계층에서 처리한다.
@Component
public class KakaoLocalClient {

    private static final Duration SEARCH_CACHE_TTL = Duration.ofHours(6);
    private static final String KAKAO_AUTH_HEADER_PREFIX = "KakaoAK ";

    private final WebClient kakaoLocalApiWebClient;
    private final KakaoLocalProperties kakaoLocalProperties;
    private final TourApiCacheSupport tourApiCacheSupport;

    public KakaoLocalClient(
            @Qualifier("kakaoLocalApiWebClient") WebClient kakaoLocalApiWebClient,
            KakaoLocalProperties kakaoLocalProperties,
            TourApiCacheSupport tourApiCacheSupport
    ) {
        this.kakaoLocalApiWebClient = kakaoLocalApiWebClient;
        this.kakaoLocalProperties = kakaoLocalProperties;
        this.tourApiCacheSupport = tourApiCacheSupport;
    }

    // 키워드로 장소 검색 (v2/local/search/keyword)
    public List<KakaoPlaceDocument> searchByKeyword(String keyword) {
        return tourApiCacheSupport.getOrLoad(
                TourApiRedisKeys.kakaoLocalSearch(keyword),
                SEARCH_CACHE_TTL,
                new TypeReference<>() {
                },
                () -> call(keyword)
        );
    }

    private List<KakaoPlaceDocument> call(String keyword) {
        KakaoLocalSearchResponse response = get(
                uriBuilder -> uriBuilder
                        .path("/v2/local/search/keyword.json")
                        .queryParam("query", keyword)
                        .build(),
                new ParameterizedTypeReference<>() {
                }
        );

        if (response == null || response.documents() == null) {
            throw new GeneralException(TourApiErrorStatus.KAKAO_LOCAL_RESPONSE_INVALID);
        }
        return response.documents();
    }

    private <T> T get(
            Function<UriBuilder, URI> uriFunction,
            ParameterizedTypeReference<T> responseType
    ) {
        try {
            return kakaoLocalApiWebClient.get()
                    .uri(uriFunction::apply)
                    .header(HttpHeaders.AUTHORIZATION, KAKAO_AUTH_HEADER_PREFIX + kakaoLocalProperties.getRestApiKey())
                    .retrieve()
                    .bodyToMono(responseType)
                    .block();
        } catch (WebClientException exception) {
            throw new GeneralException(TourApiErrorStatus.KAKAO_LOCAL_CONNECTION_FAILED, exception);
        }
    }
}
