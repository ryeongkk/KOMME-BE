package com.komme.domain.course.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 코스 체류시간 - 반나절/하루/여유롭게. DB엔 저장하지 않고 코스 생성 요청 시 스팟 개수를 정하는 데만 쓴다.
@Getter
@AllArgsConstructor
public enum Duration {
    HALF_DAY(4),
    FULL_DAY(6),
    LEISURELY(8);

    private final int spotCount;
}
