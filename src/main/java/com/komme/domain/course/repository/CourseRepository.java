package com.komme.domain.course.repository;

import java.time.LocalDate;
import java.util.List;

import com.komme.domain.course.entity.Course;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {

    // Upcoming 목록 조회 기능 - visitDate가 기준일 이상, D-day 임박순(오름차순)
    List<Course> findByUser_IdAndVisitDateGreaterThanEqualOrderByVisitDateAsc(Long userId, LocalDate fromDate);

    // History 목록 조회 기능 - visitDate가 기준일 미만, 최근 완료순(내림차순)
    List<Course> findByUser_IdAndVisitDateLessThanOrderByVisitDateDesc(Long userId, LocalDate beforeDate);
}
