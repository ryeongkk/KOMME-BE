package com.komme.domain.course.dto.response;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import com.komme.domain.course.entity.Course;
import com.komme.domain.course.entity.CourseSpot;
import com.komme.domain.course.enums.Topic;
import com.komme.domain.course.service.CourseGenerationResult;

public record CourseDetailResponse(
        Long courseId,
        String title,
        String regionName,
        Set<Topic> topics,
        LocalDate visitDate,
        List<CourseSpotResponse> spots
) {

    // 코스 상세 응답 생성 기능
    public static CourseDetailResponse of(Course course, List<CourseSpot> courseSpots) {
        return of(course, null, courseSpots);
    }

    // 저장된 코스 제목을 포함한 코스 상세 응답 생성 기능
    public static CourseDetailResponse of(Course course, String title, List<CourseSpot> courseSpots) {
        return new CourseDetailResponse(
                course.getId(),
                title,
                course.getRegionName(),
                course.getTopics(),
                course.getVisitDate(),
                courseSpots.stream().map(CourseSpotResponse::of).toList()
        );
    }

    // 코스 생성 결과로부터 응답 생성 기능
    public static CourseDetailResponse from(CourseGenerationResult result) {
        return of(result.course(), result.courseSpots());
    }
}
