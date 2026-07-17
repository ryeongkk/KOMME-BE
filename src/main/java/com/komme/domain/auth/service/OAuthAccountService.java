package com.komme.domain.auth.service;

import com.komme.common.exception.GeneralException;
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

    // OAuth identity 기반 사용자 조회 및 연결 기능
    @Transactional
    public User resolveUser(Provider provider, String providerId, String email) {
        User linkedUser = findLinkedUser(provider, providerId);

        if (linkedUser != null) {
            return linkedUser;
        }

        return linkOrCreateUser(provider, providerId, email);
    }

    // OAuth 계정 연결 사용자 조회 기능
    private User findLinkedUser(Provider provider, String providerId) {
        return oAuthAccountRepository.findByProviderAndProviderId(
                        provider,
                        providerId
                )
                .map(OAuthAccount::getUser)
                .orElse(null);
    }

    // 이메일 기반 기존 사용자 연결 또는 OAuth 사용자 생성 기능
    private User linkOrCreateUser(Provider provider, String providerId, String email) {
        if (email == null) {
            throw new GeneralException(resolveEmailRequiredStatus(provider));
        }

        String normalizedEmail = EmailNormalizer.normalize(email);
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseGet(() -> userRepository.saveAndFlush(
                        User.createOAuth(normalizedEmail, provider)
                ));
        saveOAuthAccount(user, provider, providerId);
        return user;
    }

    // OAuth 계정 연결 저장 기능
    private void saveOAuthAccount(User user, Provider provider, String providerId) {
        try {
            oAuthAccountRepository.saveAndFlush(
                    OAuthAccount.create(user, provider, providerId)
            );
        } catch (DataIntegrityViolationException exception) {
            throw new GeneralException(
                    AuthErrorStatus.OAUTH_ACCOUNT_ALREADY_LINKED,
                    exception
            );
        }
    }

    // Provider별 이메일 필수 오류 조회 기능
    private AuthErrorStatus resolveEmailRequiredStatus(Provider provider) {
        return provider == Provider.GOOGLE
                ? AuthErrorStatus.GOOGLE_EMAIL_REQUIRED
                : AuthErrorStatus.APPLE_EMAIL_REQUIRED;
    }
}
