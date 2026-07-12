package com.pqvcf.governance.api.config;

import com.pqvcf.governance.application.port.out.AdequacyResolver;
import com.pqvcf.governance.application.port.out.LocalizationChecker;
import com.pqvcf.governance.application.service.DataGovernanceService;
import com.pqvcf.governance.domain.repository.DataGovernanceRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceConfig {

    @Bean
    public DataGovernanceService dataGovernanceService(
            AdequacyResolver adequacyResolver,
            LocalizationChecker localizationChecker,
            DataGovernanceRepository governanceRepository) {
        return new DataGovernanceService(adequacyResolver, localizationChecker, governanceRepository);
    }
}
