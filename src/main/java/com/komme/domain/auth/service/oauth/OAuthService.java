package com.komme.domain.auth.service.oauth;

import com.komme.common.exception.GeneralException;
import com.komme.domain.auth.client.OAuthAppleClient;
import com.komme.domain.auth.client.OAuthIdentity;
import com.komme.domain.auth.client.OAuthGoogleClient;
import com.komme.domain.auth.dto.request.OAuthAppleLoginRequest;
import com.komme.domain.auth.dto.request.OAuthGoogleLoginRequest;
import com.komme.domain.auth.dto.request.OAuthProfileCompleteRequest;
import com.komme.domain.auth.dto.response.LoginResponse;
import com.komme.domain.i18n.enums.Language;
import com.komme.domain.user.entity.User;
import com.komme.domain.user.enums.Provider;
import com.komme.domain.auth.exception.AuthErrorStatus;
import com.komme.domain.auth.service.token.AuthTokenService;
import com.komme.domain.user.repository.UserRepository;
import com.komme.domain.user.service.UserReader;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OAuthService {

    private final OAuthAppleClient oAuthAppleClient;
    private final OAuthGoogleClient oAuthGoogleClient;
    private final OAuthAccountService oAuthAccountService;
    private final AuthTokenService authTokenService;
    private final UserRepository userRepository;
    private final UserReader userReader;

    // Apple identity token 로그인 흐름 조율 기능
    @Transactional
    public LoginResponse loginWithApple(OAuthAppleLoginRequest request) {
        OAuthIdentity identity = oAuthAppleClient.verifyIdentityToken(request.identityToken());
        return finishOAuthLogin(Provider.APPLE, identity, request.preferredLanguage());
    }

    // Google authorization code 로그인 흐름 조율 기능
    @Transactional
    public LoginResponse loginWithGoogle(OAuthGoogleLoginRequest request) {
        OAuthIdentity identity = oAuthGoogleClient.verifyAuthorizationCode(request.code());
        return finishOAuthLogin(Provider.GOOGLE, identity, request.preferredLanguage());
    }

    // OAuth 로그인 사용자 확정 및 토큰 발급 공통 기능
    private LoginResponse finishOAuthLogin(Provider provider, OAuthIdentity identity, Language preferredLanguage) {
        User user = oAuthAccountService.resolveUser(provider, identity.subject(), identity.email());
        user.changePreferredLanguage(preferredLanguage);
        return authTokenService.issueLoginResponse(user);
    }

    // 로그인 사용자 OAuth 프로필 완성 기능
    @Transactional
    public void completeProfile(Long userId, OAuthProfileCompleteRequest request) {
        User user = userReader.findByIdOrThrow(userId);

        try {
            user.completeProfile(request.nickname().trim());
            userRepository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new GeneralException(
                    AuthErrorStatus.NICKNAME_ALREADY_EXISTS,
                    exception
            );
        }
    }
}
