package com.komme.domain.course.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.komme.common.exception.GeneralException;
import com.komme.domain.course.dto.response.CourseDetailResponse;
import com.komme.domain.course.dto.response.CourseSummaryResponse;
import com.komme.domain.course.entity.Course;
import com.komme.domain.course.entity.UserCourse;
import com.komme.domain.course.enums.CourseStatus;
import com.komme.domain.course.enums.Topic;
import com.komme.domain.course.exception.CourseErrorStatus;
import com.komme.domain.course.repository.CourseSpotCount;
import com.komme.domain.course.repository.CourseRepository;
import com.komme.domain.course.repository.CourseSpotRepository;
import com.komme.domain.course.repository.UserCourseRepository;
import com.komme.domain.user.entity.User;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseQueryServiceTests {

    private static final Long USER_ID = 1L;
    private static final Long COURSE_ID = 10L;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private CourseSpotRepository courseSpotRepository;

    @Mock
    private UserCourseRepository userCourseRepository;

    // UPCOMING 조회 시 오름차순(D-day 임박순) 조회 메서드를 타는지 검증
    @Test
    void findListUsesAscendingQueryForUpcoming() {
        CourseQueryService service = new CourseQueryService(courseRepository, courseSpotRepository, userCourseRepository);
        UserCourse userCourse = userCourse("성동구 음식 Day", LocalDate.of(2026, 8, 10));
        when(userCourseRepository.findByUser_IdAndCourse_VisitDateGreaterThanEqualOrderByCourse_VisitDateAsc(eq(USER_ID), any()))
                .thenReturn(List.of(userCourse));
        when(courseSpotRepository.countByCourseIds(List.of(COURSE_ID)))
                .thenReturn(List.of(new CourseSpotCount(COURSE_ID, 4L)));

        List<CourseSummaryResponse> result = service.findList(USER_ID, CourseStatus.UPCOMING);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).title()).isEqualTo("성동구 음식 Day");
        assertThat(result.get(0).spotCount()).isEqualTo(4L);
    }

    // HISTORY 조회 시 내림차순(최근 완료순) 조회 메서드를 타는지 검증
    @Test
    void findListUsesDescendingQueryForHistory() {
        CourseQueryService service = new CourseQueryService(courseRepository, courseSpotRepository, userCourseRepository);
        UserCourse userCourse = userCourse("성동구 힐링 Day", LocalDate.of(2026, 7, 1));
        when(userCourseRepository.findByUser_IdAndCourse_VisitDateLessThanOrderByCourse_VisitDateDesc(eq(USER_ID), any()))
                .thenReturn(List.of(userCourse));
        when(courseSpotRepository.countByCourseIds(List.of(COURSE_ID))).thenReturn(List.of());

        List<CourseSummaryResponse> result = service.findList(USER_ID, CourseStatus.HISTORY);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).title()).isEqualTo("성동구 힐링 Day");
        assertThat(result.get(0).spotCount()).isZero();
    }

    // 본인 코스면 스팟 타임라인과 함께 상세 정보가 반환되는지 검증
    @Test
    void findDetailReturnsDetailWhenOwnedByUser() {
        CourseQueryService service = new CourseQueryService(courseRepository, courseSpotRepository, userCourseRepository);
        User owner = Mockito.mock(User.class);
        when(owner.getId()).thenReturn(USER_ID);
        Course course = course(owner, LocalDate.of(2026, 8, 10));
        when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.of(course));
        when(userCourseRepository.findByUser_IdAndCourse_Id(USER_ID, COURSE_ID))
                .thenReturn(Optional.of(UserCourse.create(owner, course, "성동구 음식 Day")));
        when(courseSpotRepository.findByCourse_IdOrderBySequenceAsc(COURSE_ID)).thenReturn(List.of());

        CourseDetailResponse response = service.findDetail(USER_ID, COURSE_ID);

        assertThat(response.title()).isEqualTo("성동구 음식 Day");
        assertThat(response.regionName()).isEqualTo("성동구");
    }

    // 생성만 하고 아직 저장하지 않은 코스는 제목이 null로 응답되는지 검증
    @Test
    void findDetailReturnsNullTitleWhenNotSaved() {
        CourseQueryService service = new CourseQueryService(courseRepository, courseSpotRepository, userCourseRepository);
        User owner = Mockito.mock(User.class);
        when(owner.getId()).thenReturn(USER_ID);
        Course course = course(owner, LocalDate.of(2026, 8, 10));
        when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.of(course));
        when(userCourseRepository.findByUser_IdAndCourse_Id(USER_ID, COURSE_ID)).thenReturn(Optional.empty());
        when(courseSpotRepository.findByCourse_IdOrderBySequenceAsc(COURSE_ID)).thenReturn(List.of());

        CourseDetailResponse response = service.findDetail(USER_ID, COURSE_ID);

        assertThat(response.title()).isNull();
        assertThat(response.regionName()).isEqualTo("성동구");
    }

    // 본인 코스가 아니면 존재 여부를 숨기기 위해 404(COURSE_NOT_FOUND)로 처리되는지 검증
    @Test
    void findDetailThrowsNotFoundWhenNotOwnedByUser() {
        CourseQueryService service = new CourseQueryService(courseRepository, courseSpotRepository, userCourseRepository);
        User otherOwner = Mockito.mock(User.class);
        when(otherOwner.getId()).thenReturn(999L);
        Course course = course(otherOwner, LocalDate.of(2026, 8, 10));
        when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.of(course));

        assertThatThrownBy(() -> service.findDetail(USER_ID, COURSE_ID))
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getErrorStatus())
                .isEqualTo(CourseErrorStatus.COURSE_NOT_FOUND);
    }

    // 존재하지 않는 코스도 동일하게 404로 처리되는지 검증
    @Test
    void findDetailThrowsNotFoundWhenCourseDoesNotExist() {
        CourseQueryService service = new CourseQueryService(courseRepository, courseSpotRepository, userCourseRepository);
        when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findDetail(USER_ID, COURSE_ID))
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getErrorStatus())
                .isEqualTo(CourseErrorStatus.COURSE_NOT_FOUND);
    }

    // 테스트 코스 생성
    private Course course(User user, LocalDate visitDate) {
        Course course = Course.create(
                user, "성동구", "11", "11200",
                Set.of(Topic.FOOD), visitDate
        );
        ReflectionTestUtils.setField(course, "id", COURSE_ID);
        return course;
    }

    private UserCourse userCourse(String title, LocalDate visitDate) {
        Course course = course(Mockito.mock(User.class), visitDate);
        return UserCourse.create(Mockito.mock(User.class), course, title);
    }
}
