package com.komme.domain.auth.jwt;

import com.komme.common.exception.GeneralException;
import com.komme.domain.auth.exception.AuthErrorStatus;
import com.komme.domain.auth.jwt.JwtProvider.IssuedToken;
import com.komme.domain.auth.jwt.JwtProvider.TokenClaims;
import com.komme.domain.auth.properties.JwtProperties;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtProviderTests {

    private static final Long USER_ID = 1L;
    private static final String SECRET =
            "test-jwt-secret-key-test-jwt-secret-key-1234567890";

    private JwtProvider jwtProvider;

    // JWT Provider 테스트 환경 구성
    @BeforeEach
    void setUp() {
        jwtProvider = new JwtProvider(
                new JwtProperties(SECRET, Duration.ofHours(1), Duration.ofDays(14))
        );
    }

    // Access Token 발급과 Claim 파싱 검증
    @Test
    void issueAndParseAccessToken() {
        IssuedToken issuedToken = jwtProvider.issueAccessToken(USER_ID);

        TokenClaims tokenClaims = jwtProvider.parseAccessToken(issuedToken.value());

        assertThat(tokenClaims.userId()).isEqualTo(USER_ID);
        assertThat(tokenClaims.tokenId()).isEqualTo(issuedToken.id());
        assertThat(tokenClaims.expiresAt()).isAfter(Instant.now());
        assertThat(issuedToken.expiration()).isEqualTo(Duration.ofHours(1));
    }

    // Refresh Token 발급과 Claim 파싱 검증
    @Test
    void issueAndParseRefreshToken() {
        IssuedToken issuedToken = jwtProvider.issueRefreshToken(USER_ID);

        TokenClaims tokenClaims = jwtProvider.parseRefreshToken(issuedToken.value());

        assertThat(tokenClaims.userId()).isEqualTo(USER_ID);
        assertThat(tokenClaims.tokenId()).isEqualTo(issuedToken.id());
        assertThat(issuedToken.expiration()).isEqualTo(Duration.ofDays(14));
    }

    // Access Token 위치의 Refresh Token 사용 거부 검증
    @Test
    void parseAccessTokenRejectsRefreshToken() {
        IssuedToken refreshToken = jwtProvider.issueRefreshToken(USER_ID);

        assertThatThrownBy(() -> jwtProvider.parseAccessToken(refreshToken.value()))
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getErrorStatus())
                .isEqualTo(AuthErrorStatus.INVALID_TOKEN);
    }

    // Refresh Token 위치의 Access Token 사용 거부 검증
    @Test
    void parseRefreshTokenRejectsAccessToken() {
        IssuedToken accessToken = jwtProvider.issueAccessToken(USER_ID);

        assertThatThrownBy(() -> jwtProvider.parseRefreshToken(accessToken.value()))
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getErrorStatus())
                .isEqualTo(AuthErrorStatus.INVALID_TOKEN);
    }

    // JWT ID 누락 토큰 거부 검증
    @Test
    void parseTokenRejectsMissingTokenId() {
        String token = createCustomToken(USER_ID.toString(), true);

        assertThatThrownBy(() -> jwtProvider.parseAccessToken(token))
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getErrorStatus())
                .isEqualTo(AuthErrorStatus.INVALID_TOKEN);
    }

    // JWT subject 숫자 형식 오류 거부 검증
    @Test
    void parseTokenRejectsInvalidSubject() {
        String token = createCustomToken("not-number", false);

        assertThatThrownBy(() -> jwtProvider.parseAccessToken(token))
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getErrorStatus())
                .isEqualTo(AuthErrorStatus.INVALID_TOKEN);
    }

    // 변조된 JWT 거부 검증
    @Test
    void parseTokenRejectsTamperedToken() {
        IssuedToken accessToken = jwtProvider.issueAccessToken(USER_ID);
        String[] tokenParts = accessToken.value().split("\\.");
        char firstPayloadCharacter = tokenParts[1].charAt(0);
        tokenParts[1] = (firstPayloadCharacter == 'A' ? "B" : "A")
                + tokenParts[1].substring(1);
        String tamperedToken = String.join(".", tokenParts);

        assertThatThrownBy(() -> jwtProvider.parseAccessToken(tamperedToken))
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getErrorStatus())
                .isEqualTo(AuthErrorStatus.INVALID_TOKEN);
    }

    // 만료된 JWT 거부 검증
    @Test
    void parseTokenRejectsExpiredToken() {
        JwtProvider expiredTokenProvider = new JwtProvider(
                new JwtProperties(SECRET, Duration.ofSeconds(-1), Duration.ofDays(14))
        );
        IssuedToken expiredToken = expiredTokenProvider.issueAccessToken(USER_ID);

        assertThatThrownBy(() -> expiredTokenProvider.parseAccessToken(expiredToken.value()))
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getErrorStatus())
                .isEqualTo(AuthErrorStatus.EXPIRED_TOKEN);
    }

    // 테스트 JWT 직접 생성
    private String createCustomToken(String subject, boolean omitTokenId) {
        Instant now = Instant.now();
        var builder = Jwts.builder()
                .subject(subject)
                .claim("tokenType", "ACCESS")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(300)));

        if (!omitTokenId) {
            builder.id("token-id");
        }

        return builder.signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }
}
