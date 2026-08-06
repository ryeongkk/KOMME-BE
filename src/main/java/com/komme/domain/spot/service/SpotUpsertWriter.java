package com.komme.domain.spot.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.komme.domain.spot.entity.Spot;
import com.komme.domain.spot.mapping.SpotAttributesMapper;
import com.komme.domain.spot.repository.SpotRepository;
import com.komme.domain.tourapi.client.LocationBasedListItem;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

// tourapi 응답을 Spot으로 write-through upsert하는 DB 쓰기 전담 컴포넌트.
// 외부 API 호출(SpotService)과 분리해, 트랜잭션이 외부 호출까지 감싸지 않도록 한다.
@Component
@RequiredArgsConstructor
class SpotUpsertWriter {

    private final SpotRepository spotRepository;

    // tourapi 응답 item 목록을 Spot으로 upsert - 건별 조회 대신 in절 일괄조회로 N+1을 피한다
    @Transactional
    List<Spot> upsertAll(List<LocationBasedListItem> items) {
        List<String> contentIds = items.stream().map(LocationBasedListItem::contentId).toList();
        Map<String, Spot> existingSpotsByContentId = spotRepository.findAllByContentIdIn(contentIds).stream()
                .collect(Collectors.toMap(Spot::getContentId, Function.identity()));

        List<Spot> result = new ArrayList<>();
        for (LocationBasedListItem item : items) {
            result.add(upsertOne(item, existingSpotsByContentId.get(item.contentId())));
        }
        return result;
    }

    // 스팟 하나 upsert 기능 - 신규 생성 시 동시 요청과의 unique 제약 충돌을 재조회+refresh로 흡수한다
    private Spot upsertOne(LocationBasedListItem item, Spot existingSpot) {
        Spot.Attributes attributes = SpotAttributesMapper.fromLocationBasedItem(item);
        if (existingSpot != null) {
            existingSpot.refresh(attributes);
            return existingSpot;
        }
        try {
            return spotRepository.saveAndFlush(Spot.create(item.contentId(), attributes));
        } catch (DataIntegrityViolationException exception) {
            // 동시 요청이 먼저 같은 contentId를 넣은 경우 - 요청 전체를 실패시키지 않고 그 row를 최신화해서 흡수
            Spot concurrentlyInsertedSpot = spotRepository.findByContentId(item.contentId())
                    .orElseThrow(() -> exception);
            concurrentlyInsertedSpot.refresh(attributes);
            return concurrentlyInsertedSpot;
        }
    }
}
