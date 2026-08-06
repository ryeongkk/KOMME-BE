package com.komme.domain.course.service;

import java.time.LocalDate;
import java.util.List;

import com.komme.common.exception.GeneralException;
import com.komme.domain.course.dto.response.CourseDetailResponse;
import com.komme.domain.course.dto.response.CourseSummaryResponse;
import com.komme.domain.course.entity.Course;
import com.komme.domain.course.entity.CourseSpot;
import com.komme.domain.course.enums.CourseStatus;
import com.komme.domain.course.exception.CourseErrorStatus;
import com.komme.domain.course.repository.CourseRepository;
import com.komme.domain.course.repository.CourseSpotRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

// 코스 목록/상세 조회 전담 - 생성(CourseGenerationService)과 책임을 분리
@Service
@RequiredArgsConstructor
public class CourseQueryService {

    private final CourseRepository courseRepository;
    private final CourseSpotRepository courseSpotRepository;

    // 코스 목록 조회 기능 - UPCOMING/HISTORY는 컬럼이 아니라 visitDate와 오늘 날짜를 비교해 그때그때 계산
    @Transactional(readOnly = true)
    public List<CourseSummaryResponse> findList(Long userId, CourseStatus status) {
        LocalDate today = LocalDate.now();
        List<Course> courses = status == CourseStatus.UPCOMING
                ? courseRepository.findByUser_IdAndVisitDateGreaterThanEqualOrderByVisitDateAsc(userId, today)
                : courseRepository.findByUser_IdAndVisitDateLessThanOrderByVisitDateDesc(userId, today);
        return courses.stream().map(CourseSummaryResponse::of).toList();
    }

    // 코스 상세 조회 기능 - 본인 코스가 아니면 존재 자체를 숨기기 위해 없음과 동일하게 404로 처리
    @Transactional(readOnly = true)
    public CourseDetailResponse findDetail(Long userId, Long courseId) {
        Course course = courseRepository.findById(courseId)
                .filter(found -> found.getUser().getId().equals(userId))
                .orElseThrow(() -> new GeneralException(CourseErrorStatus.COURSE_NOT_FOUND));
        List<CourseSpot> courseSpots = courseSpotRepository.findByCourse_IdOrderBySequenceAsc(courseId);
        return CourseDetailResponse.of(course, courseSpots);
    }
}
