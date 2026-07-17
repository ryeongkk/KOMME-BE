package com.komme.domain.auth.client;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.Base64;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApplePublicKeyFactoryTests {

    // Apple JWK RSA 공개키 변환 검증
    @Test
    void createConvertsJwkToRsaPublicKey() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair keyPair = generator.generateKeyPair();
        RSAPublicKey expectedKey = (RSAPublicKey) keyPair.getPublic();
        AppleJwk appleJwk = new AppleJwk(
                "RSA",
                "key-id",
                "sig",
                "RS256",
                encodeUnsigned(expectedKey.getModulus()),
                encodeUnsigned(expectedKey.getPublicExponent())
        );

        PublicKey publicKey = new ApplePublicKeyFactory().create(appleJwk);

        assertThat(publicKey).isEqualTo(expectedKey);
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
