package com.komme.domain.course.repository;

import java.util.List;

import com.komme.domain.course.entity.CourseSpot;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseSpotRepository extends JpaRepository<CourseSpot, Long> {

    // 코스 상세 조회용 - 방문 순서(sequence)대로 정렬해서 조회
    List<CourseSpot> findByCourse_IdOrderBySequenceAsc(Long courseId);

    // 코스 삭제 시 하위 코스 스팟 일괄 삭제 기능 - course_spot.course_id FK 제약 때문에 Course보다 먼저 지워야 한다
    void deleteByCourse_Id(Long courseId);
}
