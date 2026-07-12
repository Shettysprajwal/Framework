package com.pqvcf.pqc.api.config;

import com.pqvcf.pqc.application.port.out.PqcCryptoProvider;
import com.pqvcf.pqc.application.service.PqcCryptographyService;
import com.pqvcf.pqc.domain.repository.PqcKmsRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceConfig {

    @Bean
    public PqcCryptographyService pqcCryptographyService(
            PqcCryptoProvider cryptoProvider,
            PqcKmsRepository kmsRepository) {
        return new PqcCryptographyService(cryptoProvider, kmsRepository);
    }
}
