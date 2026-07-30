package com.komme.domain.auth.service.token;

import com.komme.common.exception.GeneralException;
import com.komme.domain.auth.exception.AuthErrorStatus;
import com.komme.domain.auth.jwt.JwtProvider.TokenClaims;
import com.komme.domain.auth.jwt.JwtRedisKeys;

import java.time.Duration;
import java.time.Instant;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AccessTokenBlacklistStore {

    private final StringRedisTemplate redisTemplate;

    // Access Token 남은 만료시간 기반 블랙리스트 등록 기능
    public void blacklist(TokenClaims accessTokenClaims) {
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

    // Access Token 블랙리스트 등록 여부 검증 기능
    public void validateNotBlacklisted(TokenClaims accessTokenClaims) {
        if (Boolean.TRUE.equals(redisTemplate.hasKey(
                JwtRedisKeys.accessTokenBlacklist(accessTokenClaims.tokenId())
        ))) {
            throw new GeneralException(AuthErrorStatus.INVALID_TOKEN);
        }
    }
}
