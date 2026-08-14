package com.komme.domain.course.dto;

import java.time.LocalDate;
import java.util.Set;

import com.komme.domain.course.dto.request.CreateCourseRequest;
import com.komme.domain.course.dto.request.SaveCourseRequest;
import com.komme.domain.course.enums.SpotCount;
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
                "성수동", Set.of(Topic.FOOD), SpotCount.FOUR_OR_MORE, LocalDate.of(2026, 8, 10)
        );

        Set<ConstraintViolation<CreateCourseRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    // 지역 키워드가 없으면 검증 실패하는지 확인
    @Test
    void createCourseRequestFailsWhenRegionKeywordBlank() {
        CreateCourseRequest request = new CreateCourseRequest(
                " ", Set.of(Topic.FOOD), SpotCount.FOUR_OR_MORE, LocalDate.of(2026, 8, 10)
        );

        Set<ConstraintViolation<CreateCourseRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
    }

    // 주제가 비어있으면 검증 실패하는지 확인
    @Test
    void createCourseRequestFailsWhenTopicsEmpty() {
        CreateCourseRequest request = new CreateCourseRequest(
                "성수동", Set.of(), SpotCount.FOUR_OR_MORE, LocalDate.of(2026, 8, 10)
        );

        Set<ConstraintViolation<CreateCourseRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
    }

    // 장소 개수/방문날짜가 없으면 검증 실패하는지 확인
    @Test
    void createCourseRequestFailsWhenSpotCountOrVisitDateMissing() {
        CreateCourseRequest request = new CreateCourseRequest(
                "성수동", Set.of(Topic.FOOD), null, null
        );

        Set<ConstraintViolation<CreateCourseRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(2);
    }

    // 코스 저장 요청 올바른 값 검증 통과 확인
    @Test
    void saveCourseRequestPassesWithValidValue() {
        SaveCourseRequest request = new SaveCourseRequest("성수동 데이트 코스");

        assertThat(validator.validate(request)).isEmpty();
    }

    // 코스명이 비어있으면 검증 실패하는지 확인
    @Test
    void saveCourseRequestFailsWhenTitleBlank() {
        SaveCourseRequest request = new SaveCourseRequest(" ");

        Set<ConstraintViolation<SaveCourseRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
    }
}
