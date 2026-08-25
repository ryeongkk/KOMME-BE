package com.komme.domain.auth.client;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GoogleTokenResponse(
        @JsonProperty("id_token")
        String idToken
) {
}
