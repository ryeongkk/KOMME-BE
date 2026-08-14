package com.komme.domain.course.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 코스 생성 시 선택하는 방문 장소 개수 - 2개/3개/4개 이상
@Getter
@AllArgsConstructor
public enum SpotCount {
    TWO(2),
    THREE(3),
    FOUR_OR_MORE(4);

    private final int value;
}
