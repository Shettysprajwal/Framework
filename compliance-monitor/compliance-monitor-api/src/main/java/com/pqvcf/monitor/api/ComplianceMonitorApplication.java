package com.pqvcf.monitor.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {"com.pqvcf.monitor", "com.pqvcf.shared"})
@EntityScan(basePackages = "com.pqvcf.monitor.infrastructure.persistence.jpa")
@EnableJpaRepositories(basePackages = "com.pqvcf.monitor.infrastructure.persistence.jpa")
public class ComplianceMonitorApplication {

    public static void main(String[] args) {
        SpringApplication.run(ComplianceMonitorApplication.class, args);
    }
}
