package com.komme.domain.course.entity;

import com.komme.domain.spot.entity.Spot;
import com.komme.domain.spot.enums.TimeSlot;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

class CourseSpotTests {

    // 코스 스팟 엔티티 생성 시 필드가 그대로 저장되는지 검증
    @Test
    void createStoresAllFields() {
        Course course = Mockito.mock(Course.class);
        Spot spot = Mockito.mock(Spot.class);

        CourseSpot courseSpot = CourseSpot.create(course, spot, 1, TimeSlot.MORNING, 350);

        assertThat(courseSpot.getCourse()).isSameAs(course);
        assertThat(courseSpot.getSpot()).isSameAs(spot);
        assertThat(courseSpot.getSequence()).isEqualTo(1);
        assertThat(courseSpot.getTimeSlot()).isEqualTo(TimeSlot.MORNING);
        assertThat(courseSpot.getDistanceToNextMeters()).isEqualTo(350);
    }

    // 코스의 마지막 스팟은 다음 스팟까지의 거리가 null일 수 있는지 검증
    @Test
    void createAllowsNullDistanceToNextMetersForLastSpot() {
        CourseSpot lastCourseSpot = CourseSpot.create(
                Mockito.mock(Course.class), Mockito.mock(Spot.class), 3, TimeSlot.EVENING, null
        );

        assertThat(lastCourseSpot.getDistanceToNextMeters()).isNull();
    }
}
