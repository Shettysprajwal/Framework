package com.pqvcf.zkp.api.config;

import com.pqvcf.zkp.application.port.out.ZkProofProvider;
import com.pqvcf.zkp.application.service.ZkProofService;
import com.pqvcf.zkp.domain.repository.ZkProofRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceConfig {

    @Bean
    public ZkProofService zkProofService(
            ZkProofProvider proofProvider,
            ZkProofRepository proofRepository) {
        return new ZkProofService(proofProvider, proofRepository);
    }
}
