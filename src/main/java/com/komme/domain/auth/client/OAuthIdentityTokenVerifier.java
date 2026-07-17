package com.komme.domain.auth.client;

import com.komme.common.exception.GeneralException;
import com.komme.domain.auth.exception.AuthErrorStatus;

import java.security.Key;
import java.util.Set;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.ProtectedHeader;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OAuthIdentityTokenVerifier {

    private static final String RS256_ALGORITHM = "RS256";

    private final OAuthPublicKeyFactory oAuthPublicKeyFactory;

    // OAuth identity token 검증 및 사용자 정보 추출 기능
    public OAuthIdentity verify(
            String identityToken,
            AbstractOAuthJwksProvider jwksProvider,
            String clientId,
            Set<String> issuers,
            AuthErrorStatus invalidTokenStatus
    ) {
        try {
            Claims claims = parseClaims(
                    identityToken,
                    jwksProvider,
                    clientId,
                    issuers
            );
            String subject = claims.getSubject();
            validateSubject(subject);
            return new OAuthIdentity(subject, resolveVerifiedEmail(claims));
        } catch (JwtException | IllegalArgumentException exception) {
            throw new GeneralException(invalidTokenStatus, exception);
        }
    }

    // OAuth identity token 서명과 기본 Claim 검증 기능
    private Claims parseClaims(
            String identityToken,
            AbstractOAuthJwksProvider jwksProvider,
            String clientId,
            Set<String> issuers
    ) {
        Claims claims = Jwts.parser()
                .keyLocator(header -> locateSigningKey(header, jwksProvider))
                .requireAudience(clientId)
                .build()
                .parseSignedClaims(identityToken)
                .getPayload();

        validateIssuer(claims.getIssuer(), issuers);
        return claims;
    }

    // identity token Header 기반 OAuth 공개키 생성 기능
    private Key locateSigningKey(
            Header header,
            AbstractOAuthJwksProvider jwksProvider
    ) {
        if (!(header instanceof ProtectedHeader protectedHeader)
                || !RS256_ALGORITHM.equals(protectedHeader.getAlgorithm())) {
            throw new IllegalArgumentException("Unsupported OAuth token header");
        }

        OAuthJwk oAuthJwk = jwksProvider.getSigningJwk(protectedHeader.getKeyId());
        return oAuthPublicKeyFactory.create(oAuthJwk);
    }

    // OAuth 발급자 Claim 검증 기능
    private void validateIssuer(String issuer, Set<String> issuers) {
        if (issuer == null || !issuers.contains(issuer)) {
            throw new IllegalArgumentException("Invalid OAuth token issuer");
        }
    }

    // OAuth 사용자 식별자 필수값 검증 기능
    private void validateSubject(String subject) {
        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException("OAuth subject is required");
        }
    }

    // OAuth 인증 완료 이메일 조회 기능
    private String resolveVerifiedEmail(Claims claims) {
        String email = claims.get("email", String.class);

        if (email == null || email.isBlank()) {
            return null;
        }

        Object emailVerified = claims.get("email_verified");
        if (!Boolean.parseBoolean(String.valueOf(emailVerified))) {
            throw new IllegalArgumentException("OAuth email is not verified");
        }

        return email;
    }
}
