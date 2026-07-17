package com.komme.domain.auth.client;

public record OAuthIdentity(
        String subject,
        String email
) {
}
