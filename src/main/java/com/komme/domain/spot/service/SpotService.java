package com.komme.domain.spot.service;

import java.math.BigDecimal;
import java.util.List;

import com.komme.domain.spot.entity.Spot;
import com.komme.domain.tourapi.client.KorServiceListClient;
import com.komme.domain.tourapi.client.LocationBasedListItem;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

// 위치기반 스팟 조회 - tourapi 응답을 Spot으로 write-through upsert하고, 응답 순서(거리순) 그대로 반환한다.
// 외부 API 호출은 트랜잭션 밖에서 실행하고, DB 쓰기(upsert)만 SpotUpsertWriter의 트랜잭션 안에서 처리한다.
@Service
@RequiredArgsConstructor
public class SpotService {

    private static final int DEFAULT_RADIUS_METERS = 3000;

    private final KorServiceListClient korServiceListClient;
    private final SpotUpsertWriter spotUpsertWriter;

    // 기본 반경(3km) 주변 스팟 조회 기능
    public List<Spot> findNearby(BigDecimal longitude, BigDecimal latitude, String contentTypeId) {
        return findNearby(longitude, latitude, DEFAULT_RADIUS_METERS, contentTypeId);
    }

    // 반경 지정 주변 스팟 조회 기능 - locationBasedList2가 이미 거리순으로 응답하므로 그 순서를 그대로 유지한다
    public List<Spot> findNearby(BigDecimal longitude, BigDecimal latitude, int radiusMeters, String contentTypeId) {
        List<LocationBasedListItem> items = korServiceListClient.findLocationBasedList(
                longitude.toPlainString(),
                latitude.toPlainString(),
                String.valueOf(radiusMeters),
                contentTypeId
        );
        if (items.isEmpty()) {
            return List.of();
        }
        return spotUpsertWriter.upsertAll(items);
    }
}
