package com.komme.domain.auth.service;

import com.komme.common.exception.GeneralException;
import com.komme.domain.auth.exception.AuthErrorStatus;
import com.komme.domain.auth.jwt.JwtProvider.IssuedToken;
import com.komme.domain.auth.jwt.JwtProvider.TokenClaims;
import com.komme.domain.auth.jwt.JwtRedisKeys;
import com.komme.domain.auth.repository.UserRepository;

import java.util.Set;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RefreshTokenStore {

    private final StringRedisTemplate redisTemplate;
    private final UserRepository userRepository;

    // Refresh Token Redis 저장 기능
    public void save(Long userId, IssuedToken refreshToken) {
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

    // Refresh Token 검증 및 소비 기능
    public void validateAndConsume(TokenClaims tokenClaims) {
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
    public void invalidateAll(Long userId) {
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
}
