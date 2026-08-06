package com.komme.domain.course.service;

import java.time.LocalDate;
import java.util.List;

import com.komme.domain.course.dto.response.CourseSummaryResponse;
import com.komme.domain.course.entity.Course;
import com.komme.domain.course.enums.CourseStatus;
import com.komme.domain.course.repository.CourseRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

// 코스 목록/상세 조회 전담 - 생성(CourseGenerationService)과 책임을 분리
@Service
@RequiredArgsConstructor
public class CourseQueryService {

    private final CourseRepository courseRepository;

    // 코스 목록 조회 기능 - UPCOMING/HISTORY는 컬럼이 아니라 visitDate와 오늘 날짜를 비교해 그때그때 계산
    @Transactional(readOnly = true)
    public List<CourseSummaryResponse> findList(Long userId, CourseStatus status) {
        LocalDate today = LocalDate.now();
        List<Course> courses = status == CourseStatus.UPCOMING
                ? courseRepository.findByUser_IdAndVisitDateGreaterThanEqualOrderByVisitDateAsc(userId, today)
                : courseRepository.findByUser_IdAndVisitDateLessThanOrderByVisitDateDesc(userId, today);
        return courses.stream().map(CourseSummaryResponse::of).toList();
    }
}
