package com.komme.domain.user.service;

import com.komme.common.exception.GeneralException;
import com.komme.domain.user.entity.User;
import com.komme.domain.user.exception.UserErrorStatus;
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
                .orElseThrow(() -> new GeneralException(UserErrorStatus.USER_NOT_FOUND));
    }

    // 사용자 ID 존재 여부 조회 기능
    public boolean existsById(Long userId) {
        return userRepository.existsById(userId);
    }

    // 사용자 닉네임 존재 여부 조회 기능
    public boolean existsByNickname(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

}
