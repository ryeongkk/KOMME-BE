package com.komme.domain.auth.util;

public final class PasswordPolicy {

    public static final String PATTERN = "^(?=.*[A-Za-z])(?=.*\\d)[!-~]{8,20}$";
    public static final String MESSAGE =
            "비밀번호는 8~20자의 영문과 숫자를 포함해야 하며, 특수문자도 사용할 수 있습니다.";

    // 인스턴스 생성 방지
    private PasswordPolicy() {
    }
}
