package com.komme.domain.user.repository;

import com.komme.domain.user.entity.User;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    // 이메일 기반 사용자 조회 기능
    Optional<User> findByEmail(String email);

    // 이메일 중복 여부 확인 기능
    boolean existsByEmail(String email);

    // 닉네임 중복 여부 확인 기능
    boolean existsByNickname(String nickname);

}
