package com.komme.domain.auth.client;

public record OAuthJwk(
        String kty,
        String kid,
        String use,
        String alg,
        String n,
        String e
) {
}
