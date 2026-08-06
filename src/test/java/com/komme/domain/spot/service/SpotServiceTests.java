package com.komme.domain.spot.service;

import java.math.BigDecimal;
import java.util.List;

import com.komme.domain.spot.entity.Spot;
import com.komme.domain.spot.enums.TimeSlot;
import com.komme.domain.tourapi.client.KorServiceListClient;
import com.komme.domain.tourapi.client.LocationBasedListItem;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpotServiceTests {

    @Mock
    private KorServiceListClient korServiceListClient;

    @Mock
    private SpotUpsertWriter spotUpsertWriter;

    // 응답이 비어있으면 바로 빈 리스트를 반환하고 upsert를 시도하지 않는지 검증
    @Test
    void findNearbyReturnsEmptyListWithoutUpsertWhenNoResults() {
        SpotService spotService = new SpotService(korServiceListClient, spotUpsertWriter);
        when(korServiceListClient.findLocationBasedList("127.05", "37.54", "3000", "39"))
                .thenReturn(List.of());

        List<Spot> result = spotService.findNearby(new BigDecimal("127.05"), new BigDecimal("37.54"), "39");

        assertThat(result).isEmpty();
        verify(spotUpsertWriter, never()).upsertAll(anyListOfLocationBasedListItem());
    }

    // tourapi 응답을 그대로 SpotUpsertWriter에 위임하고 그 결과를 반환하는지 검증
    @Test
    void findNearbyDelegatesToUpsertWriterAndReturnsItsResult() {
        SpotService spotService = new SpotService(korServiceListClient, spotUpsertWriter);
        LocationBasedListItem item = locationBasedItem("111", "가까운 카페");
        Spot upsertedSpot = existingSpot("111", "가까운 카페");
        when(korServiceListClient.findLocationBasedList("127.05", "37.54", "3000", "39"))
                .thenReturn(List.of(item));
        when(spotUpsertWriter.upsertAll(List.of(item))).thenReturn(List.of(upsertedSpot));

        List<Spot> result = spotService.findNearby(new BigDecimal("127.05"), new BigDecimal("37.54"), "39");

        assertThat(result).containsExactly(upsertedSpot);
    }

    // 반경 미지정 시 기본값 3km(3000m)로 호출되는지 검증
    @Test
    void findNearbyUsesDefaultThreeKilometerRadius() {
        SpotService spotService = new SpotService(korServiceListClient, spotUpsertWriter);
        when(korServiceListClient.findLocationBasedList(eq("127.05"), eq("37.54"), eq("3000"), eq("39")))
                .thenReturn(List.of());

        spotService.findNearby(new BigDecimal("127.05"), new BigDecimal("37.54"), "39");

        verify(korServiceListClient).findLocationBasedList("127.05", "37.54", "3000", "39");
    }

    @SuppressWarnings("unchecked")
    private List<LocationBasedListItem> anyListOfLocationBasedListItem() {
        return org.mockito.ArgumentMatchers.any(List.class);
    }

    private Spot existingSpot(String contentId, String name) {
        return Spot.create(contentId, new Spot.Attributes(
                name, "A05", "A0502", "A05020900", TimeSlot.LUNCH,
                new BigDecimal("37.5443300"), new BigDecimal("127.0557800"),
                "1", "2", null
        ));
    }

    private LocationBasedListItem locationBasedItem(String contentId, String title) {
        return new LocationBasedListItem(
                contentId, "39", title, "서울 성동구",
                "1", "2",
                "A05", "A0502", "A05020900",
                "127.0557800", "37.5443300",
                "https://tong.visitkorea.or.kr/full.jpg", "https://tong.visitkorea.or.kr/thumb.jpg",
                "100"
        );
    }
}
