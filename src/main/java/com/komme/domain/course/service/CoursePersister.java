package com.komme.domain.course.service;

import java.util.ArrayList;
import java.util.List;

import com.komme.domain.course.entity.Course;
import com.komme.domain.course.entity.CourseSpot;
import com.komme.domain.course.repository.CourseRepository;
import com.komme.domain.course.repository.CourseSpotRepository;
import com.komme.domain.spot.entity.Spot;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

// Course/CourseSpot 저장 전담 컴포넌트. 외부 API 호출(CourseGenerationService)과 분리해, 트랜잭션이 DB 쓰기에만 걸리도록 한다.
@Component
@RequiredArgsConstructor
class CoursePersister {

    private final CourseRepository courseRepository;
    private final CourseSpotRepository courseSpotRepository;

    // 코스와 코스 스팟들을 함께 저장 기능 - course는 이미 만들어진(아직 저장 전) 엔티티,
    // orderedSpots/distancesToNext는 이미 동선 정렬이 끝난 상태로 들어온다
    @Transactional
    CourseGenerationResult persist(Course course, List<Spot> orderedSpots, List<Integer> distancesToNext) {
        courseRepository.save(course);

        List<CourseSpot> courseSpots = new ArrayList<>();
        for (int i = 0; i < orderedSpots.size(); i++) {
            Spot spot = orderedSpots.get(i);
            Integer distanceToNext = i < distancesToNext.size() ? distancesToNext.get(i) : null;
            courseSpots.add(CourseSpot.create(course, spot, i + 1, spot.getTimeSlot(), distanceToNext));
        }
        courseSpotRepository.saveAll(courseSpots);

        return new CourseGenerationResult(course, courseSpots);
    }
}
