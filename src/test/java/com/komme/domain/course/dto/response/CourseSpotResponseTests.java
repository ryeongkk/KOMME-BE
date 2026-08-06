package com.komme.domain.course.dto.response;

import java.math.BigDecimal;

import com.komme.domain.course.entity.Course;
import com.komme.domain.course.entity.CourseSpot;
import com.komme.domain.spot.entity.Spot;
import com.komme.domain.spot.enums.TimeSlot;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

class CourseSpotResponseTests {

    // CourseSpot과 그 안의 Spot 필드가 응답으로 그대로 매핑되는지 검증
    @Test
    void ofMapsCourseSpotAndUnderlyingSpotFields() {
        Spot spot = Mockito.mock(Spot.class);
        Mockito.when(spot.getId()).thenReturn(10L);
        Mockito.when(spot.getName()).thenReturn("성수동 카페");
        Mockito.when(spot.getLatitude()).thenReturn(new BigDecimal("37.5443300"));
        Mockito.when(spot.getLongitude()).thenReturn(new BigDecimal("127.0557800"));
        Mockito.when(spot.getThumbnailUrl()).thenReturn("https://tong.visitkorea.or.kr/thumb.jpg");
        CourseSpot courseSpot = CourseSpot.create(Mockito.mock(Course.class), spot, 1, TimeSlot.MORNING, 500);

        CourseSpotResponse response = CourseSpotResponse.of(courseSpot);

        assertThat(response.spotId()).isEqualTo(10L);
        assertThat(response.name()).isEqualTo("성수동 카페");
        assertThat(response.sequence()).isEqualTo(1);
        assertThat(response.timeSlot()).isEqualTo(TimeSlot.MORNING);
        assertThat(response.latitude()).isEqualByComparingTo("37.5443300");
        assertThat(response.longitude()).isEqualByComparingTo("127.0557800");
        assertThat(response.thumbnailUrl()).isEqualTo("https://tong.visitkorea.or.kr/thumb.jpg");
        assertThat(response.distanceToNextMeters()).isEqualTo(500);
    }

    // 마지막 스팟은 distanceToNextMeters가 null로 그대로 매핑되는지 검증
    @Test
    void ofAllowsNullDistanceToNextMetersForLastSpot() {
        CourseSpot courseSpot = CourseSpot.create(
                Mockito.mock(Course.class), Mockito.mock(Spot.class), 3, TimeSlot.EVENING, null
        );

        CourseSpotResponse response = CourseSpotResponse.of(courseSpot);

        assertThat(response.distanceToNextMeters()).isNull();
    }
}
