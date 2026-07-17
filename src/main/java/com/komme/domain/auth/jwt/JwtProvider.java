package com.komme.domain.auth.jwt;

import com.komme.domain.auth.properties.JwtProperties;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

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
}
