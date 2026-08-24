package com.komme.common.health.controller.docs;

public final class HealthCheckApiExamples {

    public static final String HEALTH_CHECK_SUCCESS = """
            {
              "isSuccess": true,
              "code": "COM_200",
              "message": "성공적으로 처리되었습니다.",
              "data": {
                "status": "UP"
              }
            }
            """;

    // 인스턴스 생성 방지
    private HealthCheckApiExamples() {
    }
}
