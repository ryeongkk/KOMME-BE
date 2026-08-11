package com.komme.domain.user.repository;

import com.komme.domain.auth.entity.OAuthAccount;
import com.komme.domain.auth.repository.OAuthAccountRepository;
import com.komme.domain.i18n.enums.Language;
import com.komme.domain.user.entity.User;
import com.komme.domain.user.enums.Gender;
import com.komme.domain.user.enums.Provider;
import com.komme.domain.user.enums.ServiceInterest;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:komme_repository_test;MODE=MySQL;NON_KEYWORDS=USER;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
@Transactional
class RepositoryJpaTests {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OAuthAccountRepository oAuthAccountRepository;

    // 사용자 이메일 유니크 제약조건 검증
    @Test
    void userRepositoryEnforcesUniqueEmail() {
        userRepository.saveAndFlush(createLocalUser("user@example.com", "nickname"));

        User duplicateUser = createLocalUser("user@example.com", "nickname2");

        assertThatThrownBy(() -> userRepository.saveAndFlush(duplicateUser))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    // 사용자 닉네임 유니크 제약조건 검증
    @Test
    void userRepositoryEnforcesUniqueNickname() {
        userRepository.saveAndFlush(createLocalUser("user1@example.com", "nickname"));

        User duplicateUser = createLocalUser("user2@example.com", "nickname");

        assertThatThrownBy(() -> userRepository.saveAndFlush(duplicateUser))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    // OAuth 계정 provider와 provider ID 유니크 제약조건 검증
    @Test
    void oAuthAccountRepositoryEnforcesUniqueProviderAndProviderId() {
        User firstUser = userRepository.saveAndFlush(createLocalUser(
                "user1@example.com",
                "nickname1"
        ));
        User secondUser = userRepository.saveAndFlush(createLocalUser(
                "user2@example.com",
                "nickname2"
        ));
        oAuthAccountRepository.saveAndFlush(
                OAuthAccount.create(firstUser, Provider.GOOGLE, "provider-id")
        );

        OAuthAccount duplicateAccount =
                OAuthAccount.create(secondUser, Provider.GOOGLE, "provider-id");

        assertThatThrownBy(() -> oAuthAccountRepository.saveAndFlush(duplicateAccount))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    // 사용자 ID 기반 OAuth 계정 전체 삭제 검증
    @Test
    void oAuthAccountRepositoryDeletesAllByUserId() {
        User user = userRepository.saveAndFlush(createLocalUser("user@example.com", "nickname"));
        oAuthAccountRepository.saveAndFlush(
                OAuthAccount.create(user, Provider.GOOGLE, "google-id")
        );
        oAuthAccountRepository.saveAndFlush(
                OAuthAccount.create(user, Provider.APPLE, "apple-id")
        );

        oAuthAccountRepository.deleteAllByUserId(user.getId());
        oAuthAccountRepository.flush();

        assertThat(oAuthAccountRepository.findByProviderAndProviderId(
                Provider.GOOGLE,
                "google-id"
        )).isEmpty();
        assertThat(oAuthAccountRepository.findByProviderAndProviderId(
                Provider.APPLE,
                "apple-id"
        )).isEmpty();
    }

    // 사용자 Repository 조회 메서드 검증
    @Test
    void userRepositoryFindsAndChecksUserFields() {
        User user = userRepository.saveAndFlush(createLocalUser("user@example.com", "nickname"));

        assertThat(userRepository.findByEmail("user@example.com")).contains(user);
        assertThat(userRepository.existsByEmail("user@example.com")).isTrue();
        assertThat(userRepository.existsByNickname("nickname")).isTrue();
        assertThat(userRepository.existsByNicknameAndIdNot("nickname", user.getId())).isFalse();
    }

    // 테스트 LOCAL 사용자 생성
    private User createLocalUser(String email, String nickname) {
        return User.createLocal(
                email,
                "encoded-password",
                nickname,
                "KR",
                Gender.FEMALE,
                Language.ENGLISH,
                Set.of(ServiceInterest.COURSE)
        );
    }
}
