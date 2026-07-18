package com.komme.domain.auth.service;

import com.komme.domain.auth.dto.request.TermsAgreementRequest;
import com.komme.domain.auth.entity.TermsAgreement;
import com.komme.domain.user.entity.User;
import com.komme.domain.auth.enums.TermsType;
import com.komme.domain.auth.repository.TermsAgreementRepository;
import com.komme.domain.user.service.UserReader;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    // 사용자 선택 약관 동의 상태 조회 기능
    @Transactional(readOnly = true)
    public Map<TermsType, Boolean> getOptionalConsentAgreements(Long userId) {
        Map<TermsType, TermsAgreement> agreements = findOptionalConsentAgreements(userId);

        return TermsType.optionalConsentTypes().stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        termsType -> resolveAgreement(agreements, termsType)
                ));
    }

    // 약관 동의 저장 또는 갱신 기능
    private void saveOrUpdate(User user, TermsType termsType, boolean agreed) {
        TermsAgreement termsAgreement = termsAgreementRepository
                .findByUserIdAndTermsType(user.getId(), termsType)
                .orElseGet(() -> TermsAgreement.create(user, termsType, agreed));

        termsAgreement.updateAgreement(agreed);
        termsAgreementRepository.save(termsAgreement);
    }

    // 사용자 선택 약관 동의 엔티티 목록 조회 기능
    private Map<TermsType, TermsAgreement> findOptionalConsentAgreements(Long userId) {
        List<TermsAgreement> agreements = termsAgreementRepository.findByUserIdAndTermsTypeIn(
                userId,
                TermsType.optionalConsentTypes()
        );

        return agreements.stream()
                .collect(Collectors.toMap(
                        TermsAgreement::getTermsType,
                        Function.identity()
                ));
    }

    // 약관 동의 상태 기본값 조회 기능
    private boolean resolveAgreement(
            Map<TermsType, TermsAgreement> agreements,
            TermsType termsType
    ) {
        TermsAgreement termsAgreement = agreements.get(termsType);
        return termsAgreement != null && termsAgreement.isAgreed();
    }

}
