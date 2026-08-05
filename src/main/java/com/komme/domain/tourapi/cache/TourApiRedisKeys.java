package com.komme.domain.tourapi.cache;

public final class TourApiRedisKeys {

    private static final String KOR_SERVICE_AREA_BASED_LIST_KEY_PREFIX = "tourapi:kor-service:area-based-list:";
    private static final String KOR_SERVICE_LOCATION_BASED_LIST_KEY_PREFIX = "tourapi:kor-service:location-based-list:";
    private static final String RELATED_SPOT_KEY_PREFIX = "tourapi:related-spot:";
    private static final String CONCENTRATION_RATE_KEY_PREFIX = "tourapi:concentration-rate:";
    private static final String MULTILINGUAL_DETAIL_KEY_PREFIX = "tourapi:multilingual-detail:";
    private static final String KAKAO_LOCAL_SEARCH_KEY_PREFIX = "tourapi:kakao:local-search:";

    // 인스턴스 생성 방지
    private TourApiRedisKeys() {
    }

    // 지역기반 스팟 목록(areaBasedList2) 캐시 키 생성
    public static String korServiceAreaBasedList(String areaCode, String sigunguCode, String contentTypeId) {
        return KOR_SERVICE_AREA_BASED_LIST_KEY_PREFIX + areaCode + ":" + sigunguCode + ":" + contentTypeId;
    }

    // 좌표기반 스팟 목록(locationBasedList2) 캐시 키 생성
    public static String korServiceLocationBasedList(String mapX, String mapY, String radius, String contentTypeId) {
        return KOR_SERVICE_LOCATION_BASED_LIST_KEY_PREFIX + mapX + ":" + mapY + ":" + radius + ":" + contentTypeId;
    }

    // 연관 관광지 목록 캐시 키 생성
    public static String relatedSpot(String contentId) {
        return RELATED_SPOT_KEY_PREFIX + contentId;
    }

    // 관광지 집중률 캐시 키 생성 (기준 연월일 버킷 단위)
    public static String concentrationRate(String areaCode, String sigunguCode, String baseYmd) {
        return CONCENTRATION_RATE_KEY_PREFIX + areaCode + ":" + sigunguCode + ":" + baseYmd;
    }

    // 다국어 상세정보 캐시 키 생성
    public static String multilingualDetail(String contentId, String languageCode) {
        return MULTILINGUAL_DETAIL_KEY_PREFIX + languageCode + ":" + contentId;
    }

    // 카카오 로컬 검색 캐시 키 생성
    public static String kakaoLocalSearch(String keyword) {
        return KAKAO_LOCAL_SEARCH_KEY_PREFIX + keyword;
    }
}
