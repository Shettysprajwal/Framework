package com.pqvcf.pdp.api.config;

import com.pqvcf.pdp.application.port.out.PapPolicyProvider;
import com.pqvcf.pdp.application.port.out.PipAttributeProvider;
import com.pqvcf.pdp.application.port.out.RegulationRuleProvider;
import com.pqvcf.pdp.application.service.PolicyDecisionService;
import com.pqvcf.pdp.domain.repository.PolicyDecisionRepository;
import com.pqvcf.pdp.domain.solver.SmtComplianceSolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceConfig {

    @Bean
    public PolicyDecisionService policyDecisionService(
            PapPolicyProvider papProvider,
            PipAttributeProvider pipProvider,
            RegulationRuleProvider ruleProvider,
            SmtComplianceSolver solver,
            PolicyDecisionRepository auditRepository) {
        return new PolicyDecisionService(papProvider, pipProvider, ruleProvider, solver, auditRepository);
    }
}
