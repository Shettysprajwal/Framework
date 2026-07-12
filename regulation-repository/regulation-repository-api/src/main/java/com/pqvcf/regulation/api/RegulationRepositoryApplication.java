package com.pqvcf.regulation.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@ComponentScan(basePackages = {"com.pqvcf.regulation", "com.pqvcf.shared"})
@EntityScan(basePackages = "com.pqvcf.regulation.infrastructure.persistence.jpa")
@EnableJpaRepositories(basePackages = "com.pqvcf.regulation.infrastructure.persistence.jpa")
public class RegulationRepositoryApplication {

    public static void main(String[] args) {
        SpringApplication.run(RegulationRepositoryApplication.class, args);
    }
}
