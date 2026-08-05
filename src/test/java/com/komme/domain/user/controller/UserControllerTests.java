package com.komme.domain.user.controller;

import com.komme.common.exception.GeneralExceptionAdvice;
import com.komme.domain.user.dto.response.NicknameAvailabilityResponse;
import com.komme.domain.user.service.UserProfileService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerTests {

    private UserProfileService userProfileService;
    private MockMvc mockMvc;

    // 사용자 컨트롤러 테스트 환경 구성
    @BeforeEach
    void setUp() {
        userProfileService = mock(UserProfileService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new UserController(userProfileService))
                .setControllerAdvice(new GeneralExceptionAdvice())
                .build();
    }

    // 닉네임 사용 가능 여부 API 성공 응답 검증
    @Test
    void checkNicknameAvailabilityReturnsAvailability() throws Exception {
        when(userProfileService.getNicknameAvailability("nickname"))
                .thenReturn(NicknameAvailabilityResponse.of(true));

        mockMvc.perform(get("/api/v1/users/nicknames/availability")
                        .param("nickname", "nickname"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data.available").value(true));
    }

    // 닉네임 사용 가능 여부 API 입력값 오류 응답 검증
    @Test
    void checkNicknameAvailabilityRejectsMissingNickname() throws Exception {
        mockMvc.perform(get("/api/v1/users/nicknames/availability"))
                .andExpect(status().isBadRequest());
    }
}
