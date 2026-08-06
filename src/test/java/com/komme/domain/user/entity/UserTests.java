package com.komme.domain.user.entity;

import com.komme.domain.user.enums.Gender;
import com.komme.domain.user.enums.Provider;
import com.komme.domain.user.enums.ServiceInterest;
import com.komme.domain.i18n.enums.Language;

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

    // 사용자 선호 언어 변경 검증
    @Test
    void changePreferredLanguageUpdatesPreferredLanguage() {
        User user = createUser();

        user.changePreferredLanguage(Language.JAPANESE);

        assertThat(user.getPreferredLanguage()).isEqualTo(Language.JAPANESE);
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

    // OAuth 사용자 프로필 미완성 상태 검증
    @Test
    void createOAuthCreatesIncompleteProfileUser() {
        User user = User.createOAuth("apple@example.com", Provider.APPLE);

        assertThat(user.isProfileCompleted()).isFalse();
    }

    // OAuth 사용자 프로필 완성 상태 변경 검증
    @Test
    void completeProfileUpdatesProfileAndCompletionState() {
        User user = User.createOAuth("google@example.com", Provider.GOOGLE);

        user.completeProfile(
                "nickname",
                "KR",
                Gender.MALE,
                Language.CHINESE_SIMPLIFIED,
                Set.of(ServiceInterest.TOURIST_SPOT)
        );

        assertThat(user.getNickname()).isEqualTo("nickname");
        assertThat(user.getNationality()).isEqualTo("KR");
        assertThat(user.getGender()).isEqualTo(Gender.MALE);
        assertThat(user.getPreferredLanguage()).isEqualTo(Language.CHINESE_SIMPLIFIED);
        assertThat(user.getServiceInterests()).containsExactly(ServiceInterest.TOURIST_SPOT);
        assertThat(user.isProfileCompleted()).isTrue();
    }

    // 닉네임만 비어있어도 프로필 미완성으로 판단되는지 검증
    @Test
    void isProfileCompletedReturnsFalseWhenNicknameMissing() {
        User user = User.createOAuth("google@example.com", Provider.GOOGLE);
        user.completeProfile(null, "KR", Gender.MALE, Language.ENGLISH, Set.of(ServiceInterest.COURSE));

        assertThat(user.isProfileCompleted()).isFalse();
    }

    // 국적만 비어있어도 프로필 미완성으로 판단되는지 검증
    @Test
    void isProfileCompletedReturnsFalseWhenNationalityMissing() {
        User user = User.createOAuth("google@example.com", Provider.GOOGLE);
        user.completeProfile("nickname", null, Gender.MALE, Language.ENGLISH, Set.of(ServiceInterest.COURSE));

        assertThat(user.isProfileCompleted()).isFalse();
    }

    // 성별만 비어있어도 프로필 미완성으로 판단되는지 검증
    @Test
    void isProfileCompletedReturnsFalseWhenGenderMissing() {
        User user = User.createOAuth("google@example.com", Provider.GOOGLE);
        user.completeProfile("nickname", "KR", null, Language.ENGLISH, Set.of(ServiceInterest.COURSE));

        assertThat(user.isProfileCompleted()).isFalse();
    }

    // 선호 언어만 비어있어도 프로필 미완성으로 판단되는지 검증
    @Test
    void isProfileCompletedReturnsFalseWhenPreferredLanguageMissing() {
        User user = User.createOAuth("google@example.com", Provider.GOOGLE);
        user.completeProfile("nickname", "KR", Gender.MALE, null, Set.of(ServiceInterest.COURSE));

        assertThat(user.isProfileCompleted()).isFalse();
    }

    // 관심 서비스가 빈 Set이어도 프로필 미완성으로 판단되는지 검증
    // 참고: serviceInterests는 completeProfile/createLocal 양쪽 다 new HashSet<>(...)로 감싸기 때문에
    // null을 넘기면 NPE가 나서, isProfileCompleted()의 "serviceInterests != null" 체크는
    // 실제로는 도달 불가능한 방어 코드다.
    @Test
    void isProfileCompletedReturnsFalseWhenServiceInterestsEmpty() {
        User user = User.createOAuth("google@example.com", Provider.GOOGLE);
        user.completeProfile("nickname", "KR", Gender.MALE, Language.ENGLISH, Set.of());

        assertThat(user.isProfileCompleted()).isFalse();
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
