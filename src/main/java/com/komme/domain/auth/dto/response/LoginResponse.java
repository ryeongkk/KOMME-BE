package com.komme.domain.auth.dto.response;

public record LoginResponse(
        String accessToken,
        String refreshToken
) {

    // 로그인 토큰 응답 생성 기능
    public static LoginResponse of(
            String accessToken,
            String refreshToken
    ) {
        return new LoginResponse(
                accessToken,
                refreshToken
        );
    }
}
