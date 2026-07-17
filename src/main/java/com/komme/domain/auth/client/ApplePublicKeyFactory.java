package com.komme.domain.auth.client;

import com.komme.common.exception.GeneralException;
import com.komme.domain.auth.exception.AuthErrorStatus;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

import org.springframework.stereotype.Component;

@Component
public class ApplePublicKeyFactory {

    private static final String RSA_ALGORITHM = "RSA";

    // Apple JWK 기반 RSA 공개키 생성 기능
    public PublicKey create(AppleJwk appleJwk) {
        try {
            Base64.Decoder decoder = Base64.getUrlDecoder();
            BigInteger modulus = new BigInteger(1, decoder.decode(appleJwk.n()));
            BigInteger exponent = new BigInteger(1, decoder.decode(appleJwk.e()));
            RSAPublicKeySpec keySpec = new RSAPublicKeySpec(modulus, exponent);
            return KeyFactory.getInstance(RSA_ALGORITHM).generatePublic(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | IllegalArgumentException exception) {
            throw new GeneralException(
                    AuthErrorStatus.INVALID_APPLE_IDENTITY_TOKEN,
                    exception
            );
        }
    }
}
