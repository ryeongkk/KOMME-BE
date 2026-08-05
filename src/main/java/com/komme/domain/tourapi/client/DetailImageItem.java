package com.komme.domain.tourapi.client;

import com.fasterxml.jackson.annotation.JsonProperty;

// KorService2 detailImage2(이미지정보 조회) 응답 item
public record DetailImageItem(
        @JsonProperty("contentid") String contentId,
        @JsonProperty("originimgurl") String originImageUrl,
        @JsonProperty("smallimageurl") String thumbnailImageUrl
) {
}
