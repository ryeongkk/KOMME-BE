package com.komme.common.exception;

import com.komme.common.base.status.ErrorStatus;
import com.komme.common.response.ApiResponse;
import com.komme.domain.user.util.NicknamePolicy;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.Pattern;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class GeneralExceptionAdviceTests {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    private final GeneralExceptionAdvice generalExceptionAdvice = new GeneralExceptionAdvice();

    // Bean Validation 테스트 환경 구성
    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    // Bean Validation 리소스 정리 기능
    @AfterAll
    static void closeValidatorFactory() {
        validatorFactory.close();
    }

    // 요청 파라미터 검증 예외 400 응답 변환 검증
    @Test
    void handleConstraintViolationExceptionReturnsBadRequest() {
        Set<ConstraintViolation<NicknameParameter>> violations =
                validator.validate(new NicknameParameter("n"));
        ConstraintViolationException exception = new ConstraintViolationException(violations);

        ResponseEntity<ApiResponse<Void>> response =
                generalExceptionAdvice.handleConstraintViolationException(exception);

        assertThat(response.getStatusCode()).isEqualTo(ErrorStatus.BAD_REQUEST.getHttpStatus());
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(ErrorStatus.BAD_REQUEST.getCode());
        assertThat(response.getBody().getMessage())
                .isEqualTo("nickname: " + NicknamePolicy.MESSAGE);
    }

    private record NicknameParameter(
            @Pattern(regexp = NicknamePolicy.PATTERN, message = NicknamePolicy.MESSAGE)
            String nickname
    ) {
    }
}
