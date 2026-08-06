package com.komme.domain.course.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 코스 생성 시 선택하는 주제 - 먹방/힐링/탐험
@Getter
@AllArgsConstructor
public enum Topic {
    FOOD("먹방"),
    HEALING("힐링"),
    EXPLORATION("탐험");

    private final String label;
}
