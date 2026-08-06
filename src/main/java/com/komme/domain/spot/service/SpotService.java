package com.komme.domain.spot.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.komme.domain.spot.entity.Spot;
import com.komme.domain.spot.mapping.SpotAttributesMapper;
import com.komme.domain.spot.repository.SpotRepository;
import com.komme.domain.tourapi.client.KorServiceListClient;
import com.komme.domain.tourapi.client.LocationBasedListItem;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

// 위치기반 스팟 조회 - tourapi 응답을 Spot으로 write-through upsert하고, 응답 순서(거리순) 그대로 반환한다.
@Service
@RequiredArgsConstructor
public class SpotService {

    private static final int DEFAULT_RADIUS_METERS = 3000;

    private final KorServiceListClient korServiceListClient;
    private final SpotRepository spotRepository;

    // 기본 반경(3km) 주변 스팟 조회 기능
    @Transactional
    public List<Spot> findNearby(BigDecimal longitude, BigDecimal latitude, String contentTypeId) {
        return findNearby(longitude, latitude, DEFAULT_RADIUS_METERS, contentTypeId);
    }

    // 반경 지정 주변 스팟 조회 기능 - locationBasedList2가 이미 거리순으로 응답하므로 그 순서를 그대로 유지한다
    @Transactional
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
        return upsertAll(items);
    }

    // tourapi 응답 item 목록을 Spot으로 upsert - 건별 조회 대신 in절 일괄조회로 N+1을 피한다
    private List<Spot> upsertAll(List<LocationBasedListItem> items) {
        List<String> contentIds = items.stream().map(LocationBasedListItem::contentId).toList();
        Map<String, Spot> existingSpotsByContentId = spotRepository.findAllByContentIdIn(contentIds).stream()
                .collect(Collectors.toMap(Spot::getContentId, Function.identity()));

        List<Spot> newSpots = new ArrayList<>();
        List<Spot> result = new ArrayList<>();
        for (LocationBasedListItem item : items) {
            Spot.Attributes attributes = SpotAttributesMapper.fromLocationBasedItem(item);
            Spot existingSpot = existingSpotsByContentId.get(item.contentId());
            if (existingSpot != null) {
                existingSpot.refresh(attributes);
                result.add(existingSpot);
            } else {
                Spot newSpot = Spot.create(item.contentId(), attributes);
                newSpots.add(newSpot);
                result.add(newSpot);
            }
        }
        spotRepository.saveAll(newSpots);
        return result;
    }
}
