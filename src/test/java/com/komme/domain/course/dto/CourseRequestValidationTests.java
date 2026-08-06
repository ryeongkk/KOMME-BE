package com.komme.domain.course.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import com.komme.domain.course.dto.request.CreateCourseRequest;
import com.komme.domain.course.enums.Duration;
import com.komme.domain.course.enums.Topic;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CourseRequestValidationTests {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

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

    // 코스 생성 요청 올바른 값 검증 통과 확인
    @Test
    void createCourseRequestPassesWithValidValues() {
        CreateCourseRequest request = new CreateCourseRequest(
                new BigDecimal("127.05578"), new BigDecimal("37.54433"),
                Set.of(Topic.FOOD), Duration.HALF_DAY, LocalDate.of(2026, 8, 10)
        );

        Set<ConstraintViolation<CreateCourseRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    // 좌표가 없으면 검증 실패하는지 확인
    @Test
    void createCourseRequestFailsWhenCoordinatesMissing() {
        CreateCourseRequest request = new CreateCourseRequest(
                null, null, Set.of(Topic.FOOD), Duration.HALF_DAY, LocalDate.of(2026, 8, 10)
        );

        Set<ConstraintViolation<CreateCourseRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(2);
    }

    // 주제가 비어있으면 검증 실패하는지 확인
    @Test
    void createCourseRequestFailsWhenTopicsEmpty() {
        CreateCourseRequest request = new CreateCourseRequest(
                new BigDecimal("127.05578"), new BigDecimal("37.54433"),
                Set.of(), Duration.HALF_DAY, LocalDate.of(2026, 8, 10)
        );

        Set<ConstraintViolation<CreateCourseRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
    }

    // 체류시간/방문날짜가 없으면 검증 실패하는지 확인
    @Test
    void createCourseRequestFailsWhenDurationOrVisitDateMissing() {
        CreateCourseRequest request = new CreateCourseRequest(
                new BigDecimal("127.05578"), new BigDecimal("37.54433"),
                Set.of(Topic.FOOD), null, null
        );

        Set<ConstraintViolation<CreateCourseRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(2);
    }
}
