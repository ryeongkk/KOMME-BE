package com.komme.domain.auth.service;

import com.komme.common.exception.GeneralException;
import com.komme.domain.auth.exception.AuthErrorStatus;
import com.komme.domain.auth.jwt.JwtProvider.TokenClaims;
import com.komme.domain.auth.jwt.JwtRedisKeys;
import com.komme.domain.auth.repository.UserRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuthTokenStore {

    private final StringRedisTemplate redisTemplate;
    private final UserRepository userRepository;

    // Refresh Token 검증 및 소비 기능
    public void validateAndConsumeRefreshToken(TokenClaims tokenClaims) {
        String savedUserId = redisTemplate.opsForValue().getAndDelete(
                JwtRedisKeys.refreshToken(tokenClaims.tokenId())
        );
        redisTemplate.opsForSet().remove(
                JwtRedisKeys.userRefreshTokens(tokenClaims.userId()),
                tokenClaims.tokenId()
        );

        if (!tokenClaims.userId().toString().equals(savedUserId)
                || !userRepository.existsById(tokenClaims.userId())) {
            throw new GeneralException(AuthErrorStatus.INVALID_TOKEN);
        }
    }

    // 사용자 전체 Refresh Token 폐기 기능
    public void invalidateAllRefreshTokens(Long userId) {
        String userRefreshTokensKey = JwtRedisKeys.userRefreshTokens(userId);
        Set<String> tokenIds = redisTemplate.opsForSet().members(userRefreshTokensKey);

        if (tokenIds != null && !tokenIds.isEmpty()) {
            redisTemplate.delete(
                    tokenIds.stream()
                            .map(JwtRedisKeys::refreshToken)
                            .toList()
            );
        }

        redisTemplate.delete(userRefreshTokensKey);
    }

    // Access Token 남은 만료시간 기반 블랙리스트 등록 기능
    public void blacklistAccessToken(TokenClaims accessTokenClaims) {
        Duration remainingExpiration = Duration.between(
                Instant.now(),
                accessTokenClaims.expiresAt()
        );

        if (!remainingExpiration.isNegative() && !remainingExpiration.isZero()) {
            redisTemplate.opsForValue().set(
                    JwtRedisKeys.accessTokenBlacklist(accessTokenClaims.tokenId()),
                    "true",
                    remainingExpiration
            );
        }
    }
}
