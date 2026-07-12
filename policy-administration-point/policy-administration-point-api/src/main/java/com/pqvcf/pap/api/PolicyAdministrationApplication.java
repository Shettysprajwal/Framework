package com.pqvcf.pap.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {"com.pqvcf.pap", "com.pqvcf.shared"})
@EntityScan(basePackages = "com.pqvcf.pap.infrastructure.persistence.jpa")
@EnableJpaRepositories(basePackages = "com.pqvcf.pap.infrastructure.persistence.jpa")
public class PolicyAdministrationApplication {

    public static void main(String[] args) {
        SpringApplication.run(PolicyAdministrationApplication.class, args);
    }
}
