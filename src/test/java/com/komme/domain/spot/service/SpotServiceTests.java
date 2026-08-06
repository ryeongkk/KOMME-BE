package com.komme.domain.spot.service;

import java.math.BigDecimal;
import java.util.List;

import com.komme.domain.spot.entity.Spot;
import com.komme.domain.spot.enums.TimeSlot;
import com.komme.domain.tourapi.client.KorServiceListClient;
import com.komme.domain.tourapi.client.LocationBasedListItem;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpotServiceTests {

    @Mock
    private KorServiceListClient korServiceListClient;

    @Mock
    private com.komme.domain.spot.repository.SpotRepository spotRepository;

    // 응답이 비어있으면 바로 빈 리스트를 반환하고 repository를 조회하지 않는지 검증
    @Test
    void findNearbyReturnsEmptyListWithoutTouchingRepositoryWhenNoResults() {
        SpotService spotService = new SpotService(korServiceListClient, spotRepository);
        when(korServiceListClient.findLocationBasedList("127.05", "37.54", "3000", "39"))
                .thenReturn(List.of());

        List<Spot> result = spotService.findNearby(new BigDecimal("127.05"), new BigDecimal("37.54"), "39");

        assertThat(result).isEmpty();
        verify(spotRepository, never()).findAllByContentIdIn(anyCollection());
    }

    // 신규 스팟은 upsert로 생성되어 saveAll에 전달되는지, 응답 순서가 tourapi 응답 순서(거리순) 그대로인지 검증
    @Test
    void findNearbyCreatesNewSpotsInResponseOrder() {
        SpotService spotService = new SpotService(korServiceListClient, spotRepository);
        LocationBasedListItem near = locationBasedItem("111", "가까운 카페", "100");
        LocationBasedListItem far = locationBasedItem("222", "먼 카페", "900");
        when(korServiceListClient.findLocationBasedList("127.05", "37.54", "3000", "39"))
                .thenReturn(List.of(near, far));
        when(spotRepository.findAllByContentIdIn(anyCollection())).thenReturn(List.of());

        List<Spot> result = spotService.findNearby(new BigDecimal("127.05"), new BigDecimal("37.54"), "39");

        assertThat(result).extracting(Spot::getContentId).containsExactly("111", "222");

        ArgumentCaptor<List<Spot>> savedCaptor = ArgumentCaptor.forClass(List.class);
        verify(spotRepository).saveAll(savedCaptor.capture());
        assertThat(savedCaptor.getValue()).hasSize(2);
    }

    // 이미 존재하는 스팟은 새로 저장하지 않고 필드만 갱신(refresh)되는지 검증
    @Test
    void findNearbyRefreshesExistingSpotInsteadOfCreating() {
        SpotService spotService = new SpotService(korServiceListClient, spotRepository);
        LocationBasedListItem item = locationBasedItem("111", "가까운 카페(개명)", "100");
        Spot existingSpot = Spot.create("111", new Spot.Attributes(
                "가까운 카페", "A05", "A0502", "A05020900", TimeSlot.LUNCH,
                new BigDecimal("37.5443300"), new BigDecimal("127.0557800"),
                "1", "2", null
        ));
        when(korServiceListClient.findLocationBasedList("127.05", "37.54", "3000", "39"))
                .thenReturn(List.of(item));
        when(spotRepository.findAllByContentIdIn(anyCollection())).thenReturn(List.of(existingSpot));

        List<Spot> result = spotService.findNearby(new BigDecimal("127.05"), new BigDecimal("37.54"), "39");

        assertThat(result).containsExactly(existingSpot);
        assertThat(existingSpot.getName()).isEqualTo("가까운 카페(개명)");

        ArgumentCaptor<List<Spot>> savedCaptor = ArgumentCaptor.forClass(List.class);
        verify(spotRepository).saveAll(savedCaptor.capture());
        assertThat(savedCaptor.getValue()).isEmpty();
    }

    // 반경 미지정 시 기본값 3km(3000m)로 호출되는지 검증
    @Test
    void findNearbyUsesDefaultThreeKilometerRadius() {
        SpotService spotService = new SpotService(korServiceListClient, spotRepository);
        when(korServiceListClient.findLocationBasedList(eq("127.05"), eq("37.54"), eq("3000"), eq("39")))
                .thenReturn(List.of());

        spotService.findNearby(new BigDecimal("127.05"), new BigDecimal("37.54"), "39");

        verify(korServiceListClient).findLocationBasedList("127.05", "37.54", "3000", "39");
    }

    private LocationBasedListItem locationBasedItem(String contentId, String title, String dist) {
        return new LocationBasedListItem(
                contentId, "39", title, "서울 성동구",
                "1", "2",
                "A05", "A0502", "A05020900",
                "127.0557800", "37.5443300",
                "https://tong.visitkorea.or.kr/full.jpg", "https://tong.visitkorea.or.kr/thumb.jpg",
                dist
        );
    }
}
