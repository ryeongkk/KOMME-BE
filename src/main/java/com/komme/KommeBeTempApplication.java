package com.komme;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
@ConfigurationPropertiesScan
public class KommeBeTempApplication {

    // KOMME 애플리케이션 실행
    public static void main(String[] args) {
        SpringApplication.run(KommeBeTempApplication.class, args);
    }

}
