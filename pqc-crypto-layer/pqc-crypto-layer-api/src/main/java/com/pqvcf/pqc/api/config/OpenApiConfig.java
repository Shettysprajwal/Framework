package com.pqvcf.pqc.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("PQVCF — Post-Quantum Cryptography Layer API")
                        .version("1.0.0")
                        .description("REST and gRPC interfaces for generating ML-KEM, ML-DSA, and SLH-DSA keys, signing compliance bundles, and verifying signatures.")
                        .license(new License().name("Apache 2.0").url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
