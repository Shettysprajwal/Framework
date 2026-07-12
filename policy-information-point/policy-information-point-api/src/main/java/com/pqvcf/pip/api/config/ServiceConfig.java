package com.pqvcf.pip.api.config;

import com.pqvcf.pip.application.service.AttributeResolutionService;
import com.pqvcf.pip.domain.resolver.AdequacyPathResolver;
import com.pqvcf.pip.domain.resolver.AttributeResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceConfig {

    @Bean
    public AttributeResolutionService attributeResolutionService(
            AttributeResolver attributeResolver,
            AdequacyPathResolver adequacyPathResolver) {
        return new AttributeResolutionService(attributeResolver, adequacyPathResolver);
    }
}
