package com.komme.domain.course.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.komme.domain.spot.entity.Spot;

// 좌표 기반 nearest-neighbor로 스팟 방문 순서를 정렬한다.
// 관광공사 연관 관광지 API(RelatedSpotClient)는 "의미적 연관성"이지 "이동거리"가 아니라서 동선 정렬 용도로는 쓰지 않기로 했다.
public final class SpotRouteSequencer {

    private static final double EARTH_RADIUS_METERS = 6_371_000;

    private SpotRouteSequencer() {
    }

    // 시작 좌표에서 가장 가까운 스팟부터 차례로 방문하도록 정렬 기능 (nearest-neighbor 근사, 최적해는 아님)
    public static List<Spot> sequenceFrom(BigDecimal startLongitude, BigDecimal startLatitude, List<Spot> spots) {
        List<Spot> remaining = new ArrayList<>(spots);
        List<Spot> ordered = new ArrayList<>();
        BigDecimal currentLongitude = startLongitude;
        BigDecimal currentLatitude = startLatitude;

        while (!remaining.isEmpty()) {
            Spot nearest = findNearest(currentLongitude, currentLatitude, remaining);
            remaining.remove(nearest);
            ordered.add(nearest);
            currentLongitude = nearest.getLongitude();
            currentLatitude = nearest.getLatitude();
        }
        return ordered;
    }

    // 정렬된 스팟들의 연속 구간 거리(m) 목록 기능 - 마지막 스팟은 다음이 없어 크기가 spots.size() - 1
    public static List<Integer> distancesBetweenConsecutive(List<Spot> orderedSpots) {
        List<Integer> distances = new ArrayList<>();
        for (int i = 0; i < orderedSpots.size() - 1; i++) {
            distances.add(distanceMeters(orderedSpots.get(i), orderedSpots.get(i + 1)));
        }
        return distances;
    }

    private static Spot findNearest(BigDecimal fromLongitude, BigDecimal fromLatitude, List<Spot> candidates) {
        return candidates.stream()
                .min(Comparator.comparingDouble(
                        spot -> haversineMeters(fromLongitude, fromLatitude, spot.getLongitude(), spot.getLatitude())
                ))
                .orElseThrow(); // candidates가 비어있지 않은 상태에서만 호출되므로 안전
    }

    private static int distanceMeters(Spot from, Spot to) {
        return (int) Math.round(
                haversineMeters(from.getLongitude(), from.getLatitude(), to.getLongitude(), to.getLatitude())
        );
    }

    private static double haversineMeters(BigDecimal fromLongitude, BigDecimal fromLatitude, BigDecimal toLongitude, BigDecimal toLatitude) {
        double fromLatRad = Math.toRadians(fromLatitude.doubleValue());
        double toLatRad = Math.toRadians(toLatitude.doubleValue());
        double deltaLatRad = Math.toRadians(toLatitude.doubleValue() - fromLatitude.doubleValue());
        double deltaLonRad = Math.toRadians(toLongitude.doubleValue() - fromLongitude.doubleValue());

        double a = Math.sin(deltaLatRad / 2) * Math.sin(deltaLatRad / 2)
                + Math.cos(fromLatRad) * Math.cos(toLatRad) * Math.sin(deltaLonRad / 2) * Math.sin(deltaLonRad / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_METERS * c;
    }
}
