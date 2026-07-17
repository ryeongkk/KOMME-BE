package com.komme.domain.auth.service;

import com.komme.common.exception.GeneralException;
import com.komme.domain.auth.dto.request.LoginRequest;
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
import com.komme.domain.auth.jwt.JwtProvider.IssuedToken;
import com.komme.domain.auth.jwt.JwtProvider.TokenClaims;
import com.komme.domain.auth.repository.UserRepository;
import com.komme.domain.auth.util.EmailNormalizer;
import com.komme.i18n.enums.Language;

import java.util.Locale;
import java.util.Set;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String REFRESH_TOKEN_KEY_PREFIX = "auth:refresh-token:";
    private static final String USER_REFRESH_TOKENS_KEY_PREFIX = "auth:user-refresh-tokens:";

    private final UserRepository userRepository;
    private final EmailVerificationService emailVerificationService;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final StringRedisTemplate redisTemplate;

    // 이메일 인증 기반 LOCAL 사용자 가입 기능
    @Transactional
    public void signUp(SignUpRequest request) {
        NormalizedSignUpData data = normalizeSignUpData(request);
        validateSignUp(data);
        User user = createLocalUser(request.password(), data);

        userRepository.saveAndFlush(user);
        emailVerificationService.deleteVerifiedEmail(data.email());
    }

    // 이메일 기반 LOCAL 사용자 로그인 기능
    public LoginResponse login(LoginRequest request) {
        String email = EmailNormalizer.normalize(request.email());
        User user = findLocalUser(email);
        validatePassword(request.password(), user.getPassword());

        IssuedToken accessToken = jwtProvider.issueAccessToken(user.getId());
        IssuedToken refreshToken = jwtProvider.issueRefreshToken(user.getId());
        saveRefreshToken(user.getId(), refreshToken);

        return LoginResponse.of(
                accessToken.value(),
                refreshToken.value()
        );
    }

    // Refresh Token 기반 토큰 재발급 기능
    public TokenReissueResponse reissueToken(TokenReissueRequest request) {
        TokenClaims tokenClaims = jwtProvider.parseRefreshToken(request.refreshToken());
        validateAndConsumeRefreshToken(tokenClaims);

        IssuedToken accessToken = jwtProvider.issueAccessToken(tokenClaims.userId());
        IssuedToken refreshToken = jwtProvider.issueRefreshToken(tokenClaims.userId());
        saveRefreshToken(tokenClaims.userId(), refreshToken);

        return TokenReissueResponse.of(accessToken.value(), refreshToken.value());
    }

    // 로그인 사용자 비밀번호 변경 기능
    @Transactional
    public void changePassword(Long userId, PasswordChangeRequest request) {
        User user = findLocalUser(userId);
        validateCurrentPassword(request.currentPassword(), user.getPassword());
        user.changePassword(passwordEncoder.encode(request.newPassword()));
        invalidateAllRefreshTokens(userId);
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

    // Refresh Token 식별자 Redis 저장 기능
    private void saveRefreshToken(Long userId, IssuedToken refreshToken) {
        redisTemplate.opsForValue().set(
                createRefreshTokenKey(refreshToken.id()),
                userId.toString(),
                refreshToken.expiration()
        );
        redisTemplate.opsForSet().add(
                createUserRefreshTokensKey(userId),
                refreshToken.id()
        );
        redisTemplate.expire(
                createUserRefreshTokensKey(userId),
                refreshToken.expiration()
        );
    }

    // Refresh Token Redis 저장값 검증 및 소비 기능
    private void validateAndConsumeRefreshToken(TokenClaims tokenClaims) {
        String savedUserId = redisTemplate.opsForValue().getAndDelete(
                createRefreshTokenKey(tokenClaims.tokenId())
        );
        redisTemplate.opsForSet().remove(
                createUserRefreshTokensKey(tokenClaims.userId()),
                tokenClaims.tokenId()
        );

        if (!tokenClaims.userId().toString().equals(savedUserId)
                || !userRepository.existsById(tokenClaims.userId())) {
            throw new GeneralException(AuthErrorStatus.INVALID_TOKEN);
        }
    }

    // Refresh Token Redis 키 생성
    private String createRefreshTokenKey(String tokenId) {
        return REFRESH_TOKEN_KEY_PREFIX + tokenId;
    }

    // 사용자별 Refresh Token 목록 Redis 키 생성
    private String createUserRefreshTokensKey(Long userId) {
        return USER_REFRESH_TOKENS_KEY_PREFIX + userId;
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

    // 사용자 전체 Refresh Token 폐기 기능
    private void invalidateAllRefreshTokens(Long userId) {
        String userRefreshTokensKey = createUserRefreshTokensKey(userId);
        Set<String> tokenIds = redisTemplate.opsForSet().members(userRefreshTokensKey);

        if (tokenIds != null && !tokenIds.isEmpty()) {
            redisTemplate.delete(
                    tokenIds.stream()
                            .map(this::createRefreshTokenKey)
                            .toList()
            );
        }

        redisTemplate.delete(userRefreshTokensKey);
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
