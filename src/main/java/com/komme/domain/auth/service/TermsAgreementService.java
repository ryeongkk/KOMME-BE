package com.komme.domain.auth.service;

import com.komme.domain.auth.dto.request.TermsAgreementRequest;
import com.komme.domain.auth.entity.TermsAgreement;
import com.komme.domain.auth.entity.User;
import com.komme.domain.auth.enums.TermsType;
import com.komme.domain.auth.repository.TermsAgreementRepository;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TermsAgreementService {

    private final TermsAgreementRepository termsAgreementRepository;
    private final UserReader userReader;

    // 사용자 약관 동의 저장 기능
    @Transactional
    public void agree(Long userId, TermsAgreementRequest request) {
        User user = userReader.findByIdOrThrow(userId);

        Map<TermsType, Boolean> agreements = Map.of(
                TermsType.SERVICE_TERMS, request.serviceTermsAgreed(),
                TermsType.PRIVACY_POLICY, request.privacyPolicyAgreed(),
                TermsType.LOCATION_TERMS, request.locationTermsAgreed(),
                TermsType.LOCATION_COLLECTION, request.locationCollectionAgreed(),
                TermsType.MARKETING, request.marketingAgreed(),
                TermsType.PUSH_NOTIFICATION, request.pushNotificationAgreed(),
                TermsType.AGE_CONFIRMATION, request.ageConfirmed()
        );
        agreements.forEach((termsType, agreed) -> saveOrUpdate(user, termsType, agreed));
    }

    // 사용자 필수 약관 동의 여부 조회 기능
    @Transactional(readOnly = true)
    public boolean areRequiredTermsAgreed(Long userId) {
        return termsAgreementRepository.countByUserIdAndTermsTypeInAndAgreedTrue(
                userId,
                TermsType.requiredTypes()
        ) == TermsType.requiredTypes().size();
    }

    // 약관 동의 저장 또는 갱신 기능
    private void saveOrUpdate(User user, TermsType termsType, boolean agreed) {
        TermsAgreement termsAgreement = termsAgreementRepository
                .findByUserIdAndTermsType(user.getId(), termsType)
                .orElseGet(() -> TermsAgreement.create(user, termsType, agreed));

        termsAgreement.updateAgreement(agreed);
        termsAgreementRepository.save(termsAgreement);
    }
}
