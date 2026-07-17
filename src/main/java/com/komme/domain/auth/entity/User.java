package com.komme.domain.auth.entity;

import com.komme.common.base.BaseEntity;
import com.komme.domain.auth.enums.Gender;
import com.komme.domain.auth.enums.Provider;
import com.komme.domain.auth.enums.ServiceInterest;
import com.komme.i18n.enums.Language;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "user")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(length = 255)
    private String password;

    @Column(nullable = false, unique = true, length = 20)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Provider provider;

    @Column(length = 255)
    private String providerId;

    @Column(nullable = false, length = 2)
    private String nationality;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Language preferredLanguage;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "user_service_interest",
            joinColumns = @JoinColumn(name = "user_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "service_interest", nullable = false, length = 30)
    private Set<ServiceInterest> serviceInterests = new HashSet<>();

    // 사용자 엔티티 생성
    @Builder(access = AccessLevel.PRIVATE)
    private User(
            String email,
            String password,
            String nickname,
            Provider provider,
            String providerId,
            String nationality,
            Gender gender,
            Language preferredLanguage,
            Set<ServiceInterest> serviceInterests
    ) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.provider = provider;
        this.providerId = providerId;
        this.nationality = nationality;
        this.gender = gender;
        this.preferredLanguage = preferredLanguage;
        this.serviceInterests = new HashSet<>(serviceInterests);
    }

    // LOCAL 사용자 생성 기능
    public static User createLocal(
            String email,
            String encodedPassword,
            String nickname,
            String nationality,
            Gender gender,
            Language preferredLanguage,
            Set<ServiceInterest> serviceInterests
    ) {
        return User.builder()
                .email(email)
                .password(encodedPassword)
                .nickname(nickname)
                .provider(Provider.LOCAL)
                .providerId(null)
                .nationality(nationality)
                .gender(gender)
                .preferredLanguage(preferredLanguage)
                .serviceInterests(serviceInterests)
                .build();
    }
}
