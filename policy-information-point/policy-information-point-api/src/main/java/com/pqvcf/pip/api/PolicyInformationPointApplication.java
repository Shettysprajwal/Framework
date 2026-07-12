package com.pqvcf.pip.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {"com.pqvcf.pip", "com.pqvcf.shared"})
@EntityScan(basePackages = "com.pqvcf.pip.infrastructure.persistence.jpa")
@EnableJpaRepositories(basePackages = "com.pqvcf.pip.infrastructure.persistence.jpa")
public class PolicyInformationPointApplication {

    public static void main(String[] args) {
        SpringApplication.run(PolicyInformationPointApplication.class, args);
    }
}
