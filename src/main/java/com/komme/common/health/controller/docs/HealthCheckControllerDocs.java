package com.komme.common.health.controller.docs;

import com.komme.common.health.dto.response.HealthCheckResponse;
import com.komme.common.response.ApiResponse;

import org.springframework.http.ResponseEntity;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Health Check", description = "서버 상태 확인 API")
public interface HealthCheckControllerDocs {

    @Operation(summary = "서버 상태 확인", description = "배포 환경에서 서버 실행 상태를 확인합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "서버 상태 확인 성공",
            content = @Content(
                    schema = @Schema(implementation = HealthCheckResponse.class),
                    examples = @ExampleObject(value = HealthCheckApiExamples.HEALTH_CHECK_SUCCESS)
            )
    )
    ResponseEntity<ApiResponse<HealthCheckResponse>> checkHealth();
}
