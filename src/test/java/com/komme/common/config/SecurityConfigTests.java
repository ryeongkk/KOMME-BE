package com.komme.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.komme.domain.auth.dto.request.LoginRequest;
import com.komme.domain.auth.dto.request.PasswordChangeRequest;
import com.komme.domain.auth.dto.response.LoginResponse;
import com.komme.domain.auth.jwt.JwtProvider;
import com.komme.domain.auth.service.AuthService;
import com.komme.domain.auth.service.email.EmailVerificationService;
import com.komme.domain.auth.service.token.AccessTokenBlacklistStore;
import com.komme.domain.user.service.TermsAgreementService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTests {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private JwtProvider jwtProvider;

    @MockitoBean
    private AccessTokenBlacklistStore accessTokenBlacklistStore;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private EmailVerificationService emailVerificationService;

    @MockitoBean
    private TermsAgreementService termsAgreementService;

    // 공개 인증 API 보안 통과 검증
    @Test
    void publicAuthEndpointIsPermittedWithoutAccessToken() throws Exception {
        LoginRequest request = new LoginRequest("user@example.com", "password123");
        when(authService.login(request))
                .thenReturn(LoginResponse.of("access-token", "refresh-token"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true));
    }

    // 보호 인증 API Access Token 누락 거부 검증
    @Test
    void protectedAuthEndpointRejectsMissingAccessToken() throws Exception {
        PasswordChangeRequest request = new PasswordChangeRequest(
                "password123",
                "newpassword2"
        );

        mockMvc.perform(patch("/api/v1/auth/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.isSuccess").value(false));
    }
}
