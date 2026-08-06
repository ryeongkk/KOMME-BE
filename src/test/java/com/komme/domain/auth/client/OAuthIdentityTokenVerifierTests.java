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
import static org.mockito.Mockito.lenient;

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

    // OAuth identity token subject 클레임이 없으면(null) 거부되는지 검증
    @Test
    void verifyRejectsMissingSubject() {
        String token = createToken(null, ISSUER, CLIENT_ID, null, true);

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

    // 참고: validateSubject()의 "subject가 null은 아니지만 공백뿐인" 분기는 테스트로 만들 수 없다.
    // JJWT 빌더는 sub 클레임이 공백뿐이면 클레임 자체를 생략하고, 빌더를 완전히 우회해서
    // JWT를 바이트 단위로 직접 조립해(payload JSON에 "sub":"   "를 그대로 넣어) 시도해봐도 결과는 같다 —
    // Jwts.claims().add(map).build() 처럼 Claims 객체에 값을 넣는 것만으로도 get("sub")이 이미 null을 반환하는 것을
    // 별도로 확인했다(JJWT 0.13.0 Claims 구현체가 공백 문자열 값을 읽는 시점에 null로 정규화함).
    // 즉 파싱된 토큰이 claims.getSubject()로 노출하는 한 이 분기는 구조적으로 도달 불가능하다.

    // OAuth identity token issuer 클레임이 아예 없으면(null) 거부되는지 검증
    @Test
    void verifyRejectsMissingIssuer() {
        String token = createToken(SUBJECT, null, CLIENT_ID, EMAIL, true);

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

    // 이메일 클레임 자체가 없으면(애플의 "최초 로그인 이후 이메일 미제공" 케이스) null 이메일로 성공 처리되는지 검증
    @Test
    void verifyReturnsIdentityWithNullEmailWhenEmailClaimMissing() {
        String token = createToken(SUBJECT, ISSUER, CLIENT_ID, null, true);

        OAuthIdentity identity = verifier.verify(
                token,
                jwksProvider,
                CLIENT_ID,
                Set.of(ISSUER),
                AuthErrorStatus.INVALID_APPLE_IDENTITY_TOKEN
        );

        assertThat(identity.subject()).isEqualTo(SUBJECT);
        assertThat(identity.email()).isNull();
    }

    // 이메일 클레임이 빈 문자열이어도 null 이메일로 성공 처리되는지 검증
    @Test
    void verifyReturnsIdentityWithNullEmailWhenEmailClaimBlank() {
        String token = createToken(SUBJECT, ISSUER, CLIENT_ID, "", true);

        OAuthIdentity identity = verifier.verify(
                token,
                jwksProvider,
                CLIENT_ID,
                Set.of(ISSUER),
                AuthErrorStatus.INVALID_APPLE_IDENTITY_TOKEN
        );

        assertThat(identity.email()).isNull();
    }

    // RS256이 아닌 알고리즘으로 서명된 토큰은 거부되는지 검증
    @Test
    void verifyRejectsTokenSignedWithUnsupportedAlgorithm() {
        Instant now = Instant.now();
        String token = Jwts.builder()
                .header().keyId(KEY_ID).and()
                .subject(SUBJECT)
                .issuer(ISSUER)
                .audience().add(CLIENT_ID).and()
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(300)))
                .signWith(Jwts.SIG.HS256.key().build(), Jwts.SIG.HS256)
                .compact();

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
        lenient().when(jwksProvider.getSigningJwk(KEY_ID)).thenReturn(new OAuthJwk(
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
