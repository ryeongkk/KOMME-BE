package com.komme.domain.course.service;

import com.komme.common.exception.GeneralException;
import com.komme.domain.course.dto.request.SaveCourseRequest;
import com.komme.domain.course.entity.Course;
import com.komme.domain.course.entity.UserCourse;
import com.komme.domain.course.exception.CourseErrorStatus;
import com.komme.domain.course.repository.CourseRepository;
import com.komme.domain.course.repository.UserCourseRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

// 코스 저장 전담 - 생성된 코스에 사용자가 입력한 이름을 붙여 "내 코스 목록"에 노출시킨다
@Service
@RequiredArgsConstructor
public class CourseSaveService {

    private final CourseRepository courseRepository;
    private final UserCourseRepository userCourseRepository;

    // 코스 저장 기능 - 생성자 본인이 아니면 존재 자체를 숨기기 위해 없음과 동일하게 404로 처리.
    // 이미 저장한 코스를 다시 저장하면(멱등) 제목만 갱신한다
    @Transactional
    public void save(Long userId, Long courseId, SaveCourseRequest request) {
        Course course = courseRepository.findById(courseId)
                .filter(found -> found.getUser().getId().equals(userId))
                .orElseThrow(() -> new GeneralException(CourseErrorStatus.COURSE_NOT_FOUND));

        UserCourse userCourse = userCourseRepository.findByUser_IdAndCourse_Id(userId, courseId)
                .map(existing -> renamed(existing, request.title()))
                .orElseGet(() -> UserCourse.create(course.getUser(), course, request.title()));

        userCourseRepository.save(userCourse);
    }

    private UserCourse renamed(UserCourse userCourse, String title) {
        userCourse.changeTitle(title);
        return userCourse;
    }
}
