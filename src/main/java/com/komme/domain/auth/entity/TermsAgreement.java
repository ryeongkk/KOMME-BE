package com.komme.domain.auth.entity;

import com.komme.common.base.BaseEntity;
import com.komme.domain.auth.enums.TermsType;

import java.time.LocalDateTime;

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
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "terms_agreement",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_terms_agreement_user_type",
                columnNames = {"user_id", "terms_type"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TermsAgreement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "terms_type", nullable = false, length = 30)
    private TermsType termsType;

    @Column(nullable = false)
    private boolean agreed;

    @Column(name = "agreed_at")
    private LocalDateTime agreedAt;

    // 약관 동의 엔티티 생성
    private TermsAgreement(User user, TermsType termsType, boolean agreed) {
        this.user = user;
        this.termsType = termsType;
        updateAgreement(agreed);
    }

    // 약관 동의 엔티티 생성 기능
    public static TermsAgreement create(User user, TermsType termsType, boolean agreed) {
        return new TermsAgreement(user, termsType, agreed);
    }

    // 약관 동의 상태 변경 기능
    public void updateAgreement(boolean agreed) {
        this.agreed = agreed;
        this.agreedAt = agreed ? LocalDateTime.now() : null;
    }
}
