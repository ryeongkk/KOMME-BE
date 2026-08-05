package com.komme.domain.course.client;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

// 카카오 로컬 API 키워드로 장소 검색 응답
public record KakaoLocalSearchResponse(
        @JsonProperty("documents") List<KakaoPlaceDocument> documents,
        @JsonProperty("meta") Meta meta
) {

    public record Meta(
            @JsonProperty("total_count") Integer totalCount,
            @JsonProperty("pageable_count") Integer pageableCount,
            @JsonProperty("is_end") Boolean isEnd
    ) {
    }
}
