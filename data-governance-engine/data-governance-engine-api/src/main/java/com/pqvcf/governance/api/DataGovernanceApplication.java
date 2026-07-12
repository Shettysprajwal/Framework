package com.pqvcf.governance.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {"com.pqvcf.governance", "com.pqvcf.shared"})
@EntityScan(basePackages = "com.pqvcf.governance.infrastructure.persistence.jpa")
@EnableJpaRepositories(basePackages = "com.pqvcf.governance.infrastructure.persistence.jpa")
public class DataGovernanceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataGovernanceApplication.class, args);
    }
}
