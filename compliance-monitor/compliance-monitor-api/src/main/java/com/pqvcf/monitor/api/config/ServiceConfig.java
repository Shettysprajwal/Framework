package com.pqvcf.monitor.api.config;

import com.pqvcf.monitor.application.port.out.GovernanceClientProvider;
import com.pqvcf.monitor.application.service.ComplianceMonitoringService;
import com.pqvcf.monitor.domain.repository.ComplianceMonitorRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceConfig {

    @Bean
    public ComplianceMonitoringService complianceMonitoringService(
            GovernanceClientProvider governanceClient,
            ComplianceMonitorRepository monitorRepository) {
        return new ComplianceMonitoringService(governanceClient, monitorRepository);
    }
}
