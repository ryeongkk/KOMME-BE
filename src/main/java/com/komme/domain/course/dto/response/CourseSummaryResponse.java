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
        Set<Topic> topics
) {

    // 코스 목록 아이템 응답 생성 기능 - 목록은 저장된 코스(UserCourse) 기준이라 제목은 사용자가 저장 시 입력한 값이다
    public static CourseSummaryResponse of(UserCourse userCourse) {
        return new CourseSummaryResponse(
                userCourse.getCourse().getId(),
                userCourse.getTitle(),
                userCourse.getCourse().getRegionName(),
                userCourse.getCourse().getVisitDate(),
                userCourse.getCourse().getTopics()
        );
    }
}
