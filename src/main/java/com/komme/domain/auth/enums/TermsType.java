package com.komme.domain.auth.enums;

import java.util.Set;

public enum TermsType {
    SERVICE_TERMS,
    PRIVACY_POLICY,
    LOCATION_TERMS,
    LOCATION_COLLECTION,
    MARKETING,
    PUSH_NOTIFICATION,
    AGE_CONFIRMATION;

    // 필수 약관 유형 조회 기능
    public static Set<TermsType> requiredTypes() {
        return Set.of(
                SERVICE_TERMS,
                PRIVACY_POLICY,
                LOCATION_TERMS,
                LOCATION_COLLECTION,
                AGE_CONFIRMATION
        );
    }
}
