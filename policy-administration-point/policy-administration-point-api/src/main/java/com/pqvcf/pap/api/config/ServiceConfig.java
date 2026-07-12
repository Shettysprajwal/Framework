package com.pqvcf.pap.api.config;

import com.pqvcf.pap.application.service.PolicyAdministrationService;
import com.pqvcf.pap.domain.repository.PolicyRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceConfig {

    @Bean
    public PolicyAdministrationService policyAdministrationService(PolicyRepository repository) {
        return new PolicyAdministrationService(repository);
    }
}
