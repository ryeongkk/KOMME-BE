package com.komme.domain.course.service;

import com.komme.common.exception.GeneralException;
import com.komme.domain.course.entity.Course;
import com.komme.domain.course.exception.CourseErrorStatus;
import com.komme.domain.course.repository.CourseRepository;
import com.komme.domain.course.repository.CourseSpotRepository;
import com.komme.domain.course.repository.UserCourseRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

// 코스 삭제 전담 - 하드 삭제. 생성자 본인이 아니면 존재 자체를 숨기기 위해 없음과 동일하게 404로 처리
@Service
@RequiredArgsConstructor
public class CourseDeletionService {

    private final CourseRepository courseRepository;
    private final CourseSpotRepository courseSpotRepository;
    private final UserCourseRepository userCourseRepository;

    // 코스 삭제 기능 - course_spot/user_course가 course를 FK로 참조하므로 둘 다 먼저 지운다
    @Transactional
    public void delete(Long userId, Long courseId) {
        Course course = courseRepository.findById(courseId)
                .filter(found -> found.getUser().getId().equals(userId))
                .orElseThrow(() -> new GeneralException(CourseErrorStatus.COURSE_NOT_FOUND));
        courseSpotRepository.deleteByCourse_Id(courseId);
        userCourseRepository.deleteByCourse_Id(courseId);
        courseRepository.delete(course);
    }
}
