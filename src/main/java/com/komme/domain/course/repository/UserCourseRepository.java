package com.komme.domain.course.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.komme.domain.course.entity.UserCourse;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserCourseRepository extends JpaRepository<UserCourse, Long> {

    // 특정 사용자가 특정 코스를 이미 저장했는지 조회 기능 - 저장 요청의 멱등 처리(재저장 시 제목만 갱신)에 사용
    Optional<UserCourse> findByUser_IdAndCourse_Id(Long userId, Long courseId);

    // Upcoming 목록 조회 기능 - course.visitDate가 기준일 이상, D-day 임박순(오름차순)
    List<UserCourse> findByUser_IdAndCourse_VisitDateGreaterThanEqualOrderByCourse_VisitDateAsc(
            Long userId, LocalDate fromDate
    );

    // History 목록 조회 기능 - course.visitDate가 기준일 미만, 최근 완료순(내림차순)
    List<UserCourse> findByUser_IdAndCourse_VisitDateLessThanOrderByCourse_VisitDateDesc(
            Long userId, LocalDate beforeDate
    );

    // 코스 삭제 시 하위 저장 기록 일괄 삭제 기능 - user_course.course_id FK 제약 때문에 Course보다 먼저 지워야 한다
    void deleteByCourse_Id(Long courseId);
}
