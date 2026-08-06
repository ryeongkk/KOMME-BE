package com.komme.domain.course.service;

import java.math.BigDecimal;
import java.util.List;

import com.komme.domain.spot.entity.Spot;
import com.komme.domain.spot.enums.TimeSlot;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SpotRouteSequencerTests {

    private static final BigDecimal START_LONGITUDE = new BigDecimal("127.000000");
    private static final BigDecimal START_LATITUDE = new BigDecimal("37.000000");

    // 시작 좌표에서 가까운 순서대로 정렬되는지 검증 (near -> medium -> far)
    @Test
    void sequenceFromOrdersSpotsByNearestNeighbor() {
        Spot near = spotAt("near", "127.001000", "37.001000");
        Spot medium = spotAt("medium", "127.010000", "37.010000");
        Spot far = spotAt("far", "127.050000", "37.050000");

        List<Spot> ordered = SpotRouteSequencer.sequenceFrom(
                START_LONGITUDE, START_LATITUDE, List.of(far, near, medium)
        );

        assertThat(ordered).extracting(Spot::getContentId).containsExactly("near", "medium", "far");
    }

    // 연속 구간 거리 목록의 크기가 (스팟 개수 - 1)이고 전부 양수인지 검증
    @Test
    void distancesBetweenConsecutiveReturnsOneFewerThanSpotCount() {
        List<Spot> ordered = List.of(
                spotAt("1", "127.000000", "37.000000"),
                spotAt("2", "127.001000", "37.001000"),
                spotAt("3", "127.010000", "37.010000")
        );

        List<Integer> distances = SpotRouteSequencer.distancesBetweenConsecutive(ordered);

        assertThat(distances).hasSize(2);
        assertThat(distances).allSatisfy(distance -> assertThat(distance).isPositive());
    }

    // 스팟이 1개면 순서는 그대로, 구간 거리는 없는지 검증
    @Test
    void singleSpotHasNoDistances() {
        List<Spot> single = List.of(spotAt("only", "127.001000", "37.001000"));

        List<Spot> ordered = SpotRouteSequencer.sequenceFrom(START_LONGITUDE, START_LATITUDE, single);
        List<Integer> distances = SpotRouteSequencer.distancesBetweenConsecutive(ordered);

        assertThat(ordered).extracting(Spot::getContentId).containsExactly("only");
        assertThat(distances).isEmpty();
    }

    // 빈 목록이면 순서/거리 모두 비어있는지 검증
    @Test
    void emptySpotListReturnsEmptyResults() {
        List<Spot> ordered = SpotRouteSequencer.sequenceFrom(START_LONGITUDE, START_LATITUDE, List.of());

        assertThat(ordered).isEmpty();
        assertThat(SpotRouteSequencer.distancesBetweenConsecutive(ordered)).isEmpty();
    }

    private Spot spotAt(String contentId, String longitude, String latitude) {
        return Spot.create(contentId, new Spot.Attributes(
                "spot-" + contentId, "A05", "A0502", "A05020900", TimeSlot.LUNCH,
                new BigDecimal(latitude), new BigDecimal(longitude),
                "1", "2", null
        ));
    }
}
