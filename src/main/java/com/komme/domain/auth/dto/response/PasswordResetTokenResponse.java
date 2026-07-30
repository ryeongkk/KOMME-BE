package com.komme.domain.auth.dto.response;

public record PasswordResetTokenResponse(
        String resetToken
) {

    // 비밀번호 재설정 토큰 응답 생성
    public static PasswordResetTokenResponse of(String resetToken) {
        return new PasswordResetTokenResponse(resetToken);
    }
}
