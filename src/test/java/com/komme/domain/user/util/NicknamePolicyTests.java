package com.komme.domain.user.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NicknamePolicyTests {

    // 닉네임 정책 유효값 허용 검증
    @Test
    void patternAcceptsValidNickname() {
        assertThat("ab").matches(NicknamePolicy.PATTERN);
        assertThat("nickname123").matches(NicknamePolicy.PATTERN);
    }

    // 닉네임 정책 길이 범위 거부 검증
    @Test
    void patternRejectsInvalidLength() {
        assertThat("a").doesNotMatch(NicknamePolicy.PATTERN);
        assertThat("abcdefghijklmnopqrstu").doesNotMatch(NicknamePolicy.PATTERN);
    }

    // 닉네임 정책 영문 숫자 외 문자 거부 검증
    @Test
    void patternRejectsNonAlphanumeric() {
        assertThat("nick_name").doesNotMatch(NicknamePolicy.PATTERN);
        assertThat("닉네임").doesNotMatch(NicknamePolicy.PATTERN);
    }
}
