package com.komme.common.health.controller;

import com.komme.common.base.status.SuccessStatus;
import com.komme.common.health.controller.docs.HealthCheckControllerDocs;
import com.komme.common.health.dto.response.HealthCheckResponse;
import com.komme.common.response.ApiResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController implements HealthCheckControllerDocs {

    // 서버 상태 확인 기능
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<HealthCheckResponse>> checkHealth() {
        return ApiResponse.success(
                SuccessStatus.COMMON_SUCCESS_STATUS,
                HealthCheckResponse.up()
        );
    }
}
