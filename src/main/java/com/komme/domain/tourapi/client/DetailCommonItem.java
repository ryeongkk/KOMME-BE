package com.komme.domain.tourapi.client;

import com.fasterxml.jackson.annotation.JsonProperty;

// KorService2 detailCommon2(공통정보 조회) 응답 item
public record DetailCommonItem(
        @JsonProperty("contentid") String contentId,
        @JsonProperty("contenttypeid") String contentTypeId,
        @JsonProperty("title") String title,
        @JsonProperty("overview") String overview,
        @JsonProperty("homepage") String homepage,
        @JsonProperty("tel") String tel,
        @JsonProperty("addr1") String address
) {
}
