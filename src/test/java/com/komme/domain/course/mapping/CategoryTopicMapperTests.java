package com.komme.domain.course.mapping;

import com.komme.domain.course.enums.Topic;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CategoryTopicMapperTests {

    // 뚜렷이 연결되는 카테고리는 올바른 Topic으로 매핑되는지 검증
    @Test
    void resolveMapsKnownCategoriesToTopic() {
        assertThat(CategoryTopicMapper.resolve("A01")).contains(Topic.HEALING);
        assertThat(CategoryTopicMapper.resolve("A02")).contains(Topic.EXPLORATION);
        assertThat(CategoryTopicMapper.resolve("A03")).contains(Topic.EXPLORATION);
        assertThat(CategoryTopicMapper.resolve("A05")).contains(Topic.FOOD);
    }

    // 주제와 애매하게 연결되는 카테고리(쇼핑/숙박/추천코스)는 폴백 없이 제외되는지 검증
    @Test
    void resolveExcludesAmbiguousCategories() {
        assertThat(CategoryTopicMapper.resolve("A04")).isEmpty();
        assertThat(CategoryTopicMapper.resolve("B02")).isEmpty();
        assertThat(CategoryTopicMapper.resolve("C01")).isEmpty();
    }

    // 알 수 없는 코드나 null도 제외되는지 검증
    @Test
    void resolveExcludesUnknownOrNullCategory() {
        assertThat(CategoryTopicMapper.resolve("Z99")).isEmpty();
        assertThat(CategoryTopicMapper.resolve(null)).isEmpty();
    }
}
