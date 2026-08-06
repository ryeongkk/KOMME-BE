package com.komme.domain.course.dto.response;

import java.time.LocalDate;
import java.util.Set;

import com.komme.domain.course.entity.Course;
import com.komme.domain.course.enums.Topic;

public record CourseSummaryResponse(
        Long courseId,
        String title,
        String regionName,
        LocalDate visitDate,
        Set<Topic> topics
) {

    // 코스 목록 아이템 응답 생성 기능
    public static CourseSummaryResponse of(Course course) {
        return new CourseSummaryResponse(
                course.getId(),
                course.getTitle(),
                course.getRegionName(),
                course.getVisitDate(),
                course.getTopics()
        );
    }
}
