package com.komme.domain.auth.jwt;

import com.komme.common.exception.GeneralException;
import com.komme.domain.auth.exception.AuthErrorStatus;
import com.komme.domain.auth.jwt.JwtProvider.TokenClaims;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String AUTH_ERROR_STATUS_ATTRIBUTE = "authErrorStatus";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtProvider jwtProvider;
    private final StringRedisTemplate redisTemplate;

    // Access Token 검증 및 인증 정보 등록 기능
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String accessToken = resolveAccessToken(request);

        if (accessToken != null) {
            authenticate(request, accessToken);
        }

        filterChain.doFilter(request, response);
    }

    // Authorization Header Access Token 조회 기능
    private String resolveAccessToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            return null;
        }

        return authorizationHeader.substring(BEARER_PREFIX.length());
    }

    // Access Token 기반 사용자 인증 기능
    private void authenticate(HttpServletRequest request, String accessToken) {
        try {
            TokenClaims tokenClaims = jwtProvider.parseAccessToken(accessToken);
            validateNotBlacklisted(tokenClaims);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            tokenClaims.userId(),
                            null,
                            List.of()
                    );
            authentication.setDetails(tokenClaims);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (GeneralException exception) {
            SecurityContextHolder.clearContext();
            request.setAttribute(AUTH_ERROR_STATUS_ATTRIBUTE, exception.getErrorStatus());
        }
    }

    // Access Token 블랙리스트 미등록 검증 기능
    private void validateNotBlacklisted(TokenClaims tokenClaims) {
        if (Boolean.TRUE.equals(redisTemplate.hasKey(
                JwtRedisKeys.accessTokenBlacklist(tokenClaims.tokenId())
        ))) {
            throw new GeneralException(AuthErrorStatus.INVALID_TOKEN);
        }
    }
}
