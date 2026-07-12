package com.pqvcf.translation.api.config;

import com.pqvcf.translation.application.service.RuleTranslationService;
import com.pqvcf.translation.domain.parser.DeonticParser;
import com.pqvcf.translation.domain.repository.RuleTranslationRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceConfig {

    @Bean
    public RuleTranslationService ruleTranslationService(
            RuleTranslationRepository repository,
            DeonticParser cnlParser) {
        return new RuleTranslationService(repository, cnlParser);
    }
}
