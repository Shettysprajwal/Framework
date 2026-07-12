package com.pqvcf.pdp.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {"com.pqvcf.pdp", "com.pqvcf.shared"})
@EntityScan(basePackages = "com.pqvcf.pdp.infrastructure.persistence.jpa")
@EnableJpaRepositories(basePackages = "com.pqvcf.pdp.infrastructure.persistence.jpa")
public class PolicyDecisionApplication {

    public static void main(String[] args) {
        SpringApplication.run(PolicyDecisionApplication.class, args);
    }
}
