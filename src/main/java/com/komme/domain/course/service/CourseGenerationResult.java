package com.komme.domain.course.service;

import java.util.List;

import com.komme.domain.course.entity.Course;
import com.komme.domain.course.entity.CourseSpot;

// 코스 생성 결과 - 컨트롤러가 응답 DTO를 만들 때 재조회 없이 바로 쓸 수 있도록 생성된 CourseSpot 목록까지 함께 담는다.
public record CourseGenerationResult(Course course, List<CourseSpot> courseSpots) {
}
