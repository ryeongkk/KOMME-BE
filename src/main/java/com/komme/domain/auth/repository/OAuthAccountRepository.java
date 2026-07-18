package com.komme.domain.auth.repository;

import com.komme.domain.auth.entity.OAuthAccount;
import com.komme.domain.user.enums.Provider;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OAuthAccountRepository extends JpaRepository<OAuthAccount, Long> {

    // provider와 provider 식별자 기반 OAuth 계정 조회 기능
    Optional<OAuthAccount> findByProviderAndProviderId(
            Provider provider,
            String providerId
    );
}
