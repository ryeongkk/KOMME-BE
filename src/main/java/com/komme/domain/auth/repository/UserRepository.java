경package com.komme.domain.auth.repository;

import com.komme.domain.auth.entity.User;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    // 이메일로 사용자를 조회한다.
    Optional<User> findByEmail(String email);

    // 이메일 중복 여부를 확인한다.
    boolean existsByEmail(String email);

    // 닉네임 중복 여부를 확인한다.
    boolean existsByNickname(String nickname);
}
