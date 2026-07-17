package com.komme.domain.auth.service;

import com.komme.domain.auth.dto.response.LoginResponse;
import com.komme.domain.auth.jwt.JwtProvider;
import com.komme.domain.auth.jwt.JwtProvider.IssuedToken;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthTokenService {

    private final JwtProvider jwtProvider;
    private final RefreshTokenStore refreshTokenStore;

    // 사용자 로그인 토큰 발급 기능
    public LoginResponse issueLoginTokens(Long userId) {
        IssuedToken accessToken = jwtProvider.issueAccessToken(userId);
        IssuedToken refreshToken = jwtProvider.issueRefreshToken(userId);
        refreshTokenStore.save(userId, refreshToken);

        return LoginResponse.of(accessToken.value(), refreshToken.value());
    }

}
