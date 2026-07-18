package com.komme.domain.auth.entity;

import com.komme.common.base.BaseEntity;
import com.komme.domain.user.enums.Provider;
import com.komme.domain.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "oauth_account",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_oauth_account_provider_id",
                columnNames = {"provider", "provider_id"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OAuthAccount extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Provider provider;

    @Column(name = "provider_id", nullable = false, length = 255)
    private String providerId;

    // OAuth 계정 엔티티 생성
    @Builder(access = AccessLevel.PRIVATE)
    private OAuthAccount(User user, Provider provider, String providerId) {
        this.user = user;
        this.provider = provider;
        this.providerId = providerId;
    }

    // 사용자 OAuth 계정 연결 생성
    public static OAuthAccount create(User user, Provider provider, String providerId) {
        return OAuthAccount.builder()
                .user(user)
                .provider(provider)
                .providerId(providerId)
                .build();
    }
}
