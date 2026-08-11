package com.komme.domain.auth.util;

public final class PasswordPolicy {

    public static final String PATTERN = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z0-9])[!-~]{8,20}$";
    public static final String MESSAGE =
            "비밀번호는 8~20자의 영문, 숫자, 특수문자를 모두 포함해야 합니다.";

    // 인스턴스 생성 방지
    private PasswordPolicy() {
    }
}
