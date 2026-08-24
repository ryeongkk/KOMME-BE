package com.komme.common.health.dto.response;

public record HealthCheckResponse(
        String status
) {

    private static final String UP = "UP";

    // 정상 상태 응답 생성
    public static HealthCheckResponse up() {
        return new HealthCheckResponse(UP);
    }
}
