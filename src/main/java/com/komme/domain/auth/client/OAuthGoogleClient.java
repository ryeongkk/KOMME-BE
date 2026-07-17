package com.komme.domain.auth.client;

import com.komme.common.exception.GeneralException;
import com.komme.domain.auth.exception.AuthErrorStatus;
import com.komme.domain.auth.properties.GoogleProperties;

import java.security.Key;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.ProtectedHeader;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OAuthGoogleClient {

    private static final String GOOGLE_ISSUER = "https://accounts.google.com";
    private static final String LEGACY_GOOGLE_ISSUER = "accounts.google.com";
    private static final String RS256_ALGORITHM = "RS256";

    private final GoogleJwksProvider googleJwksProvider;
    private final GooglePublicKeyFactory googlePublicKeyFactory;
    private final GoogleProperties googleProperties;

    // Google identity token 검증 및 사용자 정보 조회 기능
    public GoogleIdentity verifyIdentityToken(String identityToken) {
        try {
            Claims claims = parseClaims(identityToken);
            String subject = claims.getSubject();
            validateSubject(subject);
            return new GoogleIdentity(subject, resolveVerifiedEmail(claims));
        } catch (JwtException | IllegalArgumentException exception) {
            throw new GeneralException(
                    AuthErrorStatus.INVALID_GOOGLE_IDENTITY_TOKEN,
                    exception
            );
        }
    }

    // Google identity token 서명과 필수 Claim 검증 기능
    private Claims parseClaims(String identityToken) {
        Claims claims = Jwts.parser()
                .keyLocator(this::locateSigningKey)
                .requireAudience(googleProperties.getClientId())
                .build()
                .parseSignedClaims(identityToken)
                .getPayload();

        validateIssuer(claims.getIssuer());
        return claims;
    }

    // identity token Header 기반 Google 공개키 생성 기능
    private Key locateSigningKey(Header header) {
        if (!(header instanceof ProtectedHeader protectedHeader)
                || !RS256_ALGORITHM.equals(protectedHeader.getAlgorithm())) {
            throw new IllegalArgumentException("Unsupported Google token header");
        }

        GoogleJwk googleJwk = googleJwksProvider.getSigningJwk(protectedHeader.getKeyId());
        return googlePublicKeyFactory.create(googleJwk);
    }

    // Google 발급자 Claim 검증 기능
    private void validateIssuer(String issuer) {
        if (!GOOGLE_ISSUER.equals(issuer) && !LEGACY_GOOGLE_ISSUER.equals(issuer)) {
            throw new GeneralException(AuthErrorStatus.INVALID_GOOGLE_IDENTITY_TOKEN);
        }
    }

    // Google 사용자 식별자 필수값 검증 기능
    private void validateSubject(String subject) {
        if (subject == null || subject.isBlank()) {
            throw new GeneralException(AuthErrorStatus.INVALID_GOOGLE_IDENTITY_TOKEN);
        }
    }

    // Google 인증 완료 이메일 조회 기능
    private String resolveVerifiedEmail(Claims claims) {
        String email = claims.get("email", String.class);

        if (email == null || email.isBlank()) {
            return null;
        }

        Object emailVerified = claims.get("email_verified");
        if (!Boolean.parseBoolean(String.valueOf(emailVerified))) {
            throw new GeneralException(AuthErrorStatus.INVALID_GOOGLE_IDENTITY_TOKEN);
        }

        return email;
    }

    public record GoogleIdentity(String subject, String email) {
    }
}
