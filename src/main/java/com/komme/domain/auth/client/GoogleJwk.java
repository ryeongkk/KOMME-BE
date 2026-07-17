package com.komme.domain.auth.client;

public record GoogleJwk(
        String kty,
        String kid,
        String use,
        String alg,
        String n,
        String e
) {
}
