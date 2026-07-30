package com.komme.domain.auth.service;

import com.komme.common.exception.GeneralException;
import com.komme.domain.auth.exception.AuthErrorStatus;
import com.komme.domain.user.entity.User;
import com.komme.domain.user.enums.Provider;
import com.komme.domain.user.service.UserReader;

import java.util.Optional;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthUserReader {

    private final UserReader userReader;

    // LOCAL 이메일 사용자 필수 조회 기능
    public User findLocalByEmailOrThrow(String email) {
        User user = userReader.findByEmail(email)
                .orElseThrow(() -> new GeneralException(AuthErrorStatus.INVALID_CREDENTIALS));
        return validateLocalUser(user, AuthErrorStatus.INVALID_CREDENTIALS);
    }

    // 비밀번호 재설정용 LOCAL 이메일 사용자 필수 조회 기능
    public User findLocalByEmailForPasswordResetOrThrow(String email) {
        return findLocalByEmailForPasswordReset(email)
                .orElseThrow(() -> new GeneralException(AuthErrorStatus.EMAIL_NOT_REGISTERED));
    }

    // 비밀번호 재설정용 LOCAL 이메일 사용자 조회 기능
    public Optional<User> findLocalByEmailForPasswordReset(String email) {
        return userReader.findByEmail(email)
                .filter(user -> user.getProvider() == Provider.LOCAL);
    }

    // LOCAL ID 사용자 필수 조회 기능
    public User findLocalByIdOrThrow(Long userId) {
        User user = userReader.findById(userId)
                .orElseThrow(() -> new GeneralException(AuthErrorStatus.INVALID_TOKEN));
        return validateLocalUser(user, AuthErrorStatus.INVALID_TOKEN);
    }

    // LOCAL 사용자 여부 검증 기능
    private User validateLocalUser(User user, AuthErrorStatus errorStatus) {
        if (user.getProvider() != Provider.LOCAL) {
            throw new GeneralException(errorStatus);
        }
        return user;
    }
}
