package com.komme.domain.auth.dto.response;

public record TokenReissueResponse(
        String accessToken,
        String refreshToken
) {

    // 재발급 토큰 응답 생성 기능
    public static TokenReissueResponse of(String accessToken, String refreshToken) {
        return new TokenReissueResponse(accessToken, refreshToken);
    }
}
