package com.komme.domain.auth.client;

import com.komme.common.exception.GeneralException;
import com.komme.domain.auth.exception.AuthErrorStatus;
import com.komme.domain.auth.properties.GoogleProperties;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;

import io.jsonwebtoken.Jwts;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OAuthGoogleClientTests {

    private static final String CLIENT_ID = "com.komme.google.app";
    private static final String KEY_ID = "google-key-id";
    private static final String SUBJECT = "google-sub";
    private static final String EMAIL = "user@example.com";

    private KeyPair keyPair;
    private OAuthGoogleClient oAuthGoogleClient;

    // Google OAuth 클라이언트 테스트 환경 구성
    @BeforeEach
    void setUp() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        keyPair = keyPairGenerator.generateKeyPair();
        GoogleProperties properties = new GoogleProperties(CLIENT_ID, Duration.ofHours(1));
        oAuthGoogleClient = new OAuthGoogleClient(
                new GoogleJwksProvider(createGoogleWebClient(), properties),
                properties,
                new OAuthIdentityTokenVerifier(new OAuthPublicKeyFactory())
        );
    }

    // 유효한 Google identity token 사용자 정보 추출 검증
    @Test
    void verifyIdentityTokenReturnsVerifiedIdentity() {
        OAuthIdentity identity = oAuthGoogleClient.verifyIdentityToken(
                createIdentityToken(CLIENT_ID, "https://accounts.google.com", true)
        );

        assertThat(identity.subject()).isEqualTo(SUBJECT);
        assertThat(identity.email()).isEqualTo(EMAIL);
    }

    // 잘못된 Google identity token 발급자 거부 검증
    @Test
    void verifyIdentityTokenRejectsInvalidIssuer() {
        assertThatThrownBy(() -> oAuthGoogleClient.verifyIdentityToken(
                createIdentityToken(CLIENT_ID, "https://invalid.google.com", true)
        ))
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getErrorStatus())
                .isEqualTo(AuthErrorStatus.INVALID_GOOGLE_IDENTITY_TOKEN);
    }

    // 인증되지 않은 Google 이메일 거부 검증
    @Test
    void verifyIdentityTokenRejectsUnverifiedEmail() {
        assertThatThrownBy(() -> oAuthGoogleClient.verifyIdentityToken(
                createIdentityToken(CLIENT_ID, "https://accounts.google.com", false)
        ))
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getErrorStatus())
                .isEqualTo(AuthErrorStatus.INVALID_GOOGLE_IDENTITY_TOKEN);
    }

    // 테스트 Google JWKS WebClient 생성
    private WebClient createGoogleWebClient() {
        return WebClient.builder()
                .exchangeFunction(request -> Mono.just(
                        ClientResponse.create(HttpStatus.OK)
                                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                .body(createJwksJson())
                                .build()
                ))
                .build();
    }

    // 테스트 Google identity token 생성
    private String createIdentityToken(
            String audience,
            String issuer,
            boolean emailVerified
    ) {
        Instant issuedAt = Instant.now();
        return Jwts.builder()
                .header()
                .keyId(KEY_ID)
                .and()
                .issuer(issuer)
                .subject(SUBJECT)
                .claim("aud", audience)
                .claim("email", EMAIL)
                .claim("email_verified", String.valueOf(emailVerified))
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(issuedAt.plus(Duration.ofMinutes(5))))
                .signWith(keyPair.getPrivate(), Jwts.SIG.RS256)
                .compact();
    }

    // 테스트 Google JWKS JSON 생성
    private String createJwksJson() {
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        return """
                {
                  "keys": [
                    {
                      "kty": "RSA",
                      "kid": "%s",
                      "use": "sig",
                      "alg": "RS256",
                      "n": "%s",
                      "e": "%s"
                    }
                  ]
                }
                """.formatted(
                KEY_ID,
                encodeUnsigned(publicKey.getModulus()),
                encodeUnsigned(publicKey.getPublicExponent())
        );
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
