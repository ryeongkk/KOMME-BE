package com.komme.domain.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.komme.common.exception.GeneralExceptionAdvice;
import com.komme.domain.auth.dto.request.OAuthAppleLoginRequest;
import com.komme.domain.auth.dto.request.OAuthGoogleLoginRequest;
import com.komme.domain.auth.dto.response.LoginResponse;
import com.komme.domain.auth.service.oauth.OAuthService;

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

class OAuthControllerTests {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private OAuthService oAuthService;
    private MockMvc mockMvc;

    // OAuth 컨트롤러 테스트 환경 구성
    @BeforeEach
    void setUp() {
        oAuthService = mock(OAuthService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new OAuthController(oAuthService))
                .setControllerAdvice(new GeneralExceptionAdvice())
                .build();
    }

    // Google 로그인 API 성공 응답 검증
    @Test
    void loginWithGoogleReturnsLoginResponse() throws Exception {
        OAuthGoogleLoginRequest request = new OAuthGoogleLoginRequest("identity-token");
        when(oAuthService.loginWithGoogle(request))
                .thenReturn(LoginResponse.of("access-token", "refresh-token"));

        mockMvc.perform(post("/api/v1/auth/oauth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"));
    }

    // Apple 로그인 API 성공 응답 검증
    @Test
    void loginWithAppleReturnsLoginResponse() throws Exception {
        OAuthAppleLoginRequest request = new OAuthAppleLoginRequest("identity-token");
        when(oAuthService.loginWithApple(request))
                .thenReturn(LoginResponse.of("apple-access-token", "apple-refresh-token"));

        mockMvc.perform(post("/api/v1/auth/oauth/apple")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("apple-access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("apple-refresh-token"));
    }

    // Google 로그인 API 입력값 오류 응답 검증
    @Test
    void loginWithGoogleRejectsBlankToken() throws Exception {
        OAuthGoogleLoginRequest request = new OAuthGoogleLoginRequest("");

        mockMvc.perform(post("/api/v1/auth/oauth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false));
    }

    // Apple 로그인 API 입력값 오류 응답 검증
    @Test
    void loginWithAppleRejectsBlankToken() throws Exception {
        OAuthAppleLoginRequest request = new OAuthAppleLoginRequest("");

        mockMvc.perform(post("/api/v1/auth/oauth/apple")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false));
    }
}
