package com.komme.domain.spot.repository;

import java.math.BigDecimal;

import com.komme.domain.spot.entity.Spot;
import com.komme.domain.spot.enums.TimeSlot;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:komme_spot_repository_test;MODE=MySQL;NON_KEYWORDS=USER;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
@Transactional
class SpotRepositoryJpaTests {

    @Autowired
    private SpotRepository spotRepository;

    // contentId 유니크 제약조건 검증
    @Test
    void spotRepositoryEnforcesUniqueContentId() {
        spotRepository.saveAndFlush(createSpot("126508"));

        Spot duplicateSpot = createSpot("126508");

        assertThatThrownBy(() -> spotRepository.saveAndFlush(duplicateSpot))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    // contentId 기반 조회 검증
    @Test
    void spotRepositoryFindsByContentId() {
        Spot spot = spotRepository.saveAndFlush(createSpot("126508"));

        assertThat(spotRepository.findByContentId("126508")).contains(spot);
        assertThat(spotRepository.findByContentId("no-such-id")).isEmpty();
    }

    private Spot createSpot(String contentId) {
        return Spot.create(contentId, new Spot.Attributes(
                "성수동 카페",
                "A05", "A0502", "A05020900",
                TimeSlot.LUNCH,
                new BigDecimal("37.5443300"), new BigDecimal("127.0557800"),
                "1", "2",
                "https://tong.visitkorea.or.kr/thumb.jpg"
        ));
    }
}
