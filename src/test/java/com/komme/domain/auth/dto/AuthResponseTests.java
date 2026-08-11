package com.komme.domain.auth.dto;

import com.komme.domain.auth.dto.response.LoginResponse;
import com.komme.domain.auth.dto.response.PasswordResetTokenResponse;
import com.komme.domain.auth.dto.response.TokenReissueResponse;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuthResponseTests {

    // 기본 로그인 응답 생성 검증
    @Test
    void loginResponseOfCreatesDefaultOnboardingState() {
        LoginResponse response = LoginResponse.of("access-token", "refresh-token");

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        assertThat(response.profileCompleted()).isFalse();
    }

    // 온보딩 상태 포함 로그인 응답 생성 검증
    @Test
    void loginResponseOfCreatesOnboardingState() {
        LoginResponse response = LoginResponse.of(
                "access-token",
                "refresh-token",
                true
        );

        assertThat(response.profileCompleted()).isTrue();
    }

    // 토큰 재발급 응답 생성 검증
    @Test
    void tokenReissueResponseOfCreatesTokenResponse() {
        TokenReissueResponse response = TokenReissueResponse.of(
                "access-token",
                "refresh-token"
        );

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
    }

    // 비밀번호 재설정 토큰 응답 생성 검증
    @Test
    void passwordResetTokenResponseOfCreatesResetTokenResponse() {
        PasswordResetTokenResponse response = PasswordResetTokenResponse.of("reset-token");

        assertThat(response.resetToken()).isEqualTo("reset-token");
    }
}
