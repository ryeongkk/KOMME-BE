package com.komme.domain.auth.service;

import com.komme.common.exception.GeneralException;
import com.komme.domain.auth.entity.OAuthAccount;
import com.komme.domain.user.entity.User;
import com.komme.domain.auth.enums.Provider;
import com.komme.domain.auth.exception.AuthErrorStatus;
import com.komme.domain.auth.repository.OAuthAccountRepository;
import com.komme.domain.user.repository.UserRepository;
import com.komme.domain.user.service.UserReader;
import com.komme.domain.auth.util.EmailNormalizer;

import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OAuthAccountService {

    private final OAuthAccountRepository oAuthAccountRepository;
    private final UserRepository userRepository;
    private final UserReader userReader;
    private final AuthConstraintExceptionMapper authConstraintExceptionMapper;

    // OAuth identity 기반 사용자 조회 및 연결 기능
    @Transactional
    public User resolveUser(Provider provider, String providerId, String email) {
        return findLinkedUser(provider, providerId)
                .orElseGet(() -> linkOrCreateUser(provider, providerId, email));
    }

    // OAuth 계정 연결 사용자 조회 기능
    private Optional<User> findLinkedUser(Provider provider, String providerId) {
        return oAuthAccountRepository.findByProviderAndProviderId(
                        provider,
                        providerId
                )
                .map(OAuthAccount::getUser);
    }

    // 이메일 기반 기존 사용자 연결 또는 OAuth 사용자 생성 기능
    private User linkOrCreateUser(Provider provider, String providerId, String email) {
        if (email == null) {
            throw new GeneralException(resolveEmailRequiredStatus(provider));
        }

        String normalizedEmail = EmailNormalizer.normalize(email);
        User user = findOrCreateUser(provider, normalizedEmail);
        saveOAuthAccount(user, provider, providerId);
        return user;
    }

    // OAuth 이메일 기반 사용자 조회 또는 생성 기능
    private User findOrCreateUser(Provider provider, String email) {
        return userReader.findByEmail(email)
                .orElseGet(() -> createOAuthUser(provider, email));
    }

    // OAuth 최소 프로필 사용자 저장 기능
    private User createOAuthUser(Provider provider, String email) {
        try {
            return userRepository.saveAndFlush(User.createOAuth(email, provider));
        } catch (DataIntegrityViolationException exception) {
            throw authConstraintExceptionMapper.map(
                    exception,
                    AuthErrorStatus.EMAIL_ALREADY_EXISTS
            );
        }
    }

    // OAuth 계정 연결 저장 기능
    private void saveOAuthAccount(User user, Provider provider, String providerId) {
        try {
            oAuthAccountRepository.saveAndFlush(
                    OAuthAccount.create(user, provider, providerId)
            );
        } catch (DataIntegrityViolationException exception) {
            throw authConstraintExceptionMapper.map(
                    exception,
                    AuthErrorStatus.OAUTH_ACCOUNT_ALREADY_LINKED
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
