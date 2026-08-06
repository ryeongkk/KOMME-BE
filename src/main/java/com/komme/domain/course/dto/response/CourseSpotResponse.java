package com.komme.domain.course.dto.response;

import java.math.BigDecimal;

import com.komme.domain.course.entity.CourseSpot;
import com.komme.domain.spot.enums.TimeSlot;

public record CourseSpotResponse(
        Long spotId,
        String name,
        int sequence,
        TimeSlot timeSlot,
        BigDecimal latitude,
        BigDecimal longitude,
        String thumbnailUrl,
        Integer distanceToNextMeters
) {

    // 코스 스팟 응답 생성 기능
    public static CourseSpotResponse of(CourseSpot courseSpot) {
        return new CourseSpotResponse(
                courseSpot.getSpot().getId(),
                courseSpot.getSpot().getName(),
                courseSpot.getSequence(),
                courseSpot.getTimeSlot(),
                courseSpot.getSpot().getLatitude(),
                courseSpot.getSpot().getLongitude(),
                courseSpot.getSpot().getThumbnailUrl(),
                courseSpot.getDistanceToNextMeters()
        );
    }
}
