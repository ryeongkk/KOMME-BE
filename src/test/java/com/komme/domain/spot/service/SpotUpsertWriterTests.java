package com.komme.domain.spot.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.komme.domain.spot.entity.Spot;
import com.komme.domain.spot.enums.TimeSlot;
import com.komme.domain.spot.repository.SpotRepository;
import com.komme.domain.tourapi.client.LocationBasedListItem;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpotUpsertWriterTests {

    @Mock
    private SpotRepository spotRepository;

    // 신규 스팟은 saveAndFlush로 생성되고, 응답 순서(입력 순서)가 유지되는지 검증
    @Test
    void upsertAllCreatesNewSpotsInInputOrder() {
        SpotUpsertWriter spotUpsertWriter = new SpotUpsertWriter(spotRepository);
        LocationBasedListItem near = locationBasedItem("111", "가까운 카페");
        LocationBasedListItem far = locationBasedItem("222", "먼 카페");
        when(spotRepository.findAllByContentIdIn(anyCollection())).thenReturn(List.of());
        when(spotRepository.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));

        List<Spot> result = spotUpsertWriter.upsertAll(List.of(near, far));

        assertThat(result).extracting(Spot::getContentId).containsExactly("111", "222");
    }

    // 이미 존재하는 스팟은 새로 저장하지 않고 refresh만 되는지 검증
    @Test
    void upsertAllRefreshesExistingSpotInsteadOfCreating() {
        SpotUpsertWriter spotUpsertWriter = new SpotUpsertWriter(spotRepository);
        LocationBasedListItem item = locationBasedItem("111", "가까운 카페(개명)");
        Spot existingSpot = existingSpot("111", "가까운 카페");
        when(spotRepository.findAllByContentIdIn(anyCollection())).thenReturn(List.of(existingSpot));

        List<Spot> result = spotUpsertWriter.upsertAll(List.of(item));

        assertThat(result).containsExactly(existingSpot);
        assertThat(existingSpot.getName()).isEqualTo("가까운 카페(개명)");
        verify(spotRepository, never()).saveAndFlush(any());
    }

    // 신규 생성 중 동시 요청과의 unique 제약 충돌이 나면, 실패시키지 않고 재조회 후 refresh로 흡수하는지 검증
    @Test
    void upsertAllRecoversFromConcurrentUniqueConstraintViolation() {
        SpotUpsertWriter spotUpsertWriter = new SpotUpsertWriter(spotRepository);
        LocationBasedListItem item = locationBasedItem("111", "가까운 카페(새 데이터)");
        Spot concurrentlyInsertedSpot = existingSpot("111", "가까운 카페");
        when(spotRepository.findAllByContentIdIn(anyCollection())).thenReturn(List.of());
        when(spotRepository.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("duplicate"));
        when(spotRepository.findByContentId("111")).thenReturn(Optional.of(concurrentlyInsertedSpot));

        List<Spot> result = spotUpsertWriter.upsertAll(List.of(item));

        assertThat(result).containsExactly(concurrentlyInsertedSpot);
        assertThat(concurrentlyInsertedSpot.getName()).isEqualTo("가까운 카페(새 데이터)");
    }

    // 충돌 이후 재조회도 실패하면(설명 불가한 상황) 원래 예외를 그대로 던지는지 검증
    @Test
    void upsertAllRethrowsOriginalExceptionWhenRefetchAfterConflictAlsoFails() {
        SpotUpsertWriter spotUpsertWriter = new SpotUpsertWriter(spotRepository);
        LocationBasedListItem item = locationBasedItem("111", "가까운 카페");
        DataIntegrityViolationException conflictException = new DataIntegrityViolationException("duplicate");
        when(spotRepository.findAllByContentIdIn(anyCollection())).thenReturn(List.of());
        when(spotRepository.saveAndFlush(any())).thenThrow(conflictException);
        when(spotRepository.findByContentId("111")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> spotUpsertWriter.upsertAll(List.of(item)))
                .isSameAs(conflictException);
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
