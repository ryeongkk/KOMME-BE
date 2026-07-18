package com.komme.domain.auth.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

public record TermsAgreementRequest(
        @NotNull(message = "서비스 이용약관 동의 여부는 필수입니다.")
        @AssertTrue(message = "서비스 이용약관에 동의해야 합니다.")
        Boolean serviceTermsAgreed,

        @NotNull(message = "개인정보처리방침 동의 여부는 필수입니다.")
        @AssertTrue(message = "개인정보처리방침에 동의해야 합니다.")
        Boolean privacyPolicyAgreed,

        @NotNull(message = "위치정보 이용약관 동의 여부는 필수입니다.")
        @AssertTrue(message = "위치정보 이용약관에 동의해야 합니다.")
        Boolean locationTermsAgreed,

        @NotNull(message = "위치정보 수집·이용 동의 여부는 필수입니다.")
        @AssertTrue(message = "위치정보 수집·이용에 동의해야 합니다.")
        Boolean locationCollectionAgreed,

        @NotNull(message = "마케팅 정보 수신 동의 여부는 필수입니다.")
        Boolean marketingAgreed,

        @NotNull(message = "푸시 알림 수신 동의 여부는 필수입니다.")
        Boolean pushNotificationAgreed,

        @NotNull(message = "만 14세 이상 확인 여부는 필수입니다.")
        @AssertTrue(message = "만 14세 이상만 가입할 수 있습니다.")
        Boolean ageConfirmed
) {
}
