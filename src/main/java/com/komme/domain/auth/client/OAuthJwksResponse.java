package com.komme.domain.auth.client;

import java.util.List;

public record OAuthJwksResponse(List<OAuthJwk> keys) {
}
