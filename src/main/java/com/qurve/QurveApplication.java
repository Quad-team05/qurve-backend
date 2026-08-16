package com.qurve;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing(dateTimeProviderRef = "kstDateTimeProvider")
public class QurveApplication {

    public static void main(String[] args) {
        SpringApplication.run(QurveApplication.class, args);
    }

}
