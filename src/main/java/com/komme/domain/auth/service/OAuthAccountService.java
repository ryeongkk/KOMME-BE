package com.komme.domain.auth.service;

import com.komme.common.exception.GeneralException;
import com.komme.domain.auth.client.OAuthAppleClient.AppleIdentity;
import com.komme.domain.auth.entity.OAuthAccount;
import com.komme.domain.auth.entity.User;
import com.komme.domain.auth.enums.Provider;
import com.komme.domain.auth.exception.AuthErrorStatus;
import com.komme.domain.auth.repository.OAuthAccountRepository;
import com.komme.domain.auth.repository.UserRepository;
import com.komme.domain.auth.util.EmailNormalizer;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OAuthAccountService {

    private final OAuthAccountRepository oAuthAccountRepository;
    private final UserRepository userRepository;

    // Apple identity 기반 사용자 조회 및 연결 기능
    @Transactional
    public User resolveAppleUser(AppleIdentity identity) {
        User linkedUser = findLinkedUser(identity.subject());

        if (linkedUser != null) {
            return linkedUser;
        }

        return linkOrCreateAppleUser(identity);
    }

    // Apple 계정 연결 사용자 조회 기능
    private User findLinkedUser(String providerId) {
        return oAuthAccountRepository.findByProviderAndProviderId(
                        Provider.APPLE,
                        providerId
                )
                .map(OAuthAccount::getUser)
                .orElse(null);
    }

    // 이메일 기반 기존 사용자 연결 또는 Apple 사용자 생성 기능
    private User linkOrCreateAppleUser(AppleIdentity identity) {
        if (identity.email() == null) {
            throw new GeneralException(AuthErrorStatus.APPLE_EMAIL_REQUIRED);
        }

        String email = EmailNormalizer.normalize(identity.email());
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> userRepository.saveAndFlush(
                        User.createOAuth(email, Provider.APPLE)
                ));
        saveOAuthAccount(user, identity.subject());
        return user;
    }

    // Apple OAuth 계정 연결 저장 기능
    private void saveOAuthAccount(User user, String providerId) {
        try {
            oAuthAccountRepository.saveAndFlush(
                    OAuthAccount.create(user, Provider.APPLE, providerId)
            );
        } catch (DataIntegrityViolationException exception) {
            throw new GeneralException(
                    AuthErrorStatus.OAUTH_ACCOUNT_ALREADY_LINKED,
                    exception
            );
        }
    }
}
