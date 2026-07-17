package com.komme.domain.auth.client;

import com.komme.common.exception.GeneralException;
import com.komme.domain.auth.exception.AuthErrorStatus;
import com.komme.domain.auth.properties.AppleProperties;

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
public class OAuthAppleClient {

    private static final String APPLE_ISSUER = "https://appleid.apple.com";
    private static final String RS256_ALGORITHM = "RS256";

    private final AppleJwksProvider appleJwksProvider;
    private final OAuthPublicKeyFactory oAuthPublicKeyFactory;
    private final AppleProperties appleProperties;

    // Apple identity token 검증 및 사용자 정보 조회 기능
    public AppleIdentity verifyIdentityToken(String identityToken) {
        try {
            Claims claims = parseClaims(identityToken);
            String subject = claims.getSubject();
            validateSubject(subject);
            return new AppleIdentity(subject, resolveVerifiedEmail(claims));
        } catch (JwtException | IllegalArgumentException exception) {
            throw new GeneralException(
                    AuthErrorStatus.INVALID_APPLE_IDENTITY_TOKEN,
                    exception
            );
        }
    }

    // Apple identity token 서명과 필수 Claim 검증 기능
    private Claims parseClaims(String identityToken) {
        return Jwts.parser()
                .keyLocator(this::locateSigningKey)
                .requireIssuer(APPLE_ISSUER)
                .requireAudience(appleProperties.getClientId())
                .build()
                .parseSignedClaims(identityToken)
                .getPayload();
    }

    // identity token Header 기반 Apple 공개키 생성 기능
    private Key locateSigningKey(Header header) {
        if (!(header instanceof ProtectedHeader protectedHeader)
                || !RS256_ALGORITHM.equals(protectedHeader.getAlgorithm())) {
            throw new IllegalArgumentException("Unsupported Apple token header");
        }

        OAuthJwk oAuthJwk = appleJwksProvider.getSigningJwk(protectedHeader.getKeyId());
        return oAuthPublicKeyFactory.create(oAuthJwk);
    }

    // Apple 사용자 식별자 필수값 검증 기능
    private void validateSubject(String subject) {
        if (subject == null || subject.isBlank()) {
            throw new GeneralException(AuthErrorStatus.INVALID_APPLE_IDENTITY_TOKEN);
        }
    }

    // Apple 인증 완료 이메일 조회 기능
    private String resolveVerifiedEmail(Claims claims) {
        String email = claims.get("email", String.class);

        if (email == null || email.isBlank()) {
            return null;
        }

        Object emailVerified = claims.get("email_verified");
        if (!Boolean.parseBoolean(String.valueOf(emailVerified))) {
            throw new GeneralException(AuthErrorStatus.INVALID_APPLE_IDENTITY_TOKEN);
        }

        return email;
    }

    public record AppleIdentity(String subject, String email) {
    }
}
