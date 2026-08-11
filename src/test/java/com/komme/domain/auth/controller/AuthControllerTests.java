package com.komme.domain.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.komme.common.exception.GeneralException;
import com.komme.common.exception.GeneralExceptionAdvice;
import com.komme.common.response.ApiResponse;
import com.komme.domain.auth.dto.request.EmailVerificationConfirmRequest;
import com.komme.domain.auth.dto.request.EmailVerificationSendRequest;
import com.komme.domain.auth.dto.request.LoginRequest;
import com.komme.domain.auth.dto.request.LogoutRequest;
import com.komme.domain.auth.dto.request.PasswordChangeRequest;
import com.komme.domain.auth.dto.request.PasswordResetConfirmRequest;
import com.komme.domain.auth.dto.request.PasswordResetRequest;
import com.komme.domain.auth.dto.request.PasswordResetSendRequest;
import com.komme.domain.auth.dto.request.SignUpRequest;
import com.komme.domain.auth.dto.request.TermsAgreementRequest;
import com.komme.domain.auth.dto.request.TokenReissueRequest;
import com.komme.domain.auth.dto.response.LoginResponse;
import com.komme.domain.auth.dto.response.PasswordResetTokenResponse;
import com.komme.domain.auth.dto.response.TokenReissueResponse;
import com.komme.domain.auth.exception.AuthErrorStatus;
import com.komme.domain.auth.jwt.JwtProvider.TokenClaims;
import com.komme.domain.auth.service.AuthService;
import com.komme.domain.auth.service.email.EmailVerificationService;
import com.komme.domain.user.enums.Gender;
import com.komme.domain.user.enums.ServiceInterest;
import com.komme.domain.user.service.TermsAgreementService;
import com.komme.domain.i18n.enums.Language;

import java.time.Instant;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
    private TermsAgreementService termsAgreementService;
    private AuthController authController;
    private MockMvc mockMvc;

    // 인증 컨트롤러 테스트 환경 구성
    @BeforeEach
    void setUp() {
        authService = mock(AuthService.class);
        emailVerificationService = mock(EmailVerificationService.class);
        termsAgreementService = mock(TermsAgreementService.class);
        authController = new AuthController(authService, emailVerificationService, termsAgreementService);
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
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
                "newpassword2!"
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

    // 이메일 인증 코드 확인 API 성공 응답 검증
    @Test
    void confirmEmailVerificationReturnsSuccess() throws Exception {
        EmailVerificationConfirmRequest request =
                new EmailVerificationConfirmRequest("user@example.com", "123456");

        mockMvc.perform(post("/api/v1/auth/email-verifications/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true));

        verify(emailVerificationService).confirmVerificationCode(request);
    }

    // 비밀번호 재설정 인증 코드 전송 API 성공 응답 검증
    @Test
    void sendPasswordResetEmailVerificationReturnsSuccess() throws Exception {
        PasswordResetSendRequest request = new PasswordResetSendRequest("user@example.com");

        mockMvc.perform(post("/api/v1/auth/password-resets/email-verifications/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true));

        verify(emailVerificationService).sendPasswordResetVerificationCode(request);
    }

    // 이메일 회원가입 API 성공 응답 검증
    @Test
    void signUpReturnsSuccess() throws Exception {
        SignUpRequest request = new SignUpRequest(
                "user@example.com", "password123!", "nickname", "kr",
                Gender.FEMALE, Language.ENGLISH, Set.of(ServiceInterest.COURSE)
        );

        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true));

        verify(authService).signUp(request);
    }

    // 약관 동의 API가 요청을 Map으로 변환해 서비스에 위임하는지 검증 (직접 호출 - @AuthenticationPrincipal)
    @Test
    void agreeTermsDelegatesToTermsAgreementService() {
        TermsAgreementRequest request = new TermsAgreementRequest(
                true, true, true, true, false, false, true
        );

        ResponseEntity<ApiResponse<Void>> response = authController.agreeTerms(1L, request);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        verify(termsAgreementService).agree(1L, request.toAgreements());
    }

    // 비밀번호 변경 API가 서비스에 위임하는지 검증 (직접 호출 - @AuthenticationPrincipal)
    @Test
    void changePasswordDelegatesToAuthService() {
        PasswordChangeRequest request = new PasswordChangeRequest("oldPassword1!", "newPassword2!");

        ResponseEntity<ApiResponse<Void>> response = authController.changePassword(1L, request);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        verify(authService).changePassword(1L, request);
    }

    // 로그아웃 API가 Authentication에서 TokenClaims를 꺼내 서비스에 위임하는지 검증
    @Test
    void logoutResolvesTokenClaimsAndDelegatesToAuthService() {
        TokenClaims tokenClaims = new TokenClaims(1L, "token-id", Instant.now());
        Authentication authentication = mock(Authentication.class);
        when(authentication.getDetails()).thenReturn(tokenClaims);
        LogoutRequest request = new LogoutRequest("refresh-token");

        ResponseEntity<ApiResponse<Void>> response = authController.logout(1L, authentication, request);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        verify(authService).logout(1L, tokenClaims, request);
    }

    // 로그아웃 API가 Authentication의 details가 TokenClaims가 아니면 INVALID_TOKEN 예외를 던지는지 검증
    @Test
    void logoutThrowsWhenAuthenticationDetailsIsNotTokenClaims() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getDetails()).thenReturn("not-token-claims");
        LogoutRequest request = new LogoutRequest("refresh-token");

        assertThatThrownBy(() -> authController.logout(1L, authentication, request))
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getErrorStatus())
                .isEqualTo(AuthErrorStatus.INVALID_TOKEN);
    }

    // 계정 탈퇴 API가 Authentication에서 TokenClaims를 꺼내 서비스에 위임하는지 검증
    @Test
    void withdrawResolvesTokenClaimsAndDelegatesToAuthService() {
        TokenClaims tokenClaims = new TokenClaims(1L, "token-id", Instant.now());
        Authentication authentication = mock(Authentication.class);
        when(authentication.getDetails()).thenReturn(tokenClaims);

        ResponseEntity<ApiResponse<Void>> response = authController.withdraw(1L, authentication);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        verify(authService).withdraw(1L, tokenClaims);
    }
}
