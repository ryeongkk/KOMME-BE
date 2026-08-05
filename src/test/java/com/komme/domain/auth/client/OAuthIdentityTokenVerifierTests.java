package com.komme.domain.auth.client;

import com.komme.common.exception.GeneralException;
import com.komme.domain.auth.exception.AuthErrorStatus;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.Set;

import io.jsonwebtoken.Jwts;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OAuthIdentityTokenVerifierTests {

    private static final String KEY_ID = "key-id";
    private static final String CLIENT_ID = "client-id";
    private static final String ISSUER = "https://accounts.example.com";
    private static final String SUBJECT = "provider-subject";
    private static final String EMAIL = "user@example.com";

    @Mock
    private AbstractOAuthJwksProvider jwksProvider;

    private KeyPair keyPair;
    private OAuthIdentityTokenVerifier verifier;

    // OAuth identity token 검증 테스트 환경 구성
    @BeforeEach
    void setUp() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        keyPair = generator.generateKeyPair();
        verifier = new OAuthIdentityTokenVerifier(new OAuthPublicKeyFactory());
        prepareSigningJwk();
    }

    // OAuth identity token 검증 성공 검증
    @Test
    void verifyReturnsOAuthIdentity() {
        String token = createToken(SUBJECT, ISSUER, CLIENT_ID, EMAIL, true);

        OAuthIdentity identity = verifier.verify(
                token,
                jwksProvider,
                CLIENT_ID,
                Set.of(ISSUER),
                AuthErrorStatus.INVALID_GOOGLE_IDENTITY_TOKEN
        );

        assertThat(identity.subject()).isEqualTo(SUBJECT);
        assertThat(identity.email()).isEqualTo(EMAIL);
    }

    // OAuth identity token 미인증 이메일 거부 검증
    @Test
    void verifyRejectsUnverifiedEmail() {
        String token = createToken(SUBJECT, ISSUER, CLIENT_ID, EMAIL, false);

        assertThatThrownBy(() -> verifier.verify(
                token,
                jwksProvider,
                CLIENT_ID,
                Set.of(ISSUER),
                AuthErrorStatus.INVALID_GOOGLE_IDENTITY_TOKEN
        ))
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getErrorStatus())
                .isEqualTo(AuthErrorStatus.INVALID_GOOGLE_IDENTITY_TOKEN);
    }

    // OAuth identity token 발급자 불일치 거부 검증
    @Test
    void verifyRejectsInvalidIssuer() {
        String token = createToken(SUBJECT, "https://invalid.example.com", CLIENT_ID, EMAIL, true);

        assertThatThrownBy(() -> verifier.verify(
                token,
                jwksProvider,
                CLIENT_ID,
                Set.of(ISSUER),
                AuthErrorStatus.INVALID_APPLE_IDENTITY_TOKEN
        ))
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getErrorStatus())
                .isEqualTo(AuthErrorStatus.INVALID_APPLE_IDENTITY_TOKEN);
    }

    // OAuth identity token subject 누락 거부 검증
    @Test
    void verifyRejectsBlankSubject() {
        String token = createToken("", ISSUER, CLIENT_ID, null, true);

        assertThatThrownBy(() -> verifier.verify(
                token,
                jwksProvider,
                CLIENT_ID,
                Set.of(ISSUER),
                AuthErrorStatus.INVALID_GOOGLE_IDENTITY_TOKEN
        ))
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getErrorStatus())
                .isEqualTo(AuthErrorStatus.INVALID_GOOGLE_IDENTITY_TOKEN);
    }

    // 테스트 identity token 생성
    private String createToken(
            String subject,
            String issuer,
            String audience,
            String email,
            boolean emailVerified
    ) {
        Instant now = Instant.now();
        var builder = Jwts.builder()
                .header()
                .keyId(KEY_ID)
                .and()
                .subject(subject)
                .issuer(issuer)
                .audience()
                .add(audience)
                .and()
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(300)));

        if (email != null) {
            builder.claim("email", email)
                    .claim("email_verified", emailVerified);
        }

        return builder.signWith(keyPair.getPrivate(), Jwts.SIG.RS256)
                .compact();
    }

    // 서명 JWK Mock 구성
    private void prepareSigningJwk() {
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        when(jwksProvider.getSigningJwk(KEY_ID)).thenReturn(new OAuthJwk(
                "RSA",
                KEY_ID,
                "sig",
                "RS256",
                encodeUnsigned(publicKey.getModulus()),
                encodeUnsigned(publicKey.getPublicExponent())
        ));
    }

    // 양의 정수 Base64 URL 인코딩 생성
    private String encodeUnsigned(BigInteger value) {
        byte[] bytes = value.toByteArray();
        int offset = bytes.length > 1 && bytes[0] == 0 ? 1 : 0;
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(Arrays.copyOfRange(bytes, offset, bytes.length));
    }
}
