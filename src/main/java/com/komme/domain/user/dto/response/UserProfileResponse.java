package com.komme.domain.user.dto.response;

import com.komme.domain.user.entity.User;
import com.komme.domain.user.enums.Provider;
import com.komme.domain.user.enums.TermsType;
import com.komme.domain.i18n.enums.Language;

import java.util.Map;

public record UserProfileResponse(
        String nickname,
        Provider provider,
        Language preferredLanguage,
        boolean marketingAgreed,
        boolean pushNotificationAgreed
) {

    // 사용자 프로필 응답 생성 기능
    public static UserProfileResponse of(
            User user,
            Map<TermsType, Boolean> optionalConsentAgreements
    ) {
        return new UserProfileResponse(
                user.getNickname(),
                user.getProvider(),
                user.getPreferredLanguage(),
                optionalConsentAgreements.getOrDefault(TermsType.MARKETING, false),
                optionalConsentAgreements.getOrDefault(TermsType.PUSH_NOTIFICATION, false)
        );
    }
}
