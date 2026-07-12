package com.pqvcf.pqc.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {"com.pqvcf.pqc", "com.pqvcf.shared"})
@EntityScan(basePackages = "com.pqvcf.pqc.infrastructure.persistence.jpa")
@EnableJpaRepositories(basePackages = "com.pqvcf.pqc.infrastructure.persistence.jpa")
public class PqcCryptoApplication {

    public static void main(String[] args) {
        SpringApplication.run(PqcCryptoApplication.class, args);
    }
}
