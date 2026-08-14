package com.komme.domain.course.dto.request;

import jakarta.validation.constraints.NotBlank;

public record SaveCourseRequest(
        @NotBlank(message = "코스명은 필수입니다.")
        String title
) {
}
