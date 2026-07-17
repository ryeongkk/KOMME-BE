package com.komme.domain.auth.service;

import com.komme.common.exception.GeneralException;
import com.komme.domain.auth.dto.request.LoginRequest;
import com.komme.domain.auth.dto.request.LogoutRequest;
import com.komme.domain.auth.dto.request.PasswordChangeRequest;
import com.komme.domain.auth.dto.request.SignUpRequest;
import com.komme.domain.auth.dto.request.TokenReissueRequest;
import com.komme.domain.auth.dto.response.LoginResponse;
import com.komme.domain.auth.dto.response.TokenReissueResponse;
import com.komme.domain.auth.entity.User;
import com.komme.domain.auth.enums.Gender;
import com.komme.domain.auth.enums.Provider;
import com.komme.domain.auth.enums.ServiceInterest;
import com.komme.domain.auth.exception.AuthErrorStatus;
import com.komme.domain.auth.jwt.JwtProvider;
import com.komme.domain.auth.jwt.JwtProvider.TokenClaims;
import com.komme.domain.auth.repository.UserRepository;
import com.komme.domain.auth.util.EmailNormalizer;
import com.komme.i18n.enums.Language;

import java.util.Locale;
import java.util.Set;

import org.hibernate.exception.ConstraintViolationException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final EmailVerificationService emailVerificationService;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final AuthTokenService authTokenService;
    private final AuthTokenStore authTokenStore;

    // 이메일 인증 기반 LOCAL 사용자 가입 기능
    @Transactional
    public void signUp(SignUpRequest request) {
        NormalizedSignUpData data = normalizeSignUpData(request);
        validateSignUp(data);
        User user = createLocalUser(request.password(), data);

        saveUser(user);
        emailVerificationService.deleteVerifiedEmail(data.email());
    }

    // 이메일 기반 LOCAL 사용자 로그인 기능
    public LoginResponse login(LoginRequest request) {
        String email = EmailNormalizer.normalize(request.email());
        User user = findLocalUser(email);
        validatePassword(request.password(), user.getPassword());

        return authTokenService.issueLoginTokens(user.getId());
    }

    // Refresh Token 기반 토큰 재발급 기능
    public TokenReissueResponse reissueToken(TokenReissueRequest request) {
        TokenClaims tokenClaims = jwtProvider.parseRefreshToken(request.refreshToken());
        authTokenStore.validateAndConsumeRefreshToken(tokenClaims);

        LoginResponse loginResponse = authTokenService.issueLoginTokens(tokenClaims.userId());
        return TokenReissueResponse.of(
                loginResponse.accessToken(),
                loginResponse.refreshToken()
        );
    }

    // 로그인 사용자 비밀번호 변경 기능
    @Transactional
    public void changePassword(Long userId, PasswordChangeRequest request) {
        User user = findLocalUser(userId);
        validateCurrentPassword(request.currentPassword(), user.getPassword());
        user.changePassword(passwordEncoder.encode(request.newPassword()));
        authTokenStore.invalidateAllRefreshTokens(userId);
    }

    // 현재 기기 토큰 로그아웃 기능
    public void logout(
            Long userId,
            TokenClaims accessTokenClaims,
            LogoutRequest request
    ) {
        TokenClaims refreshTokenClaims = jwtProvider.parseRefreshToken(request.refreshToken());

        if (!userId.equals(accessTokenClaims.userId())
                || !userId.equals(refreshTokenClaims.userId())) {
            throw new GeneralException(AuthErrorStatus.INVALID_TOKEN);
        }

        authTokenStore.validateAndConsumeRefreshToken(refreshTokenClaims);
        authTokenStore.blacklistAccessToken(accessTokenClaims);
    }

    // 회원가입 입력값 정규화 기능
    private NormalizedSignUpData normalizeSignUpData(SignUpRequest request) {
        return new NormalizedSignUpData(
                EmailNormalizer.normalize(request.email()),
                request.nickname().trim(),
                request.nationality().toUpperCase(Locale.ROOT),
                request.gender(),
                request.preferredLanguage(),
                request.serviceInterests()
        );
    }

    // 회원가입 가능 여부 검증 기능
    private void validateSignUp(NormalizedSignUpData data) {
        emailVerificationService.validateVerifiedEmail(data.email());
        validateEmailNotRegistered(data.email());
        validateNicknameNotRegistered(data.nickname());
    }

    // LOCAL 사용자 생성 기능
    private User createLocalUser(String password, NormalizedSignUpData data) {
        return User.createLocal(
                data.email(),
                passwordEncoder.encode(password),
                data.nickname(),
                data.nationality(),
                data.gender(),
                data.preferredLanguage(),
                data.serviceInterests()
        );
    }

    // 사용자 저장 및 unique 제약조건 오류 변환 기능
    private void saveUser(User user) {
        try {
            userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException exception) {
            throw convertUniqueConstraintException(exception);
        }
    }

    // unique 제약조건 기반 도메인 예외 변환 기능
    private RuntimeException convertUniqueConstraintException(
            DataIntegrityViolationException exception
    ) {
        String constraintName = findConstraintName(exception);

        if ("uk_user_email".equalsIgnoreCase(constraintName)) {
            return new GeneralException(AuthErrorStatus.EMAIL_ALREADY_EXISTS, exception);
        }

        if ("uk_user_nickname".equalsIgnoreCase(constraintName)) {
            return new GeneralException(AuthErrorStatus.NICKNAME_ALREADY_EXISTS, exception);
        }

        return exception;
    }

    // 예외 원인 체인의 Hibernate 제약조건명 조회 기능
    private String findConstraintName(Throwable throwable) {
        Throwable cause = throwable;

        while (cause != null) {
            if (cause instanceof ConstraintViolationException constraintViolationException) {
                return constraintViolationException.getConstraintName();
            }
            cause = cause.getCause();
        }

        return null;
    }

    // 가입된 이메일 여부 확인 기능
    private void validateEmailNotRegistered(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new GeneralException(AuthErrorStatus.EMAIL_ALREADY_EXISTS);
        }
    }

    // 가입된 닉네임 여부 확인 기능
    private void validateNicknameNotRegistered(String nickname) {
        if (userRepository.existsByNickname(nickname)) {
            throw new GeneralException(AuthErrorStatus.NICKNAME_ALREADY_EXISTS);
        }
    }

    // 이메일 기반 LOCAL 사용자 조회 기능
    private User findLocalUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new GeneralException(AuthErrorStatus.INVALID_CREDENTIALS));

        if (user.getProvider() != Provider.LOCAL) {
            throw new GeneralException(AuthErrorStatus.INVALID_CREDENTIALS);
        }

        return user;
    }

    // 로그인 비밀번호 검증 기능
    private void validatePassword(String rawPassword, String encodedPassword) {
        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new GeneralException(AuthErrorStatus.INVALID_CREDENTIALS);
        }
    }

    // 사용자 ID 기반 LOCAL 사용자 조회 기능
    private User findLocalUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(AuthErrorStatus.INVALID_TOKEN));

        if (user.getProvider() != Provider.LOCAL) {
            throw new GeneralException(AuthErrorStatus.INVALID_TOKEN);
        }

        return user;
    }

    // 현재 비밀번호 검증 기능
    private void validateCurrentPassword(String currentPassword, String encodedPassword) {
        if (!passwordEncoder.matches(currentPassword, encodedPassword)) {
            throw new GeneralException(AuthErrorStatus.INVALID_CURRENT_PASSWORD);
        }
    }

    private record NormalizedSignUpData(
            String email,
            String nickname,
            String nationality,
            Gender gender,
            Language preferredLanguage,
            Set<ServiceInterest> serviceInterests
    ) {
    }
}
