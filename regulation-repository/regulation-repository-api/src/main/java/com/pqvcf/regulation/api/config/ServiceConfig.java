package com.pqvcf.regulation.api.config;

import com.pqvcf.regulation.application.service.RegulationService;
import com.pqvcf.regulation.domain.repository.RegulationRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceConfig {

    @Bean
    public RegulationService regulationService(RegulationRepository repository) {
        return new RegulationService(repository);
    }
}
