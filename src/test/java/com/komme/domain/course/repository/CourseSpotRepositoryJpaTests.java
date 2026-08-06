package com.komme.domain.course.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import com.komme.domain.course.entity.Course;
import com.komme.domain.course.entity.CourseSpot;
import com.komme.domain.course.enums.Topic;
import com.komme.domain.i18n.enums.Language;
import com.komme.domain.spot.entity.Spot;
import com.komme.domain.spot.enums.TimeSlot;
import com.komme.domain.spot.repository.SpotRepository;
import com.komme.domain.user.entity.User;
import com.komme.domain.user.enums.Gender;
import com.komme.domain.user.enums.ServiceInterest;
import com.komme.domain.user.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:komme_course_spot_repository_test;MODE=MySQL;NON_KEYWORDS=USER;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
@Transactional
class CourseSpotRepositoryJpaTests {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SpotRepository spotRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CourseSpotRepository courseSpotRepository;

    // 코스 상세 조회용 방문 순서(sequence) 정렬이 실제로 동작하는지 검증
    @Test
    void findByCourseIdOrdersBySequence() {
        User user = userRepository.saveAndFlush(User.createLocal(
                "user@example.com", "encoded-password", "nickname",
                "KR", Gender.FEMALE, Language.ENGLISH, Set.of(ServiceInterest.COURSE)
        ));
        Course course = courseRepository.saveAndFlush(Course.create(
                user, "성동구 먹방 Day", null, "성동구", "11", "11200",
                Set.of(Topic.FOOD), LocalDate.of(2026, 8, 10)
        ));
        Spot first = spotRepository.saveAndFlush(spot("1"));
        Spot second = spotRepository.saveAndFlush(spot("2"));
        // 저장 순서를 일부러 뒤섞어도 sequence 기준으로 정렬되어야 한다
        courseSpotRepository.saveAndFlush(CourseSpot.create(course, second, 2, TimeSlot.LUNCH, null));
        courseSpotRepository.saveAndFlush(CourseSpot.create(course, first, 1, TimeSlot.MORNING, 500));

        List<CourseSpot> result = courseSpotRepository.findByCourse_IdOrderBySequenceAsc(course.getId());

        assertThat(result).extracting(CourseSpot::getSequence).containsExactly(1, 2);
        assertThat(result.get(0).getSpot().getContentId()).isEqualTo("1");
    }

    // deleteByCourse_Id로 코스 스팟을 먼저 지우면 course_spot -> course FK 제약 위반 없이 Course도 지울 수 있는지 검증
    @Test
    void deleteByCourseIdAllowsSubsequentCourseDeletionWithoutForeignKeyViolation() {
        User user = userRepository.saveAndFlush(User.createLocal(
                "user2@example.com", "encoded-password", "nickname2",
                "KR", Gender.FEMALE, Language.ENGLISH, Set.of(ServiceInterest.COURSE)
        ));
        Course course = courseRepository.saveAndFlush(Course.create(
                user, "성동구 먹방 Day", null, "성동구", "11", "11200",
                Set.of(Topic.FOOD), LocalDate.of(2026, 8, 10)
        ));
        Spot spot = spotRepository.saveAndFlush(spot("3"));
        courseSpotRepository.saveAndFlush(CourseSpot.create(course, spot, 1, TimeSlot.MORNING, null));

        courseSpotRepository.deleteByCourse_Id(course.getId());
        courseSpotRepository.flush();
        courseRepository.delete(course);
        courseRepository.flush();

        assertThat(courseRepository.findById(course.getId())).isEmpty();
        assertThat(courseSpotRepository.findByCourse_IdOrderBySequenceAsc(course.getId())).isEmpty();
    }

    private Spot spot(String contentId) {
        return Spot.create(contentId, new Spot.Attributes(
                "spot-" + contentId, "A05", "A0502", "A05020900", TimeSlot.MORNING,
                new BigDecimal("37.5443300"), new BigDecimal("127.0557800"),
                "11", "11200", null
        ));
    }
}
