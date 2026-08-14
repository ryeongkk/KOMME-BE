package com.komme.domain.course.service;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import com.komme.common.exception.GeneralException;
import com.komme.domain.course.entity.Course;
import com.komme.domain.course.enums.Topic;
import com.komme.domain.course.exception.CourseErrorStatus;
import com.komme.domain.course.repository.CourseRepository;
import com.komme.domain.course.repository.CourseSpotRepository;
import com.komme.domain.user.entity.User;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseDeletionServiceTests {

    private static final Long USER_ID = 1L;
    private static final Long COURSE_ID = 10L;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private CourseSpotRepository courseSpotRepository;

    // 본인 코스면 코스 스팟을 먼저 지운 뒤 코스를 삭제하는지 검증
    @Test
    void deleteRemovesCourseSpotsBeforeCourseWhenOwnedByUser() {
        CourseDeletionService service = new CourseDeletionService(courseRepository, courseSpotRepository);
        User owner = Mockito.mock(User.class);
        when(owner.getId()).thenReturn(USER_ID);
        Course course = course(owner);
        when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.of(course));

        service.delete(USER_ID, COURSE_ID);

        verify(courseSpotRepository).deleteByCourse_Id(COURSE_ID);
        verify(courseRepository).delete(course);
    }

    // 본인 코스가 아니면 존재 여부를 숨기기 위해 404로 처리되고, 아무것도 삭제되지 않는지 검증
    @Test
    void deleteThrowsNotFoundWhenNotOwnedByUserAndDeletesNothing() {
        CourseDeletionService service = new CourseDeletionService(courseRepository, courseSpotRepository);
        User otherOwner = Mockito.mock(User.class);
        when(otherOwner.getId()).thenReturn(999L);
        Course course = course(otherOwner);
        when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.of(course));

        assertThatThrownBy(() -> service.delete(USER_ID, COURSE_ID))
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getErrorStatus())
                .isEqualTo(CourseErrorStatus.COURSE_NOT_FOUND);

        verify(courseSpotRepository, never()).deleteByCourse_Id(COURSE_ID);
        verify(courseRepository, never()).delete(course);
    }

    // 존재하지 않는 코스도 동일하게 404로 처리되는지 검증
    @Test
    void deleteThrowsNotFoundWhenCourseDoesNotExist() {
        CourseDeletionService service = new CourseDeletionService(courseRepository, courseSpotRepository);
        when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(USER_ID, COURSE_ID))
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getErrorStatus())
                .isEqualTo(CourseErrorStatus.COURSE_NOT_FOUND);
    }

    private Course course(User user) {
        return Course.create(
                user, "성동구 음식 Day", "성동구", "11", "11200",
                Set.of(Topic.FOOD), LocalDate.of(2026, 8, 10)
        );
    }
}
