package com.komme.domain.user.entity;

import com.komme.domain.auth.enums.Gender;
import com.komme.domain.auth.enums.Provider;
import com.komme.domain.auth.enums.ServiceInterest;
import com.komme.i18n.enums.Language;

import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserTests {

    // LOCAL 사용자 정적 팩토리 생성값 검증
    @Test
    void createLocalCreatesLocalUser() {
        User user = createUser();

        assertThat(user.getEmail()).isEqualTo("user@example.com");
        assertThat(user.getPassword()).isEqualTo("encoded-password");
        assertThat(user.getNickname()).isEqualTo("nickname");
        assertThat(user.getProvider()).isEqualTo(Provider.LOCAL);
        assertThat(user.getNationality()).isEqualTo("KR");
        assertThat(user.getGender()).isEqualTo(Gender.FEMALE);
        assertThat(user.getPreferredLanguage()).isEqualTo(Language.ENGLISH);
        assertThat(user.getServiceInterests()).containsExactly(ServiceInterest.COURSE);
    }

    // 사용자 비밀번호 변경 검증
    @Test
    void changePasswordUpdatesEncodedPassword() {
        User user = createUser();

        user.changePassword("new-encoded-password");

        assertThat(user.getPassword()).isEqualTo("new-encoded-password");
    }

    // 사용자 닉네임 변경 검증
    @Test
    void changeNicknameUpdatesNickname() {
        User user = createUser();

        user.changeNickname("new-nickname");

        assertThat(user.getNickname()).isEqualTo("new-nickname");
    }

    // OAuth 사용자 최소 프로필 생성값 검증
    @Test
    void createOAuthCreatesMinimalProfileUser() {
        User user = User.createOAuth("apple@example.com", Provider.APPLE);

        assertThat(user.getEmail()).isEqualTo("apple@example.com");
        assertThat(user.getProvider()).isEqualTo(Provider.APPLE);
        assertThat(user.getPassword()).isNull();
        assertThat(user.getNickname()).isNull();
        assertThat(user.getNationality()).isNull();
        assertThat(user.getGender()).isNull();
        assertThat(user.getPreferredLanguage()).isNull();
        assertThat(user.getServiceInterests()).isEmpty();
    }

    // 테스트 LOCAL 사용자 생성
    private User createUser() {
        return User.createLocal(
                "user@example.com",
                "encoded-password",
                "nickname",
                "KR",
                Gender.FEMALE,
                Language.ENGLISH,
                Set.of(ServiceInterest.COURSE)
        );
    }
}
