package com.komme.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.komme.domain.auth.dto.request.LoginRequest;
import com.komme.domain.auth.dto.request.LogoutRequest;
import com.komme.domain.auth.dto.request.OAuthProfileCompleteRequest;
import com.komme.domain.auth.dto.request.PasswordChangeRequest;
import com.komme.domain.auth.dto.response.LoginResponse;
import com.komme.domain.auth.jwt.JwtProvider;
import com.komme.domain.auth.jwt.JwtProvider.TokenClaims;
import com.komme.domain.auth.service.AuthService;
import com.komme.domain.auth.service.email.EmailVerificationService;
import com.komme.domain.auth.service.oauth.OAuthService;
import com.komme.domain.auth.service.token.AccessTokenBlacklistStore;
import com.komme.domain.i18n.enums.Language;
import com.komme.domain.user.dto.response.UserProfileResponse;
import com.komme.domain.user.enums.Provider;
import com.komme.domain.user.service.UserProfileService;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
    private OAuthService oAuthService;

    @MockitoBean
    private EmailVerificationService emailVerificationService;

    @MockitoBean
    private UserProfileService userProfileService;

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

    // 보호 인증 API Access Token 인증 성공 검증
    @Test
    void protectedAuthEndpointPassesAuthenticatedUserId() throws Exception {
        PasswordChangeRequest request = new PasswordChangeRequest(
                "password123",
                "newpassword2!"
        );
        TokenClaims tokenClaims = createTokenClaims();
        when(jwtProvider.parseAccessToken("access-token")).thenReturn(tokenClaims);

        mockMvc.perform(patch("/api/v1/auth/password")
                        .header("Authorization", "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true));

        verify(accessTokenBlacklistStore).validateNotBlacklisted(tokenClaims);
        verify(authService).changePassword(1L, request);
    }

    // 로그아웃 API 인증 정보 전달 검증
    @Test
    void logoutEndpointPassesTokenClaims() throws Exception {
        LogoutRequest request = new LogoutRequest("refresh-token");
        TokenClaims tokenClaims = createTokenClaims();
        when(jwtProvider.parseAccessToken("access-token")).thenReturn(tokenClaims);

        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true));

        verify(authService).logout(1L, tokenClaims, request);
    }

    // 계정 탈퇴 API 인증 정보 전달 검증
    @Test
    void withdrawEndpointPassesTokenClaims() throws Exception {
        TokenClaims tokenClaims = createTokenClaims();
        when(jwtProvider.parseAccessToken("access-token")).thenReturn(tokenClaims);

        mockMvc.perform(delete("/api/v1/auth/withdraw")
                        .header("Authorization", "Bearer access-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true));

        verify(authService).withdraw(1L, tokenClaims);
    }

    // 보호 사용자 API Access Token 인증 성공 검증
    @Test
    void protectedUserEndpointPassesAuthenticatedUserId() throws Exception {
        TokenClaims tokenClaims = createTokenClaims();
        when(jwtProvider.parseAccessToken("access-token")).thenReturn(tokenClaims);
        when(userProfileService.getMyProfile(1L)).thenReturn(new UserProfileResponse(
                "nickname",
                Provider.LOCAL,
                Language.ENGLISH,
                false
        ));

        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer access-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data.nickname").value("nickname"));

        verify(userProfileService).getMyProfile(1L);
    }

    // 보호 OAuth 프로필 API Access Token 인증 성공 검증
    @Test
    void protectedOAuthProfileEndpointPassesAuthenticatedUserId() throws Exception {
        OAuthProfileCompleteRequest request = new OAuthProfileCompleteRequest("nickname");
        TokenClaims tokenClaims = createTokenClaims();
        when(jwtProvider.parseAccessToken("access-token")).thenReturn(tokenClaims);

        mockMvc.perform(patch("/api/v1/auth/oauth/profile")
                        .header("Authorization", "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true));

        verify(oAuthService).completeProfile(1L, request);
    }

    // 테스트 Access Token Claim 생성
    private TokenClaims createTokenClaims() {
        return new TokenClaims(
                1L,
                "access-id",
                Instant.now().plus(Duration.ofHours(1))
        );
    }
}
