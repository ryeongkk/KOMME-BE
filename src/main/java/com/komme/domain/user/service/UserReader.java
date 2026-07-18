package com.komme.domain.user.service;

import com.komme.common.exception.GeneralException;
import com.komme.domain.user.entity.User;
import com.komme.domain.auth.enums.Provider;
import com.komme.domain.auth.exception.AuthErrorStatus;
import com.komme.domain.user.repository.UserRepository;

import java.util.Optional;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserReader {

    private final UserRepository userRepository;

    // 사용자 ID 기반 조회 기능
    public Optional<User> findById(Long userId) {
        return userRepository.findById(userId);
    }

    // 사용자 이메일 기반 조회 기능
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // 사용자 ID 필수 조회 기능
    public User findByIdOrThrow(Long userId) {
        return findById(userId)
                .orElseThrow(() -> new GeneralException(AuthErrorStatus.INVALID_TOKEN));
    }

    // LOCAL 이메일 사용자 필수 조회 기능
    public User findLocalByEmailOrThrow(String email) {
        User user = findByEmail(email)
                .orElseThrow(() -> new GeneralException(AuthErrorStatus.INVALID_CREDENTIALS));
        return validateLocalUser(user, AuthErrorStatus.INVALID_CREDENTIALS);
    }

    // LOCAL ID 사용자 필수 조회 기능
    public User findLocalByIdOrThrow(Long userId) {
        return validateLocalUser(findByIdOrThrow(userId), AuthErrorStatus.INVALID_TOKEN);
    }

    // 사용자 ID 존재 여부 조회 기능
    public boolean existsById(Long userId) {
        return userRepository.existsById(userId);
    }

    // LOCAL 사용자 여부 검증 기능
    private User validateLocalUser(User user, AuthErrorStatus errorStatus) {
        if (user.getProvider() != Provider.LOCAL) {
            throw new GeneralException(errorStatus);
        }
        return user;
    }
}
