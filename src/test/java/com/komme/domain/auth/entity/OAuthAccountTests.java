package com.komme.domain.auth.entity;

import com.komme.domain.user.enums.Provider;
import com.komme.domain.user.entity.User;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OAuthAccountTests {

    // Apple OAuth 계정 연결 생성값 검증
    @Test
    void createConnectsAppleAccountToUser() {
        User user = User.createOAuth("user@example.com", Provider.APPLE);

        OAuthAccount account = OAuthAccount.create(user, Provider.APPLE, "apple-sub");

        assertThat(account.getUser()).isSameAs(user);
        assertThat(account.getProvider()).isEqualTo(Provider.APPLE);
        assertThat(account.getProviderId()).isEqualTo("apple-sub");
    }
}
