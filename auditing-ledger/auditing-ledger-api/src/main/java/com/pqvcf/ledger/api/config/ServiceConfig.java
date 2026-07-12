package com.pqvcf.ledger.api.config;

import com.pqvcf.ledger.application.port.out.LedgerHasher;
import com.pqvcf.ledger.application.service.AuditingLedgerService;
import com.pqvcf.ledger.domain.repository.AuditingLedgerRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceConfig {

    @Bean
    public AuditingLedgerService auditingLedgerService(
            LedgerHasher hasher,
            AuditingLedgerRepository repository) {
        return new AuditingLedgerService(hasher, repository);
    }
}
