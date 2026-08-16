package com.komme.domain.user.entity;

import com.komme.common.base.BaseEntity;
import com.komme.domain.user.enums.Gender;
import com.komme.domain.user.enums.Provider;
import com.komme.domain.user.enums.ServiceInterest;
import com.komme.domain.i18n.enums.Language;

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
import jakarta.persistence.UniqueConstraint;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "user",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_email", columnNames = "email"),
                @UniqueConstraint(name = "uk_user_nickname", columnNames = "nickname")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(length = 255)
    private String password;

    @Column(length = 20)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Provider provider;

    @Column(length = 2)
    private String nationality;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private Language preferredLanguage;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "user_service_interest",
            joinColumns = @JoinColumn(name = "user_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "service_interest", nullable = false, length = 30)
    private Set<ServiceInterest> serviceInterests = new HashSet<>();

    @Column(nullable = false)
    private boolean locationConsentAgreed;

    // 사용자 엔티티 생성
    @Builder(access = AccessLevel.PRIVATE)
    private User(
            String email,
            String password,
            String nickname,
            Provider provider,
            String nationality,
            Gender gender,
            Language preferredLanguage,
            Set<ServiceInterest> serviceInterests
    ) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.provider = provider;
        this.nationality = nationality;
        this.gender = gender;
        this.preferredLanguage = preferredLanguage;
        this.serviceInterests = new HashSet<>(serviceInterests);
    }

    // LOCAL 사용자 생성 기능
    // 국적/성별/선호 언어/관심 서비스는 회원가입 시점에 받지 않으며 OAuth 사용자와 동일하게 null/빈 값으로 시작한다.
    public static User createLocal(
            String email,
            String encodedPassword,
            String nickname
    ) {
        return User.builder()
                .email(email)
                .password(encodedPassword)
                .nickname(nickname)
                .provider(Provider.LOCAL)
                .nationality(null)
                .gender(null)
                .preferredLanguage(null)
                .serviceInterests(Set.of())
                .build();
    }

    // OAuth 사용자 생성 기능
    public static User createOAuth(String email, Provider provider) {
        return User.builder()
                .email(email)
                .password(null)
                .nickname(null)
                .provider(provider)
                .nationality(null)
                .gender(null)
                .preferredLanguage(null)
                .serviceInterests(Set.of())
                .build();
    }

    // OAuth 사용자 프로필 완성 기능
    public void completeProfile(String nickname) {
        this.nickname = nickname;
    }

    // 비밀번호 변경 기능
    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    // 닉네임 변경 기능
    public void changeNickname(String nickname) {
        this.nickname = nickname;
    }

    // 선호 언어 변경 기능
    public void changePreferredLanguage(Language preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }

    // 위치 정보 동의 상태 변경 기능
    public void changeLocationConsent(boolean agreed) {
        this.locationConsentAgreed = agreed;
    }

    // 사용자 프로필 완성 여부 조회 기능
    // OAuth 최소 프로필 사용자는 닉네임 설정 전까지 미완성 상태다.
    // 국적/성별/선호언어/관심서비스는 별도 온보딩 단계에서 채워지며 이 판단에 관여하지 않는다.
    public boolean isProfileCompleted() {
        return nickname != null;
    }
}
