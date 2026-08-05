package com.komme.domain.auth.jwt;

import com.komme.common.exception.GeneralException;
import com.komme.domain.auth.exception.AuthErrorStatus;
import com.komme.domain.auth.jwt.JwtProvider.TokenClaims;
import com.komme.domain.auth.service.token.AccessTokenBlacklistStore;

import java.time.Instant;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTests {

    private static final Long USER_ID = 1L;
    private static final String ACCESS_TOKEN = "access-token";

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private AccessTokenBlacklistStore accessTokenBlacklistStore;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter jwtAuthenticationFilter;

    // JWT 인증 필터 테스트 환경 구성
    @BeforeEach
    void setUp() {
        jwtAuthenticationFilter = new JwtAuthenticationFilter(
                jwtProvider,
                accessTokenBlacklistStore
        );
    }

    // SecurityContext 정리 기능
    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    // Access Token 기반 principal과 details 등록 검증
    @Test
    void filterRegistersAuthentication() throws Exception {
        TokenClaims tokenClaims = createTokenClaims();
        when(request.getHeader("Authorization")).thenReturn("Bearer " + ACCESS_TOKEN);
        when(jwtProvider.parseAccessToken(ACCESS_TOKEN)).thenReturn(tokenClaims);


        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication.getPrincipal()).isEqualTo(USER_ID);
        assertThat(authentication.getDetails()).isEqualTo(tokenClaims);
        assertThat(authentication.isAuthenticated()).isTrue();
        verify(filterChain).doFilter(request, response);
    }

    // 블랙리스트 Access Token 인증 거부 검증
    @Test
    void filterRejectsBlacklistedAccessToken() throws Exception {
        TokenClaims tokenClaims = createTokenClaims();
        when(request.getHeader("Authorization")).thenReturn("Bearer " + ACCESS_TOKEN);
        when(jwtProvider.parseAccessToken(ACCESS_TOKEN)).thenReturn(tokenClaims);
        org.mockito.Mockito.doThrow(new GeneralException(AuthErrorStatus.INVALID_TOKEN))
                .when(accessTokenBlacklistStore)
                .validateNotBlacklisted(tokenClaims);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(request).setAttribute(
                JwtAuthenticationFilter.AUTH_ERROR_STATUS_ATTRIBUTE,
                AuthErrorStatus.INVALID_TOKEN
        );
        verify(filterChain).doFilter(request, response);
    }

    // 만료된 Access Token 오류 전달 검증
    @Test
    void filterStoresExpiredTokenError() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + ACCESS_TOKEN);
        when(jwtProvider.parseAccessToken(ACCESS_TOKEN))
                .thenThrow(new GeneralException(AuthErrorStatus.EXPIRED_TOKEN));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(request).setAttribute(
                JwtAuthenticationFilter.AUTH_ERROR_STATUS_ATTRIBUTE,
                AuthErrorStatus.EXPIRED_TOKEN
        );
        verify(filterChain).doFilter(request, response);
    }

    // Authorization Header 미제공 요청 통과 검증
    @Test
    void filterContinuesWithoutAuthorizationHeader() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(jwtProvider, never()).parseAccessToken(org.mockito.Mockito.any());
        verify(accessTokenBlacklistStore, never()).validateNotBlacklisted(org.mockito.Mockito.any());
        verify(filterChain).doFilter(request, response);
    }

    // Bearer 형식이 아닌 Authorization Header 요청 통과 검증
    @Test
    void filterContinuesWithoutBearerAuthorizationHeader() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Basic token");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(jwtProvider, never()).parseAccessToken(org.mockito.Mockito.any());
        verify(accessTokenBlacklistStore, never()).validateNotBlacklisted(org.mockito.Mockito.any());
        verify(filterChain).doFilter(request, response);
    }

    // Access Token Claim 생성
    private TokenClaims createTokenClaims() {
        return new TokenClaims(USER_ID, "access-id", Instant.now().plusSeconds(3600));
    }
}
