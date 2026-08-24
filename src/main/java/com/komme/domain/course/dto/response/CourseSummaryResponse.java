package com.komme.domain.course.dto.response;

import java.time.LocalDate;
import java.util.Set;

import com.komme.domain.course.entity.UserCourse;
import com.komme.domain.course.enums.Topic;

public record CourseSummaryResponse(
        Long courseId,
        String title,
        String regionName,
        LocalDate visitDate,
        Set<Topic> topics,
        long spotCount
) {

    // 스팟 개수를 포함한 코스 목록 아이템 응답 생성 기능
    public static CourseSummaryResponse of(UserCourse userCourse, long spotCount) {
        return new CourseSummaryResponse(
                userCourse.getCourse().getId(),
                userCourse.getTitle(),
                userCourse.getCourse().getRegionName(),
                userCourse.getCourse().getVisitDate(),
                userCourse.getCourse().getTopics(),
                spotCount
        );
    }
}
