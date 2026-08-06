package com.komme.domain.course.enums;

// 코스 목록 조회 시 필터/정렬 방향을 결정하는 값 - DB 컬럼이 아니라 visitDate 기준으로 조회 시점에 계산한다
public enum CourseStatus {
    UPCOMING,
    HISTORY
}
