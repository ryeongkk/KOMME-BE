package com.komme.domain.course.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import com.komme.common.exception.GeneralException;
import com.komme.domain.course.entity.Course;
import com.komme.domain.course.enums.Duration;
import com.komme.domain.course.enums.Topic;
import com.komme.domain.course.exception.CourseErrorStatus;
import com.komme.domain.spot.entity.Spot;
import com.komme.domain.spot.enums.TimeSlot;
import com.komme.domain.spot.service.SpotService;
import com.komme.domain.user.entity.User;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseGenerationServiceTests {

    private static final BigDecimal LONGITUDE = new BigDecimal("127.0557800");
    private static final BigDecimal LATITUDE = new BigDecimal("37.5443300");
    private static final LocalDate VISIT_DATE = LocalDate.of(2026, 8, 10);

    @Mock
    private SpotService spotService;

    @Mock
    private CoursePersister coursePersister;

    // 첫 반경(3km)에서 필요한 개수만큼 모이면, 주제에 안 맞는 스팟은 걸러지고 성동구로 지역명이 해석되어 저장되는지 검증
    @Test
    void generateFiltersByTopicAndPersistsWithResolvedRegionName() {
        CourseGenerationService service = new CourseGenerationService(spotService, coursePersister);
        stubEmptyExcept(3000, "39", List.of(
                foodSpot("1"), foodSpot("2"), foodSpot("3"), foodSpot("4"), shoppingSpot("5")
        ));
        User user = Mockito.mock(User.class);
        Course expectedCourse = Mockito.mock(Course.class);
        when(coursePersister.persist(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(expectedCourse);

        Course result = service.generate(user, LONGITUDE, LATITUDE, Set.of(Topic.FOOD), Duration.HALF_DAY, VISIT_DATE);

        assertThat(result).isSameAs(expectedCourse);

        ArgumentCaptor<String> regionNameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<List<Spot>> orderedSpotsCaptor = ArgumentCaptor.forClass(List.class);
        verify(coursePersister).persist(
                any(), any(), regionNameCaptor.capture(), any(), any(), any(), any(), orderedSpotsCaptor.capture(), any()
        );
        assertThat(regionNameCaptor.getValue()).isEqualTo("성동구");
        // 쇼핑(A04) 스팟은 주제(FOOD)에 안 맞아 제외되고, 요청한 4개만 남아야 한다
        assertThat(orderedSpotsCaptor.getValue()).hasSize(4);
    }

    // 첫 반경에서 부족하면 다음 반경(6km)으로 자동 확대해서 재시도하는지 검증
    @Test
    void generateExpandsRadiusWhenFirstRadiusHasTooFewCandidates() {
        CourseGenerationService service = new CourseGenerationService(spotService, coursePersister);
        stubEmptyExcept(3000, "39", List.of(foodSpot("1"), foodSpot("2"))); // 4개 필요한데 2개뿐
        stubEmptyExcept(6000, "39", List.of(foodSpot("1"), foodSpot("2"), foodSpot("3"), foodSpot("4")));
        User user = Mockito.mock(User.class);
        Course expectedCourse = Mockito.mock(Course.class);
        when(coursePersister.persist(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(expectedCourse);

        Course result = service.generate(user, LONGITUDE, LATITUDE, Set.of(Topic.FOOD), Duration.HALF_DAY, VISIT_DATE);

        assertThat(result).isSameAs(expectedCourse);
        verify(spotService).findNearby(LONGITUDE, LATITUDE, 6000, "39");
    }

    // 9km까지 넓혀도 부족하면 실패 처리되고, 저장은 시도조차 안 하는지 검증
    @Test
    void generateThrowsWhenStillInsufficientAfterMaxRadius() {
        CourseGenerationService service = new CourseGenerationService(spotService, coursePersister);
        stubEmptyExcept(3000, "39", List.of(foodSpot("1")));
        stubEmptyExcept(6000, "39", List.of(foodSpot("1")));
        stubEmptyExcept(9000, "39", List.of(foodSpot("1")));
        User user = Mockito.mock(User.class);

        assertThatThrownBy(() -> service.generate(user, LONGITUDE, LATITUDE, Set.of(Topic.FOOD), Duration.HALF_DAY, VISIT_DATE))
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getErrorStatus())
                .isEqualTo(CourseErrorStatus.INSUFFICIENT_SPOTS);

        verify(coursePersister, never()).persist(any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    // 주어진 반경에서 특정 contentTypeId만 결과를 주고 나머지는 빈 목록을 주도록 스텁
    private void stubEmptyExcept(int radiusMeters, String contentTypeIdWithResults, List<Spot> results) {
        for (String contentTypeId : List.of("12", "14", "28", "39")) {
            if (contentTypeId.equals(contentTypeIdWithResults)) {
                when(spotService.findNearby(LONGITUDE, LATITUDE, radiusMeters, contentTypeId)).thenReturn(results);
            } else {
                when(spotService.findNearby(LONGITUDE, LATITUDE, radiusMeters, contentTypeId)).thenReturn(List.of());
            }
        }
    }

    private Spot foodSpot(String contentId) {
        return Spot.create(contentId, new Spot.Attributes(
                "음식점" + contentId, "A05", "A0502", "A05020900", TimeSlot.LUNCH,
                LATITUDE, LONGITUDE, "11", "11200", null
        ));
    }

    private Spot shoppingSpot(String contentId) {
        return Spot.create(contentId, new Spot.Attributes(
                "쇼핑몰" + contentId, "A04", "A0401", "A04010100", TimeSlot.ANYTIME,
                LATITUDE, LONGITUDE, "11", "11200", null
        ));
    }
}
