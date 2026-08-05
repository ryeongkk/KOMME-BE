package com.komme.domain.tourapi.client;

import com.fasterxml.jackson.annotation.JsonProperty;

// 관광지 집중률 방문자 추이 예측 정보 서비스(TatsCnctrRateService.tatsCnctrRatedList) 응답 item
// 관광지명(tAtsNm) 텍스트로만 식별되고 contentId 연동은 안 된다 - Spot과의 매칭은 이름 기반 best-effort로 처리
public record ConcentrationRateItem(
        @JsonProperty("baseYmd") String baseDate,
        @JsonProperty("areaCd") String areaCode,
        @JsonProperty("areaNm") String areaName,
        @JsonProperty("signguCd") String sigunguCode,
        @JsonProperty("signguNm") String sigunguName,
        @JsonProperty("tAtsNm") String touristSpotName,
        @JsonProperty("cnctrRate") Double concentrationRate
) {
}
