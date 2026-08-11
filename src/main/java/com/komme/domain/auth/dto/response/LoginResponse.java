package com.komme.domain.auth.dto.response;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        boolean profileCompleted
) {

    // 로그인 토큰 응답 생성 기능
    public static LoginResponse of(
            String accessToken,
            String refreshToken
    ) {
        return new LoginResponse(
                accessToken,
                refreshToken,
                false
        );
    }

    // 로그인 토큰과 온보딩 상태 응답 생성 기능
    public static LoginResponse of(
            String accessToken,
            String refreshToken,
            boolean profileCompleted
    ) {
        return new LoginResponse(
                accessToken,
                refreshToken,
                profileCompleted
        );
    }
}
