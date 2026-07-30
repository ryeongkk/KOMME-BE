package com.komme.domain.user.util;

public final class NicknamePolicy {

    public static final String PATTERN = "^[A-Za-z0-9]{2,20}$";
    public static final String MESSAGE = "닉네임은 2~20자의 영문과 숫자만 사용할 수 있습니다.";

    // 인스턴스 생성 방지
    private NicknamePolicy() {
    }
}
