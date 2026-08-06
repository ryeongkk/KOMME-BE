package com.komme.domain.course.service;

import com.komme.common.exception.GeneralException;
import com.komme.domain.course.entity.Course;
import com.komme.domain.course.exception.CourseErrorStatus;
import com.komme.domain.course.repository.CourseRepository;
import com.komme.domain.course.repository.CourseSpotRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

// 코스 삭제 전담 - 하드 삭제. 본인 코스가 아니면 존재 자체를 숨기기 위해 없음과 동일하게 404로 처리
@Service
@RequiredArgsConstructor
public class CourseDeletionService {

    private final CourseRepository courseRepository;
    private final CourseSpotRepository courseSpotRepository;

    // 코스 삭제 기능 - course_spot이 course를 FK로 참조하므로 course_spot을 먼저 지운다
    @Transactional
    public void delete(Long userId, Long courseId) {
        Course course = courseRepository.findById(courseId)
                .filter(found -> found.getUser().getId().equals(userId))
                .orElseThrow(() -> new GeneralException(CourseErrorStatus.COURSE_NOT_FOUND));
        courseSpotRepository.deleteByCourse_Id(courseId);
        courseRepository.delete(course);
    }
}
