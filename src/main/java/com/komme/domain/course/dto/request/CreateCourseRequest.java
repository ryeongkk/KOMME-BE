package com.komme.domain.course.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import com.komme.domain.course.enums.SpotCount;
import com.komme.domain.course.enums.Topic;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record CreateCourseRequest(
        @NotNull(message = "경도는 필수입니다.")
        BigDecimal longitude,

        @NotNull(message = "위도는 필수입니다.")
        BigDecimal latitude,

        @NotEmpty(message = "주제는 최소 1개 이상 선택해야 합니다.")
        Set<Topic> topics,

        @NotNull(message = "방문 장소 개수는 필수입니다.")
        SpotCount spotCount,

        @NotNull(message = "방문 날짜는 필수입니다.")
        LocalDate visitDate
) {
}
