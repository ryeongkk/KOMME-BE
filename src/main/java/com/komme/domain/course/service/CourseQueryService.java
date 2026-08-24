package com.komme.domain.course.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.komme.common.exception.GeneralException;
import com.komme.domain.course.dto.response.CourseDetailResponse;
import com.komme.domain.course.dto.response.CourseSummaryResponse;
import com.komme.domain.course.entity.Course;
import com.komme.domain.course.entity.CourseSpot;
import com.komme.domain.course.entity.UserCourse;
import com.komme.domain.course.enums.CourseStatus;
import com.komme.domain.course.exception.CourseErrorStatus;
import com.komme.domain.course.repository.CourseSpotCount;
import com.komme.domain.course.repository.CourseRepository;
import com.komme.domain.course.repository.CourseSpotRepository;
import com.komme.domain.course.repository.UserCourseRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

// 코스 목록/상세 조회 전담 - 생성(CourseGenerationService)과 책임을 분리
@Service
@RequiredArgsConstructor
public class CourseQueryService {

    private final CourseRepository courseRepository;
    private final CourseSpotRepository courseSpotRepository;
    private final UserCourseRepository userCourseRepository;

    // 코스 목록 조회 기능 - 저장(UserCourse)된 코스만 노출된다.
    // UPCOMING/HISTORY는 컬럼이 아니라 course.visitDate와 오늘 날짜를 비교해 그때그때 계산
    @Transactional(readOnly = true)
    public List<CourseSummaryResponse> findList(Long userId, CourseStatus status) {
        LocalDate today = LocalDate.now();
        List<UserCourse> userCourses = status == CourseStatus.UPCOMING
                ? userCourseRepository.findByUser_IdAndCourse_VisitDateGreaterThanEqualOrderByCourse_VisitDateAsc(userId, today)
                : userCourseRepository.findByUser_IdAndCourse_VisitDateLessThanOrderByCourse_VisitDateDesc(userId, today);
        Map<Long, Long> spotCountsByCourseId = spotCountsByCourseId(userCourses);
        return userCourses.stream()
                .map(userCourse -> CourseSummaryResponse.of(
                        userCourse,
                        spotCountsByCourseId.getOrDefault(userCourse.getCourse().getId(), 0L)
                ))
                .toList();
    }

    // 코스 상세 조회 기능 - 생성자 본인이 아니면 존재 자체를 숨기기 위해 없음과 동일하게 404로 처리
    @Transactional(readOnly = true)
    public CourseDetailResponse findDetail(Long userId, Long courseId) {
        Course course = courseRepository.findById(courseId)
                .filter(found -> found.getUser().getId().equals(userId))
                .orElseThrow(() -> new GeneralException(CourseErrorStatus.COURSE_NOT_FOUND));
        List<CourseSpot> courseSpots = courseSpotRepository.findByCourse_IdOrderBySequenceAsc(courseId);
        String title = userCourseRepository.findByUser_IdAndCourse_Id(userId, courseId)
                .map(UserCourse::getTitle)
                .orElse(null);
        return CourseDetailResponse.of(course, title, courseSpots);
    }

    // 코스별 스팟 개수 맵 생성 기능
    private Map<Long, Long> spotCountsByCourseId(List<UserCourse> userCourses) {
        List<Long> courseIds = userCourses.stream()
                .map(userCourse -> userCourse.getCourse().getId())
                .toList();
        if (courseIds.isEmpty()) {
            return Map.of();
        }

        return courseSpotRepository.countByCourseIds(courseIds).stream()
                .collect(Collectors.toMap(
                        CourseSpotCount::courseId,
                        CourseSpotCount::spotCount,
                        (left, right) -> left
                ));
    }
}
