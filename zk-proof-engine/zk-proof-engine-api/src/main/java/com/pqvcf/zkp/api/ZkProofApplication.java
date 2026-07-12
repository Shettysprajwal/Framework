package com.pqvcf.zkp.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {"com.pqvcf.zkp", "com.pqvcf.shared"})
@EntityScan(basePackages = "com.pqvcf.zkp.infrastructure.persistence.jpa")
@EnableJpaRepositories(basePackages = "com.pqvcf.zkp.infrastructure.persistence.jpa")
public class ZkProofApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZkProofApplication.class, args);
    }
}
