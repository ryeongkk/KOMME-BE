package com.komme.domain.course.service;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import com.komme.common.exception.GeneralException;
import com.komme.domain.course.dto.request.SaveCourseRequest;
import com.komme.domain.course.entity.Course;
import com.komme.domain.course.entity.UserCourse;
import com.komme.domain.course.enums.Topic;
import com.komme.domain.course.exception.CourseErrorStatus;
import com.komme.domain.course.repository.CourseRepository;
import com.komme.domain.course.repository.UserCourseRepository;
import com.komme.domain.user.entity.User;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseSaveServiceTests {

    private static final Long USER_ID = 1L;
    private static final Long COURSE_ID = 10L;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private UserCourseRepository userCourseRepository;

    // 처음 저장하면 새 UserCourse가 생성되는지 검증
    @Test
    void saveCreatesUserCourseWhenNotSavedYet() {
        CourseSaveService service = new CourseSaveService(courseRepository, userCourseRepository);
        User owner = Mockito.mock(User.class);
        when(owner.getId()).thenReturn(USER_ID);
        Course course = course(owner);
        when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.of(course));
        when(userCourseRepository.findByUser_IdAndCourse_Id(USER_ID, COURSE_ID)).thenReturn(Optional.empty());

        service.save(USER_ID, COURSE_ID, new SaveCourseRequest("성수동 데이트 코스"));

        ArgumentCaptor<UserCourse> captor = ArgumentCaptor.forClass(UserCourse.class);
        verify(userCourseRepository).save(captor.capture());
        assertThat(captor.getValue().getTitle()).isEqualTo("성수동 데이트 코스");
        assertThat(captor.getValue().getCourse()).isSameAs(course);
        assertThat(captor.getValue().getUser()).isSameAs(owner);
    }

    // 이미 저장한 코스를 다시 저장하면 새로 만들지 않고 제목만 갱신하는지 검증
    @Test
    void saveRenamesExistingUserCourseWhenAlreadySaved() {
        CourseSaveService service = new CourseSaveService(courseRepository, userCourseRepository);
        User owner = Mockito.mock(User.class);
        when(owner.getId()).thenReturn(USER_ID);
        Course course = course(owner);
        UserCourse existing = UserCourse.create(owner, course, "예전 이름");
        when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.of(course));
        when(userCourseRepository.findByUser_IdAndCourse_Id(USER_ID, COURSE_ID)).thenReturn(Optional.of(existing));

        service.save(USER_ID, COURSE_ID, new SaveCourseRequest("새 이름"));

        assertThat(existing.getTitle()).isEqualTo("새 이름");
        verify(userCourseRepository).save(existing);
    }

    // 생성자 본인이 아니면 존재 여부를 숨기기 위해 404로 처리되고, 아무것도 저장되지 않는지 검증
    @Test
    void saveThrowsNotFoundWhenNotOwnedByUser() {
        CourseSaveService service = new CourseSaveService(courseRepository, userCourseRepository);
        User otherOwner = Mockito.mock(User.class);
        when(otherOwner.getId()).thenReturn(999L);
        Course course = course(otherOwner);
        when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.of(course));

        assertThatThrownBy(() -> service.save(USER_ID, COURSE_ID, new SaveCourseRequest("이름")))
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getErrorStatus())
                .isEqualTo(CourseErrorStatus.COURSE_NOT_FOUND);

        verify(userCourseRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    // 존재하지 않는 코스도 동일하게 404로 처리되는지 검증
    @Test
    void saveThrowsNotFoundWhenCourseDoesNotExist() {
        CourseSaveService service = new CourseSaveService(courseRepository, userCourseRepository);
        when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.save(USER_ID, COURSE_ID, new SaveCourseRequest("이름")))
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getErrorStatus())
                .isEqualTo(CourseErrorStatus.COURSE_NOT_FOUND);
    }

    private Course course(User user) {
        return Course.create(
                user, "성동구", "11", "11200",
                Set.of(Topic.FOOD), LocalDate.of(2026, 8, 10)
        );
    }
}
