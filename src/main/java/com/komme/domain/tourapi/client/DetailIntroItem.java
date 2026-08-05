package com.komme.domain.tourapi.client;

import com.fasterxml.jackson.annotation.JsonProperty;

// KorService2 detailIntro2(소개정보 조회) 응답 item
// ⚠ 실제 필드는 contentTypeId(관광지/음식점/숙박 등)별로 이름이 달라진다 (예: 음식점은 opentimefood/restdatefood,
// 관광지는 usetime/restdate). 여기 있는 필드는 공통으로 자주 쓰이는 것만 우선 반영했고,
// 실제 호출 결과를 보면서 contentTypeId별 필드를 Stage 4에서 추가/보정한다.
public record DetailIntroItem(
        @JsonProperty("contentid") String contentId,
        @JsonProperty("contenttypeid") String contentTypeId,
        @JsonProperty("usetime") String useTime,
        @JsonProperty("restdate") String restDate,
        @JsonProperty("infocenter") String infoCenter
) {
}
