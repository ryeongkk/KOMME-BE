package com.komme.domain.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.komme.common.exception.GeneralExceptionAdvice;
import com.komme.domain.auth.dto.request.LoginRequest;
import com.komme.domain.auth.dto.response.LoginResponse;
import com.komme.domain.auth.service.AuthService;
import com.komme.domain.auth.service.email.EmailVerificationService;
import com.komme.domain.user.service.TermsAgreementService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTests {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private AuthService authService;
    private MockMvc mockMvc;

    // 인증 컨트롤러 테스트 환경 구성
    @BeforeEach
    void setUp() {
        authService = mock(AuthService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(
                        authService,
                        mock(EmailVerificationService.class),
                        mock(TermsAgreementService.class)
                ))
                .setControllerAdvice(new GeneralExceptionAdvice())
                .build();
    }

    // 로그인 API 성공 응답 검증
    @Test
    void loginReturnsLoginResponse() throws Exception {
        LoginRequest request = new LoginRequest("user@example.com", "password123");
        when(authService.login(request))
                .thenReturn(LoginResponse.of("access-token", "refresh-token", true, true));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.data.profileCompleted").value(true))
                .andExpect(jsonPath("$.data.termsAgreed").value(true));
    }

    // 로그인 API 입력값 오류 응답 검증
    @Test
    void loginRejectsInvalidRequest() throws Exception {
        LoginRequest request = new LoginRequest("invalid-email", "");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false));
    }
}
