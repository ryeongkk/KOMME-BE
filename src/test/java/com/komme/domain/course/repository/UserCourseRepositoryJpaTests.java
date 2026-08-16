package com.komme.domain.course.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import com.komme.domain.course.entity.Course;
import com.komme.domain.course.entity.UserCourse;
import com.komme.domain.course.enums.Topic;
import com.komme.domain.user.entity.User;
import com.komme.domain.user.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:komme_user_course_repository_test;MODE=MySQL;NON_KEYWORDS=USER;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
@Transactional
class UserCourseRepositoryJpaTests {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserCourseRepository userCourseRepository;

    // UPCOMING 조회가 오늘 이후(오늘 포함) 저장된 코스만 D-day 임박순으로 반환하는지 검증
    @Test
    void findUpcomingUserCoursesReturnsFutureCoursesInAscendingOrder() {
        User user = userRepository.saveAndFlush(createLocalUser());
        LocalDate today = LocalDate.now();
        userCourseRepository.saveAndFlush(userCourse(user, today.plusDays(10), "먼 코스"));
        userCourseRepository.saveAndFlush(userCourse(user, today.plusDays(1), "가까운 코스"));
        userCourseRepository.saveAndFlush(userCourse(user, today, "오늘 코스")); // 오늘 - 경계값, 포함되어야 함
        userCourseRepository.saveAndFlush(userCourse(user, today.minusDays(1), "지난 코스")); // 과거 - 제외되어야 함

        List<UserCourse> result = userCourseRepository
                .findByUser_IdAndCourse_VisitDateGreaterThanEqualOrderByCourse_VisitDateAsc(user.getId(), today);

        assertThat(result).extracting(UserCourse::getTitle)
                .containsExactly("오늘 코스", "가까운 코스", "먼 코스");
    }

    // HISTORY 조회가 오늘 이전 저장된 코스만 최근 완료순으로 반환하는지 검증
    @Test
    void findHistoryUserCoursesReturnsPastCoursesInDescendingOrder() {
        User user = userRepository.saveAndFlush(createLocalUser());
        LocalDate today = LocalDate.now();
        userCourseRepository.saveAndFlush(userCourse(user, today.minusDays(10), "오래된 코스"));
        userCourseRepository.saveAndFlush(userCourse(user, today.minusDays(1), "최근 코스"));
        userCourseRepository.saveAndFlush(userCourse(user, today, "오늘 코스")); // 오늘 - UPCOMING 몫이라 제외되어야 함

        List<UserCourse> result = userCourseRepository
                .findByUser_IdAndCourse_VisitDateLessThanOrderByCourse_VisitDateDesc(user.getId(), today);

        assertThat(result).extracting(UserCourse::getTitle)
                .containsExactly("최근 코스", "오래된 코스");
    }

    // 같은 사용자가 같은 코스를 두 번 저장하면 유니크 제약 위반이 나는지 검증
    @Test
    void userCourseRepositoryEnforcesUniqueUserAndCourse() {
        User user = userRepository.saveAndFlush(createLocalUser());
        Course course = courseRepository.saveAndFlush(course(user, LocalDate.now().plusDays(1)));
        userCourseRepository.saveAndFlush(UserCourse.create(user, course, "첫 저장"));

        UserCourse duplicate = UserCourse.create(user, course, "두 번째 저장");

        assertThatThrownBy(() -> userCourseRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    // 코스 ID 기반 저장 기록 전체 삭제 기능 검증 (코스 삭제 시 선행되어야 하는 정리 작업)
    @Test
    void deleteByCourseIdRemovesUserCourse() {
        User user = userRepository.saveAndFlush(createLocalUser());
        Course course = courseRepository.saveAndFlush(course(user, LocalDate.now().plusDays(1)));
        userCourseRepository.saveAndFlush(UserCourse.create(user, course, "저장한 코스"));

        userCourseRepository.deleteByCourse_Id(course.getId());
        userCourseRepository.flush();

        assertThat(userCourseRepository.findByUser_IdAndCourse_Id(user.getId(), course.getId())).isEmpty();
    }

    private UserCourse userCourse(User user, LocalDate visitDate, String title) {
        Course course = courseRepository.saveAndFlush(course(user, visitDate));
        return UserCourse.create(user, course, title);
    }

    private Course course(User user, LocalDate visitDate) {
        return Course.create(user, "성동구", "11", "11200", Set.of(Topic.FOOD), visitDate);
    }

    private User createLocalUser() {
        return User.createLocal("user@example.com", "encoded-password", "nickname");
    }
}
