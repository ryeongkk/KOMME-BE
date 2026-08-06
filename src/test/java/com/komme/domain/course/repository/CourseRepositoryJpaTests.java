package com.komme.domain.course.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import com.komme.domain.course.entity.Course;
import com.komme.domain.course.enums.Topic;
import com.komme.domain.i18n.enums.Language;
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
        "spring.datasource.url=jdbc:h2:mem:komme_course_repository_test;MODE=MySQL;NON_KEYWORDS=USER;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
@Transactional
class CourseRepositoryJpaTests {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    // UPCOMING 조회가 오늘 이후 코스만 D-day 임박순으로 반환하는지 검증
    @Test
    void findUpcomingCoursesReturnsFutureCoursesInAscendingOrder() {
        User user = userRepository.saveAndFlush(createLocalUser());
        LocalDate today = LocalDate.now();
        courseRepository.saveAndFlush(course(user, today.plusDays(10)));
        courseRepository.saveAndFlush(course(user, today.plusDays(1)));
        courseRepository.saveAndFlush(course(user, today.minusDays(1))); // 과거 - 결과에서 제외되어야 함

        List<Course> result = courseRepository.findByUser_IdAndVisitDateGreaterThanEqualOrderByVisitDateAsc(
                user.getId(), today
        );

        assertThat(result).extracting(Course::getVisitDate)
                .containsExactly(today.plusDays(1), today.plusDays(10));
    }

    // HISTORY 조회가 오늘 이전 코스만 최근 완료순으로 반환하는지 검증
    @Test
    void findHistoryCoursesReturnsPastCoursesInDescendingOrder() {
        User user = userRepository.saveAndFlush(createLocalUser());
        LocalDate today = LocalDate.now();
        courseRepository.saveAndFlush(course(user, today.minusDays(10)));
        courseRepository.saveAndFlush(course(user, today.minusDays(1)));
        courseRepository.saveAndFlush(course(user, today)); // 오늘 - 결과에서 제외되어야 함(UPCOMING 몫)

        List<Course> result = courseRepository.findByUser_IdAndVisitDateLessThanOrderByVisitDateDesc(
                user.getId(), today
        );

        assertThat(result).extracting(Course::getVisitDate)
                .containsExactly(today.minusDays(1), today.minusDays(10));
    }

    private Course course(User user, LocalDate visitDate) {
        return Course.create(user, "테스트 코스", null, "성동구", "11", "11200", Set.of(Topic.FOOD), visitDate);
    }

    private User createLocalUser() {
        return User.createLocal(
                "user@example.com", "encoded-password", "nickname",
                "KR", Gender.FEMALE, Language.ENGLISH, Set.of(ServiceInterest.COURSE)
        );
    }
}
