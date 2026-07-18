package com.komme.domain.auth.repository;

import com.komme.domain.auth.entity.TermsAgreement;
import com.komme.domain.auth.enums.TermsType;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TermsAgreementRepository extends JpaRepository<TermsAgreement, Long> {

    // 사용자 약관 동의 조회 기능
    Optional<TermsAgreement> findByUserIdAndTermsType(Long userId, TermsType termsType);

    // 사용자 약관 동의 목록 조회 기능
    List<TermsAgreement> findByUserIdAndTermsTypeIn(
            Long userId,
            Collection<TermsType> termsTypes
    );

    // 사용자 필수 약관 동의 개수 조회 기능
    long countByUserIdAndTermsTypeInAndAgreedTrue(
            Long userId,
            Collection<TermsType> termsTypes
    );
}
