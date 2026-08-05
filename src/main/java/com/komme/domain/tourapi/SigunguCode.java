package com.komme.domain.tourapi;

import java.util.Arrays;
import java.util.Optional;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 서울/부산 시군구 코드 (행정표준코드, KOMME MVP 서비스 지역 스코프) - 관광공사 OpenAPI의 areaCd/signguCd(sigunguCode) 파라미터에 사용
@Getter
@RequiredArgsConstructor
public enum SigunguCode {

    JONGNO_GU("11", "11110", "서울특별시", "종로구"),
    JUNG_GU_SEOUL("11", "11140", "서울특별시", "중구"),
    YONGSAN_GU("11", "11170", "서울특별시", "용산구"),
    SEONGDONG_GU("11", "11200", "서울특별시", "성동구"),
    GWANGJIN_GU("11", "11215", "서울특별시", "광진구"),
    DONGDAEMUN_GU("11", "11230", "서울특별시", "동대문구"),
    JUNGNANG_GU("11", "11260", "서울특별시", "중랑구"),
    SEONGBUK_GU("11", "11290", "서울특별시", "성북구"),
    GANGBUK_GU("11", "11305", "서울특별시", "강북구"),
    DOBONG_GU("11", "11320", "서울특별시", "도봉구"),
    NOWON_GU("11", "11350", "서울특별시", "노원구"),
    EUNPYEONG_GU("11", "11380", "서울특별시", "은평구"),
    SEODAEMUN_GU("11", "11410", "서울특별시", "서대문구"),
    MAPO_GU("11", "11440", "서울특별시", "마포구"),
    YANGCHEON_GU("11", "11470", "서울특별시", "양천구"),
    GANGSEO_GU_SEOUL("11", "11500", "서울특별시", "강서구"),
    GURO_GU("11", "11530", "서울특별시", "구로구"),
    GEUMCHEON_GU("11", "11545", "서울특별시", "금천구"),
    YEONGDEUNGPO_GU("11", "11560", "서울특별시", "영등포구"),
    DONGJAK_GU("11", "11590", "서울특별시", "동작구"),
    GWANAK_GU("11", "11620", "서울특별시", "관악구"),
    SEOCHO_GU("11", "11650", "서울특별시", "서초구"),
    GANGNAM_GU("11", "11680", "서울특별시", "강남구"),
    SONGPA_GU("11", "11710", "서울특별시", "송파구"),
    GANGDONG_GU("11", "11740", "서울특별시", "강동구"),

    JUNG_GU_BUSAN("26", "26110", "부산광역시", "중구"),
    SEO_GU_BUSAN("26", "26140", "부산광역시", "서구"),
    DONG_GU_BUSAN("26", "26170", "부산광역시", "동구"),
    YEONGDO_GU("26", "26200", "부산광역시", "영도구"),
    BUSANJIN_GU("26", "26230", "부산광역시", "부산진구"),
    DONGNAE_GU("26", "26260", "부산광역시", "동래구"),
    NAM_GU_BUSAN("26", "26290", "부산광역시", "남구"),
    BUK_GU_BUSAN("26", "26320", "부산광역시", "북구"),
    HAEUNDAE_GU("26", "26350", "부산광역시", "해운대구"),
    SAHA_GU("26", "26380", "부산광역시", "사하구"),
    GEUMJEONG_GU("26", "26410", "부산광역시", "금정구"),
    GANGSEO_GU_BUSAN("26", "26440", "부산광역시", "강서구"),
    YEONJE_GU("26", "26470", "부산광역시", "연제구"),
    SUYEONG_GU("26", "26500", "부산광역시", "수영구"),
    SASANG_GU("26", "26530", "부산광역시", "사상구"),
    GIJANG_GUN("26", "26710", "부산광역시", "기장군");

    private final String areaCode;
    private final String sigunguCode;
    private final String siDoName;
    private final String districtName;

    // 시/도명 + 구/군명으로 조회 - 서울/부산에 이름이 겹치는 구(중구, 강서구)가 있어 시/도명 없이는 특정할 수 없다
    public static Optional<SigunguCode> findBySiDoAndDistrictName(String siDoName, String districtName) {
        return Arrays.stream(values())
                .filter(code -> code.siDoName.equals(siDoName) && code.districtName.equals(districtName))
                .findFirst();
    }

    // areaCode(11=서울/26=부산) + 구/군명으로 조회
    public static Optional<SigunguCode> findByAreaCodeAndDistrictName(String areaCode, String districtName) {
        return Arrays.stream(values())
                .filter(code -> code.areaCode.equals(areaCode) && code.districtName.equals(districtName))
                .findFirst();
    }
}
