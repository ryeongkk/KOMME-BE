package com.komme.domain.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.komme.common.exception.GeneralExceptionAdvice;
import com.komme.domain.auth.dto.request.EmailVerificationSendRequest;
import com.komme.domain.auth.dto.request.LoginRequest;
import com.komme.domain.auth.dto.request.PasswordResetConfirmRequest;
import com.komme.domain.auth.dto.request.PasswordResetRequest;
import com.komme.domain.auth.dto.request.TokenReissueRequest;
import com.komme.domain.auth.dto.response.LoginResponse;
import com.komme.domain.auth.dto.response.PasswordResetTokenResponse;
import com.komme.domain.auth.dto.response.TokenReissueResponse;
import com.komme.domain.auth.service.AuthService;
import com.komme.domain.auth.service.email.EmailVerificationService;
import com.komme.domain.user.service.TermsAgreementService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTests {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private AuthService authService;
    private EmailVerificationService emailVerificationService;
    private MockMvc mockMvc;

    // 인증 컨트롤러 테스트 환경 구성
    @BeforeEach
    void setUp() {
        authService = mock(AuthService.class);
        emailVerificationService = mock(EmailVerificationService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(
                        authService,
                        emailVerificationService,
                        mock(TermsAgreementService.class)
                ))
                .setControllerAdvice(new GeneralExceptionAdvice())
                .build();
    }

    // 이메일 인증 코드 전송 API 성공 응답 검증
    @Test
    void sendEmailVerificationReturnsSuccess() throws Exception {
        EmailVerificationSendRequest request =
                new EmailVerificationSendRequest("user@example.com");

        mockMvc.perform(post("/api/v1/auth/email-verifications/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true));

        verify(emailVerificationService).sendVerificationCode(request);
    }

    // 비밀번호 재설정 인증 확인 API 토큰 응답 검증
    @Test
    void confirmPasswordResetEmailVerificationReturnsResetToken() throws Exception {
        PasswordResetConfirmRequest request = new PasswordResetConfirmRequest(
                "user@example.com",
                "123456"
        );
        when(emailVerificationService.confirmPasswordResetVerificationCode(request))
                .thenReturn(PasswordResetTokenResponse.of("reset-token"));

        mockMvc.perform(post("/api/v1/auth/password-resets/email-verifications/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data.resetToken").value("reset-token"));
    }

    // 비밀번호 재설정 API 성공 응답 검증
    @Test
    void resetPasswordReturnsSuccess() throws Exception {
        PasswordResetRequest request = new PasswordResetRequest(
                "reset-token",
                "newpassword2"
        );

        mockMvc.perform(patch("/api/v1/auth/password-resets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true));

        verify(authService).resetPassword(request);
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

    // 토큰 재발급 API 성공 응답 검증
    @Test
    void reissueTokenReturnsTokenResponse() throws Exception {
        TokenReissueRequest request = new TokenReissueRequest("refresh-token");
        when(authService.reissueToken(request))
                .thenReturn(TokenReissueResponse.of("new-access-token", "new-refresh-token"));

        mockMvc.perform(post("/api/v1/auth/tokens/reissue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("new-refresh-token"));
    }
}
