package com.komme.domain.tourapi;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SigunguCodeTests {

    // 서울/부산에 중복되는 구 이름(중구, 강서구)이 시/도명 기준으로 정확히 구분되는지 검증
    @Test
    void findBySiDoAndDistrictNameDisambiguatesDuplicateDistrictNames() {
        Optional<SigunguCode> seoulJung = SigunguCode.findBySiDoAndDistrictName("서울특별시", "중구");
        Optional<SigunguCode> busanJung = SigunguCode.findBySiDoAndDistrictName("부산광역시", "중구");

        assertThat(seoulJung).contains(SigunguCode.JUNG_GU_SEOUL);
        assertThat(busanJung).contains(SigunguCode.JUNG_GU_BUSAN);
        assertThat(seoulJung.get().getSigunguCode()).isNotEqualTo(busanJung.get().getSigunguCode());
    }

    // areaCode 기준 조회로도 동일하게 구분되는지 검증
    @Test
    void findByAreaCodeAndDistrictNameDisambiguatesDuplicateDistrictNames() {
        Optional<SigunguCode> seoulGangseo = SigunguCode.findByAreaCodeAndDistrictName("11", "강서구");
        Optional<SigunguCode> busanGangseo = SigunguCode.findByAreaCodeAndDistrictName("26", "강서구");

        assertThat(seoulGangseo).contains(SigunguCode.GANGSEO_GU_SEOUL);
        assertThat(busanGangseo).contains(SigunguCode.GANGSEO_GU_BUSAN);
    }

    // 존재하지 않는 조합은 빈 값을 반환하는지 검증
    @Test
    void findByAreaCodeAndDistrictNameReturnsEmptyWhenNotFound() {
        Optional<SigunguCode> result = SigunguCode.findByAreaCodeAndDistrictName("11", "해운대구");

        assertThat(result).isEmpty();
    }

    // 서울/부산 각각 정확한 개수가 등록되어 있는지 검증
    @Test
    void hasAllSeoulAndBusanDistricts() {
        long seoulCount = java.util.Arrays.stream(SigunguCode.values())
                .filter(code -> "11".equals(code.getAreaCode()))
                .count();
        long busanCount = java.util.Arrays.stream(SigunguCode.values())
                .filter(code -> "26".equals(code.getAreaCode()))
                .count();

        assertThat(seoulCount).isEqualTo(25);
        assertThat(busanCount).isEqualTo(16);
    }
}
