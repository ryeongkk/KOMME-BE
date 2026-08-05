package com.komme;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;

@SpringBootTest
class KommeBeTempApplicationTests {

    // Spring Context 로딩 검증
    @Test
    void contextLoads() {
    }

    // 애플리케이션 클래스 기본 생성 검증
    @Test
    void applicationCanBeInstantiated() {
        KommeBeTempApplication application = new KommeBeTempApplication();

        assertThat(application).isNotNull();
    }

    // 애플리케이션 main 실행 위임 검증
    @Test
    void mainDelegatesToSpringApplicationRun() {
        String[] args = {"--spring.profiles.active=test"};

        try (MockedStatic<SpringApplication> springApplication = mockStatic(SpringApplication.class)) {
            KommeBeTempApplication.main(args);

            springApplication.verify(() -> SpringApplication.run(
                    KommeBeTempApplication.class,
                    args
            ));
        }
    }
}
