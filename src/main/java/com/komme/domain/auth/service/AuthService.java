package com.komme.domain.auth.service;

import com.komme.common.exception.GeneralException;
import com.komme.domain.auth.dto.request.LoginRequest;
import com.komme.domain.auth.dto.request.LogoutRequest;
import com.komme.domain.auth.dto.request.PasswordChangeRequest;
import com.komme.domain.auth.dto.request.PasswordResetRequest;
import com.komme.domain.auth.dto.request.SignUpRequest;
import com.komme.domain.auth.dto.request.TokenReissueRequest;
import com.komme.domain.auth.dto.response.LoginResponse;
import com.komme.domain.auth.dto.response.TokenReissueResponse;
import com.komme.domain.auth.exception.AuthErrorStatus;
import com.komme.domain.auth.jwt.JwtProvider;
import com.komme.domain.auth.jwt.JwtProvider.TokenClaims;
import com.komme.domain.auth.repository.OAuthAccountRepository;
import com.komme.domain.auth.util.EmailNormalizer;
import com.komme.domain.i18n.enums.Language;
import com.komme.domain.user.entity.User;
import com.komme.domain.user.enums.Gender;
import com.komme.domain.user.enums.ServiceInterest;
import com.komme.domain.user.repository.TermsAgreementRepository;
import com.komme.domain.user.repository.UserRepository;
import com.komme.domain.user.service.UserReader;

import java.util.Locale;
import java.util.Set;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final OAuthAccountRepository oAuthAccountRepository;
    private final TermsAgreementRepository termsAgreementRepository;
    private final UserReader userReader;
    private final AuthUserReader authUserReader;
    private final EmailVerificationService emailVerificationService;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final AuthTokenService authTokenService;
    private final RefreshTokenStore refreshTokenStore;
    private final AccessTokenBlacklistStore accessTokenBlacklistStore;
    private final WithdrawalStore withdrawalStore;
    private final AuthConstraintExceptionMapper authConstraintExceptionMapper;

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
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        String email = EmailNormalizer.normalize(request.email());
        User user = authUserReader.findLocalByEmailOrThrow(email);
        validatePassword(request.password(), user.getPassword());

        return authTokenService.issueLoginResponse(user);
    }

    // Refresh Token 기반 토큰 재발급 기능
    @Transactional(readOnly = true)
    public TokenReissueResponse reissueToken(TokenReissueRequest request) {
        TokenClaims tokenClaims = jwtProvider.parseRefreshToken(request.refreshToken());
        refreshTokenStore.validateAndConsume(tokenClaims);

        LoginResponse loginResponse = authTokenService.issueLoginTokens(tokenClaims.userId());
        return TokenReissueResponse.of(
                loginResponse.accessToken(),
                loginResponse.refreshToken()
        );
    }

    // 로그인 사용자 비밀번호 변경 기능
    @Transactional
    public void changePassword(Long userId, PasswordChangeRequest request) {
        User user = authUserReader.findLocalByIdOrThrow(userId);
        validateCurrentPassword(request.currentPassword(), user.getPassword());
        user.changePassword(passwordEncoder.encode(request.newPassword()));
        refreshTokenStore.invalidateAll(userId);
    }

    // 비밀번호 재설정 토큰 기반 비밀번호 변경 기능
    @Transactional
    public void resetPassword(PasswordResetRequest request) {
        String email = emailVerificationService.consumePasswordResetToken(request.resetToken());
        User user = authUserReader.findLocalByEmailForPasswordResetOrThrow(email);

        user.changePassword(passwordEncoder.encode(request.newPassword()));
        refreshTokenStore.invalidateAll(user.getId());
    }

    // 현재 기기 토큰 로그아웃 기능
    @Transactional(readOnly = true)
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

        refreshTokenStore.validateAndConsume(refreshTokenClaims);
        accessTokenBlacklistStore.blacklist(accessTokenClaims);
    }

    // 로그인 사용자 계정 탈퇴 기능
    @Transactional
    public void withdraw(Long userId, TokenClaims accessTokenClaims) {
        User user = userReader.findByIdOrThrow(userId);
        String email = user.getEmail();

        oAuthAccountRepository.deleteAllByUserId(userId);
        termsAgreementRepository.deleteAllByUserId(userId);
        userRepository.delete(user);
        userRepository.flush();
        refreshTokenStore.invalidateAll(userId);
        accessTokenBlacklistStore.blacklist(accessTokenClaims);
        withdrawalStore.markWithdrawn(email);
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
        withdrawalStore.validateNotWithdrawn(data.email());
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
            throw authConstraintExceptionMapper.map(exception, null);
        }
    }

    // 가입된 이메일 여부 확인 기능
    private void validateEmailNotRegistered(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new GeneralException(AuthErrorStatus.EMAIL_ALREADY_EXISTS);
        }
    }

    // 가입된 닉네임 여부 확인 기능
    private void validateNicknameNotRegistered(String nickname) {
        if (userReader.existsByNickname(nickname)) {
            throw new GeneralException(AuthErrorStatus.NICKNAME_ALREADY_EXISTS);
        }
    }

    // 로그인 비밀번호 검증 기능
    private void validatePassword(String rawPassword, String encodedPassword) {
        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new GeneralException(AuthErrorStatus.INVALID_CREDENTIALS);
        }
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
