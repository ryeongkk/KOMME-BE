package com.komme.domain.course.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import com.komme.domain.course.entity.Course;
import com.komme.domain.course.entity.CourseSpot;
import com.komme.domain.course.enums.Topic;
import com.komme.domain.course.repository.CourseRepository;
import com.komme.domain.course.repository.CourseSpotRepository;
import com.komme.domain.spot.entity.Spot;
import com.komme.domain.spot.enums.TimeSlot;
import com.komme.domain.user.entity.User;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CoursePersisterTests {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private CourseSpotRepository courseSpotRepository;

    // 코스와 코스 스팟이 함께 저장되고, 마지막 스팟은 distanceToNext가 null인지 검증
    @Test
    void persistSavesCourseAndCourseSpotsWithSequenceAndDistances() {
        CoursePersister coursePersister = new CoursePersister(courseRepository, courseSpotRepository);
        Spot first = spot("1", TimeSlot.MORNING);
        Spot second = spot("2", TimeSlot.LUNCH);
        Course course = Course.create(
                Mockito.mock(User.class),
                "성수동 음식 Day",
                "성수동", "1", "2",
                Set.of(Topic.FOOD),
                LocalDate.of(2026, 8, 10)
        );

        CourseGenerationResult result = coursePersister.persist(course, List.of(first, second), List.of(500));

        assertThat(result.course()).isSameAs(course);
        verify(courseRepository).save(course);

        ArgumentCaptor<List<CourseSpot>> captor = ArgumentCaptor.forClass(List.class);
        verify(courseSpotRepository).saveAll(captor.capture());
        List<CourseSpot> savedCourseSpots = captor.getValue();

        assertThat(result.courseSpots()).isEqualTo(savedCourseSpots);
        assertThat(savedCourseSpots).hasSize(2);
        assertThat(savedCourseSpots.get(0).getSequence()).isEqualTo(1);
        assertThat(savedCourseSpots.get(0).getDistanceToNextMeters()).isEqualTo(500);
        assertThat(savedCourseSpots.get(1).getSequence()).isEqualTo(2);
        assertThat(savedCourseSpots.get(1).getDistanceToNextMeters()).isNull();
    }

    private Spot spot(String contentId, TimeSlot timeSlot) {
        return Spot.create(contentId, new Spot.Attributes(
                "spot-" + contentId, "A05", "A0502", "A05020900", timeSlot,
                new BigDecimal("37.5443300"), new BigDecimal("127.0557800"),
                "1", "2", null
        ));
    }
}
