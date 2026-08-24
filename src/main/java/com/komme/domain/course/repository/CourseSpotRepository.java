package com.komme.domain.course.repository;

import java.util.Collection;
import java.util.List;

import com.komme.domain.course.entity.CourseSpot;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourseSpotRepository extends JpaRepository<CourseSpot, Long> {

    // 코스 상세 조회용 - 방문 순서(sequence)대로 정렬해서 조회
    List<CourseSpot> findByCourse_IdOrderBySequenceAsc(Long courseId);

    // 코스별 스팟 개수 일괄 조회 기능
    @Query("""
            select new com.komme.domain.course.repository.CourseSpotCount(cs.course.id, count(cs.id))
            from CourseSpot cs
            where cs.course.id in :courseIds
            group by cs.course.id
            """)
    List<CourseSpotCount> countByCourseIds(@Param("courseIds") Collection<Long> courseIds);

    // 코스 삭제 시 하위 코스 스팟 일괄 삭제 기능 - course_spot.course_id FK 제약 때문에 Course보다 먼저 지워야 한다
    void deleteByCourse_Id(Long courseId);
}
