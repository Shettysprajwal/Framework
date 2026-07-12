package com.pqvcf.translation.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {"com.pqvcf.translation", "com.pqvcf.shared"})
@EntityScan(basePackages = "com.pqvcf.translation.infrastructure.persistence.jpa")
@EnableJpaRepositories(basePackages = "com.pqvcf.translation.infrastructure.persistence.jpa")
public class RuleTranslationApplication {

    public static void main(String[] args) {
        SpringApplication.run(RuleTranslationApplication.class, args);
    }
}
