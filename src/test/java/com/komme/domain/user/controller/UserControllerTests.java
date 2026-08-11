package com.komme.domain.user.controller;

import com.komme.common.exception.GeneralExceptionAdvice;
import com.komme.common.response.ApiResponse;
import com.komme.domain.i18n.enums.Language;
import com.komme.domain.user.dto.request.ChangeNicknameRequest;
import com.komme.domain.user.dto.request.ChangePreferredLanguageRequest;
import com.komme.domain.user.dto.request.LocationConsentRequest;
import com.komme.domain.user.dto.response.NicknameAvailabilityResponse;
import com.komme.domain.user.dto.response.UserProfileResponse;
import com.komme.domain.user.enums.Provider;
import com.komme.domain.user.service.UserProfileService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerTests {

    private UserProfileService userProfileService;
    private UserController userController;
    private MockMvc mockMvc;

    // 사용자 컨트롤러 테스트 환경 구성
    @BeforeEach
    void setUp() {
        userProfileService = mock(UserProfileService.class);
        userController = new UserController(userProfileService);
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new GeneralExceptionAdvice())
                .build();
    }

    // 마이페이지 프로필 조회 API 응답 검증
    @Test
    void getMyProfileReturnsProfileResponse() {
        UserProfileResponse profileResponse = new UserProfileResponse(
                "nickname",
                Provider.LOCAL,
                Language.ENGLISH,
                true
        );
        when(userProfileService.getMyProfile(1L)).thenReturn(profileResponse);

        ResponseEntity<ApiResponse<UserProfileResponse>> response =
                userController.getMyProfile(1L);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody().getData()).isEqualTo(profileResponse);
    }

    // 마이페이지 닉네임 변경 API 위임 검증
    @Test
    void changeNicknameDelegatesToService() {
        ChangeNicknameRequest request = new ChangeNicknameRequest("newNickname");

        ResponseEntity<ApiResponse<Void>> response = userController.changeNickname(1L, request);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        verify(userProfileService).changeNickname(1L, request);
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

    // 마이페이지 선호 언어 변경 API 위임 검증
    @Test
    void changePreferredLanguageDelegatesToService() {
        ChangePreferredLanguageRequest request =
                new ChangePreferredLanguageRequest(Language.JAPANESE);

        ResponseEntity<ApiResponse<Void>> response =
                userController.changePreferredLanguage(1L, request);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        verify(userProfileService).changePreferredLanguage(1L, request);
    }

    // 마이페이지 위치 정보 동의 변경 API 위임 검증
    @Test
    void updateLocationConsentDelegatesToService() {
        LocationConsentRequest request = new LocationConsentRequest(true);

        ResponseEntity<ApiResponse<Void>> response =
                userController.updateLocationConsent(1L, request);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        verify(userProfileService).updateLocationConsent(1L, request);
    }
}
