package com.komme.common.health.controller;

import com.komme.common.health.dto.response.HealthCheckResponse;
import com.komme.common.response.ApiResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class HealthCheckControllerTests {

    private HealthCheckController healthCheckController;
    private MockMvc mockMvc;

    // Health Check 컨트롤러 테스트 환경 구성
    @BeforeEach
    void setUp() {
        healthCheckController = new HealthCheckController();
        mockMvc = MockMvcBuilders.standaloneSetup(healthCheckController)
                .build();
    }

    // 서버 상태 확인 API 응답 객체 검증
    @Test
    void checkHealthReturnsUpStatus() {
        ResponseEntity<ApiResponse<HealthCheckResponse>> response =
                healthCheckController.checkHealth();

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody().getData().status()).isEqualTo("UP");
    }

    // 서버 상태 확인 API JSON 응답 검증
    @Test
    void checkHealthReturnsSuccessResponse() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("COM_200"))
                .andExpect(jsonPath("$.data.status").value("UP"));
    }
}
