package com.komme.domain.course.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.komme.common.exception.GeneralException;
import com.komme.domain.course.enums.Duration;
import com.komme.domain.course.enums.Topic;
import com.komme.domain.course.exception.CourseErrorStatus;
import com.komme.domain.course.mapping.CategoryTopicMapper;
import com.komme.domain.spot.entity.Spot;
import com.komme.domain.spot.service.SpotService;
import com.komme.domain.tourapi.SigunguCode;
import com.komme.domain.user.entity.User;
import com.komme.domain.user.service.UserReader;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

// 코스 생성 파이프라인 - 좌표+반경으로 후보 스팟을 모아 주제로 거르고, 동선을 정렬해 저장한다.
// 외부 API 호출(SpotService 경유)은 트랜잭션 밖에서 실행하고, DB 쓰기만 CoursePersister의 트랜잭션 안에서 처리한다.
@Service
@RequiredArgsConstructor
public class CourseGenerationService {

    // 관광지/문화시설/레포츠/음식점 - 축제행사/여행코스/숙박/쇼핑은 코스 후보에서 제외
    private static final List<String> CANDIDATE_CONTENT_TYPE_IDS = List.of("12", "14", "28", "39");
    // 스팟이 부족하면 3km -> 6km -> 9km로 넓혀가며 재시도, 그래도 부족하면 실패 처리
    private static final List<Integer> RADIUS_EXPANSION_METERS = List.of(3000, 6000, 9000);

    private final SpotService spotService;
    private final CoursePersister coursePersister;
    private final UserReader userReader;

    // 코스 생성 기능
    public CourseGenerationResult generate(
            Long userId,
            BigDecimal longitude,
            BigDecimal latitude,
            Set<Topic> topics,
            Duration duration,
            LocalDate visitDate
    ) {
        User user = userReader.findByIdOrThrow(userId);
        List<Spot> selectedSpots = collectCandidates(longitude, latitude, topics, duration.getSpotCount());
        List<Spot> orderedSpots = SpotRouteSequencer.sequenceFrom(longitude, latitude, selectedSpots);
        List<Integer> distancesToNext = SpotRouteSequencer.distancesBetweenConsecutive(orderedSpots);

        Spot representativeSpot = orderedSpots.get(0);
        String regionName = resolveRegionName(representativeSpot);
        String fallbackTitle = buildFallbackTitle(regionName, topics);

        return coursePersister.persist(
                user,
                fallbackTitle,
                regionName,
                representativeSpot.getAreaCode(),
                representativeSpot.getSigunguCode(),
                topics,
                visitDate,
                orderedSpots,
                distancesToNext
        );
    }

    // 반경을 넓혀가며 주제에 맞는 후보를 필요한 개수만큼 모을 때까지 재시도하는 기능
    private List<Spot> collectCandidates(BigDecimal longitude, BigDecimal latitude, Set<Topic> topics, int requiredSpotCount) {
        for (int radiusMeters : RADIUS_EXPANSION_METERS) {
            List<Spot> filtered = fetchAndFilterByTopics(longitude, latitude, radiusMeters, topics);
            if (filtered.size() >= requiredSpotCount) {
                return filtered.subList(0, requiredSpotCount);
            }
        }
        throw new GeneralException(CourseErrorStatus.INSUFFICIENT_SPOTS);
    }

    private List<Spot> fetchAndFilterByTopics(BigDecimal longitude, BigDecimal latitude, int radiusMeters, Set<Topic> topics) {
        List<Spot> candidates = new ArrayList<>();
        for (String contentTypeId : CANDIDATE_CONTENT_TYPE_IDS) {
            candidates.addAll(spotService.findNearby(longitude, latitude, radiusMeters, contentTypeId));
        }
        return candidates.stream()
                .filter(spot -> CategoryTopicMapper.resolve(spot.getCategory1())
                        .map(topics::contains)
                        .orElse(false))
                .toList();
    }

    // areaCode/sigunguCode로 지역명을 역조회하는 기능 - 못 찾으면 코드값 자체를 폴백으로 사용
    private String resolveRegionName(Spot representativeSpot) {
        return SigunguCode.findByAreaCodeAndSigunguCode(representativeSpot.getAreaCode(), representativeSpot.getSigunguCode())
                .map(SigunguCode::getDistrictName)
                .orElse(representativeSpot.getAreaCode() + "-" + representativeSpot.getSigunguCode());
    }

    private String buildFallbackTitle(String regionName, Set<Topic> topics) {
        String topicLabel = topics.stream().map(Topic::getLabel).collect(Collectors.joining("·"));
        return regionName + " " + topicLabel + " Day";
    }
}
