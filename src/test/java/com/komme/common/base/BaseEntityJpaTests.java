package com.komme.common.base;

import com.komme.domain.i18n.enums.Language;
import com.komme.domain.user.entity.User;
import com.komme.domain.user.enums.Gender;
import com.komme.domain.user.enums.ServiceInterest;
import com.komme.domain.user.repository.UserRepository;

import java.util.Set;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:komme_base_entity_test;MODE=MySQL;NON_KEYWORDS=USER;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
@Transactional
class BaseEntityJpaTests {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    // BaseEntity 생성 및 수정 시각 자동 저장 검증
    @Test
    void baseEntityStoresAuditTimestampsOnPersist() {
        User user = userRepository.saveAndFlush(createUser());
        entityManager.clear();

        User savedUser = userRepository.findById(user.getId()).orElseThrow();

        assertThat(savedUser.getCreatedAt()).isNotNull();
        assertThat(savedUser.getUpdatedAt()).isNotNull();
        assertThat(savedUser.getUpdatedAt()).isAfterOrEqualTo(savedUser.getCreatedAt());
    }

    // 테스트 사용자 생성
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
