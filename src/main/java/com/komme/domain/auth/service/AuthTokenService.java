package com.komme.domain.auth.service;

import com.komme.domain.auth.dto.response.LoginResponse;
import com.komme.domain.auth.jwt.JwtProvider;
import com.komme.domain.auth.jwt.JwtProvider.IssuedToken;
import com.komme.domain.auth.jwt.JwtRedisKeys;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthTokenService {

    private final JwtProvider jwtProvider;
    private final StringRedisTemplate redisTemplate;

    // 사용자 로그인 토큰 발급 기능
    public LoginResponse issueLoginTokens(Long userId) {
        IssuedToken accessToken = jwtProvider.issueAccessToken(userId);
        IssuedToken refreshToken = jwtProvider.issueRefreshToken(userId);
        saveRefreshToken(userId, refreshToken);

        return LoginResponse.of(accessToken.value(), refreshToken.value());
    }

    // Refresh Token 식별자 Redis 저장 기능
    private void saveRefreshToken(Long userId, IssuedToken refreshToken) {
        redisTemplate.opsForValue().set(
                JwtRedisKeys.refreshToken(refreshToken.id()),
                userId.toString(),
                refreshToken.expiration()
        );
        redisTemplate.opsForSet().add(
                JwtRedisKeys.userRefreshTokens(userId),
                refreshToken.id()
        );
        redisTemplate.expire(
                JwtRedisKeys.userRefreshTokens(userId),
                refreshToken.expiration()
        );
    }
}
