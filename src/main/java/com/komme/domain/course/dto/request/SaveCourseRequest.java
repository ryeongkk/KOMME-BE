package com.komme.domain.course.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SaveCourseRequest(
        @NotBlank(message = "코스명은 필수입니다.")
        @Size(max = 30, message = "코스명은 30자를 넘을 수 없습니다.")
        String title
) {
}
