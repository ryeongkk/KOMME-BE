package com.komme.domain.user.dto.response;

public record NicknameAvailabilityResponse(
        boolean available
) {

    // 닉네임 사용 가능 여부 응답 생성
    public static NicknameAvailabilityResponse of(boolean available) {
        return new NicknameAvailabilityResponse(available);
    }
}
