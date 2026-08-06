package com.komme.common.exception;

import com.komme.common.base.status.ErrorStatus;
import com.komme.common.response.ApiResponse;
import com.komme.domain.user.util.NicknamePolicy;

import java.lang.reflect.Method;
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
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

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

    // 검증 경로에 점(.)이 포함된 경우(메서드 파라미터 검증 등) 마지막 세그먼트만 추출되는지 검증
    @Test
    void handleConstraintViolationExceptionExtractsLastSegmentFromDottedPropertyPath() throws NoSuchMethodException {
        Method method = ValidationTarget.class.getMethod("validate", String.class);
        Set<ConstraintViolation<ValidationTarget>> violations =
                validator.forExecutables().validateParameters(new ValidationTarget(), method, new Object[] { "n" });
        ConstraintViolationException exception = new ConstraintViolationException(violations);

        ResponseEntity<ApiResponse<Void>> response =
                generalExceptionAdvice.handleConstraintViolationException(exception);

        // 메서드 파라미터 검증 경로는 "validate.arg0"처럼 점이 포함되는데, 마지막 세그먼트만 남아야 한다
        assertThat(response.getBody().getMessage()).doesNotContain("validate.");
        assertThat(response.getBody().getMessage()).contains(NicknamePolicy.MESSAGE);
    }

    // GeneralException 5xx 상태를 그대로 응답에 반영하는지 검증 (ERROR 로그 분기)
    @Test
    void handleGeneralExceptionReturnsServerErrorStatus() {
        GeneralException exception = new GeneralException(ErrorStatus.INTERNAL_SERVER_ERROR);

        ResponseEntity<ApiResponse<Void>> response =
                generalExceptionAdvice.handleGeneralException(exception);

        assertThat(response.getStatusCode()).isEqualTo(ErrorStatus.INTERNAL_SERVER_ERROR.getHttpStatus());
        assertThat(response.getBody().getCode()).isEqualTo(ErrorStatus.INTERNAL_SERVER_ERROR.getCode());
    }

    // GeneralException 4xx 상태를 그대로 응답에 반영하는지 검증 (WARN 로그 분기)
    @Test
    void handleGeneralExceptionReturnsClientErrorStatus() {
        GeneralException exception = new GeneralException(ErrorStatus.NOT_FOUND);

        ResponseEntity<ApiResponse<Void>> response =
                generalExceptionAdvice.handleGeneralException(exception);

        assertThat(response.getStatusCode()).isEqualTo(ErrorStatus.NOT_FOUND.getHttpStatus());
        assertThat(response.getBody().getCode()).isEqualTo(ErrorStatus.NOT_FOUND.getCode());
    }

    // 잘못된 인자 예외가 400 + 메시지 접두사와 함께 응답되는지 검증
    @Test
    void handleIllegalArgumentExceptionReturnsBadRequestWithPrefixedMessage() {
        IllegalArgumentException exception = new IllegalArgumentException("id는 양수여야 합니다.");

        ResponseEntity<ApiResponse<Void>> response =
                generalExceptionAdvice.handleIllegalArgumentException(exception);

        assertThat(response.getStatusCode()).isEqualTo(ErrorStatus.BAD_REQUEST.getHttpStatus());
        assertThat(response.getBody().getMessage()).isEqualTo("잘못된 요청입니다: id는 양수여야 합니다.");
    }

    // NullPointerException이 500으로 변환되는지 검증
    @Test
    void handleNullPointerExceptionReturnsInternalServerError() {
        NullPointerException exception = new NullPointerException("user is null");

        ResponseEntity<ApiResponse<Void>> response =
                generalExceptionAdvice.handleNullPointerException(exception);

        assertThat(response.getStatusCode()).isEqualTo(ErrorStatus.INTERNAL_SERVER_ERROR.getHttpStatus());
        assertThat(response.getBody().getCode()).isEqualTo(ErrorStatus.INTERNAL_SERVER_ERROR.getCode());
    }

    // 처리되지 않은 임의의 예외가 500으로 변환되는지 검증 (catch-all)
    @Test
    void handleExceptionReturnsInternalServerErrorForUnknownException() {
        RuntimeException exception = new RuntimeException("예상치 못한 오류");

        ResponseEntity<ApiResponse<Void>> response =
                generalExceptionAdvice.handleException(exception);

        assertThat(response.getStatusCode()).isEqualTo(ErrorStatus.INTERNAL_SERVER_ERROR.getHttpStatus());
        assertThat(response.getBody().getCode()).isEqualTo(ErrorStatus.INTERNAL_SERVER_ERROR.getCode());
    }

    // @Valid 필드 에러가 있으면 필드명 기반 메시지로 응답되는지 검증
    @Test
    void handleMethodArgumentNotValidUsesFirstFieldErrorMessage() throws NoSuchMethodException {
        BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "signUpRequest");
        bindingResult.addError(new FieldError("signUpRequest", "nickname", "닉네임은 필수입니다."));
        MethodArgumentNotValidException exception = methodArgumentNotValidException(bindingResult);

        ResponseEntity<Object> response = generalExceptionAdvice.handleMethodArgumentNotValid(
                exception, new HttpHeaders(), HttpStatus.BAD_REQUEST, mock(WebRequest.class)
        );

        ApiResponse<?> body = (ApiResponse<?>) response.getBody();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(body.getMessage()).isEqualTo("nickname: 닉네임은 필수입니다.");
    }

    // 필드 에러 없이 글로벌 에러만 있으면 글로벌 에러 메시지로 폴백하는지 검증
    @Test
    void handleMethodArgumentNotValidFallsBackToGlobalErrorMessage() throws NoSuchMethodException {
        BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "signUpRequest");
        bindingResult.addError(new ObjectError("signUpRequest", "비밀번호와 비밀번호 확인이 일치하지 않습니다."));
        MethodArgumentNotValidException exception = methodArgumentNotValidException(bindingResult);

        ResponseEntity<Object> response = generalExceptionAdvice.handleMethodArgumentNotValid(
                exception, new HttpHeaders(), HttpStatus.BAD_REQUEST, mock(WebRequest.class)
        );

        ApiResponse<?> body = (ApiResponse<?>) response.getBody();
        assertThat(body.getMessage())
                .isEqualTo("signUpRequest: 비밀번호와 비밀번호 확인이 일치하지 않습니다.");
    }

    private MethodArgumentNotValidException methodArgumentNotValidException(BindingResult bindingResult)
            throws NoSuchMethodException {
        Method dummyMethod = DummyTarget.class.getDeclaredMethod("dummyMethod", String.class);
        MethodParameter methodParameter = new MethodParameter(dummyMethod, 0);
        return new MethodArgumentNotValidException(methodParameter, bindingResult);
    }

    private record NicknameParameter(
            @Pattern(regexp = NicknamePolicy.PATTERN, message = NicknamePolicy.MESSAGE)
            String nickname
    ) {
    }

    // 메서드 파라미터 검증(점이 포함된 propertyPath)을 유발하기 위한 대상
    public static class ValidationTarget {
        public void validate(
                @Pattern(regexp = NicknamePolicy.PATTERN, message = NicknamePolicy.MESSAGE)
                String nickname
        ) {
        }
    }

    // MethodParameter 생성용 더미 - 실제로 호출되지 않고 리플렉션 대상으로만 쓰인다
    private static final class DummyTarget {
        void dummyMethod(String arg) {
        }
    }
}
