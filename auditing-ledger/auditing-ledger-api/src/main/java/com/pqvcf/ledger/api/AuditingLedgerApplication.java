package com.pqvcf.ledger.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {"com.pqvcf.ledger", "com.pqvcf.shared"})
@EntityScan(basePackages = "com.pqvcf.ledger.infrastructure.persistence.jpa")
@EnableJpaRepositories(basePackages = "com.pqvcf.ledger.infrastructure.persistence.jpa")
public class AuditingLedgerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuditingLedgerApplication.class, args);
    }
}
