package com.komme.domain.auth.jwt;

import com.komme.common.exception.GeneralException;
import com.komme.domain.auth.exception.AuthErrorStatus;
import com.komme.domain.auth.properties.JwtProperties;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtProvider {

    private static final String TOKEN_TYPE_CLAIM = "tokenType";
    private static final String ACCESS_TOKEN_TYPE = "ACCESS";
    private static final String REFRESH_TOKEN_TYPE = "REFRESH";

    private final JwtProperties jwtProperties;

    // Access Token 발급 기능
    public IssuedToken issueAccessToken(Long userId) {
        return issueToken(
                userId,
                ACCESS_TOKEN_TYPE,
                jwtProperties.getAccessTokenExpiration()
        );
    }

    // Refresh Token 발급 기능
    public IssuedToken issueRefreshToken(Long userId) {
        return issueToken(
                userId,
                REFRESH_TOKEN_TYPE,
                jwtProperties.getRefreshTokenExpiration()
        );
    }

    // Access Token Claim 검증 및 조회 기능
    public TokenClaims parseAccessToken(String token) {
        return parseToken(token, ACCESS_TOKEN_TYPE);
    }

    // Refresh Token Claim 검증 및 조회 기능
    public TokenClaims parseRefreshToken(String token) {
        return parseToken(token, REFRESH_TOKEN_TYPE);
    }

    // 토큰 종류 검증 및 Claim 조회 기능
    private TokenClaims parseToken(String token, String expectedTokenType) {
        Claims claims = parseClaims(token);
        String tokenType = claims.get(TOKEN_TYPE_CLAIM, String.class);

        if (!expectedTokenType.equals(tokenType)) {
            throw new GeneralException(AuthErrorStatus.INVALID_TOKEN);
        }

        try {
            Long userId = Long.valueOf(claims.getSubject());
            String tokenId = claims.getId();

            if (tokenId == null || tokenId.isBlank()) {
                throw new GeneralException(AuthErrorStatus.INVALID_TOKEN);
            }

            return new TokenClaims(userId, tokenId);
        } catch (NumberFormatException exception) {
            throw new GeneralException(AuthErrorStatus.INVALID_TOKEN, exception);
        }
    }

    // JWT 발급 기능
    private IssuedToken issueToken(Long userId, String tokenType, Duration expiration) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(expiration);
        String tokenId = UUID.randomUUID().toString();

        String token = Jwts.builder()
                .id(tokenId)
                .subject(userId.toString())
                .claim(TOKEN_TYPE_CLAIM, tokenType)
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(createSigningKey())
                .compact();

        return new IssuedToken(token, tokenId, expiration);
    }

    // JWT 서명과 만료 검증 및 Claim 조회 기능
    private Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(createSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException exception) {
            throw new GeneralException(AuthErrorStatus.EXPIRED_TOKEN, exception);
        } catch (JwtException | IllegalArgumentException exception) {
            throw new GeneralException(AuthErrorStatus.INVALID_TOKEN, exception);
        }
    }

    // JWT 서명 키 생성 기능
    private SecretKey createSigningKey() {
        return Keys.hmacShaKeyFor(
                jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8)
        );
    }

    public record IssuedToken(
            String value,
            String id,
            Duration expiration
    ) {
    }

    public record TokenClaims(
            Long userId,
            String tokenId
    ) {
    }
}
