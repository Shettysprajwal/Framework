package com.pqvcf.pdp.application.service;

import com.pqvcf.pdp.application.port.in.EvaluateRequestUseCase;
import com.pqvcf.pdp.application.port.out.PapPolicyProvider;
import com.pqvcf.pdp.application.port.out.PipAttributeProvider;
import com.pqvcf.pdp.application.port.out.RegulationRuleProvider;
import com.pqvcf.pdp.domain.model.DecisionEffect;
import com.pqvcf.pdp.domain.model.DecisionRequest;
import com.pqvcf.pdp.domain.model.DecisionResult;
import com.pqvcf.pdp.domain.repository.PolicyDecisionRepository;
import com.pqvcf.pdp.domain.repository.PolicyDecisionRepository.DecisionAuditLog;
import com.pqvcf.pdp.domain.solver.SmtComplianceSolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class PolicyDecisionService implements EvaluateRequestUseCase {

    private static final Logger log = LoggerFactory.getLogger(PolicyDecisionService.class);

    private final PapPolicyProvider papProvider;
    private final PipAttributeProvider pipProvider;
    private final RegulationRuleProvider ruleProvider;
    private final SmtComplianceSolver solver;
    private final PolicyDecisionRepository auditRepository;

    public PolicyDecisionService(
            PapPolicyProvider papProvider,
            PipAttributeProvider pipProvider,
            RegulationRuleProvider ruleProvider,
            SmtComplianceSolver solver,
            PolicyDecisionRepository auditRepository) {
        this.papProvider = papProvider;
        this.pipProvider = pipProvider;
        this.ruleProvider = ruleProvider;
        this.solver = solver;
        this.auditRepository = auditRepository;
    }

    @Override
    public EvaluateResponse evaluate(EvaluateCommand command) {
        log.info("PDP evaluating access request for subject: {}, resource: {}", command.subjectId(), command.resourceId());

        DecisionRequest request = new DecisionRequest(
                command.subjectId(),
                command.resourceId(),
                command.actionId(),
                command.sourceCountry(),
                command.targetCountry(),
                command.policyName()
        );

        // 1. Fetch active policies from Module 3 PAP
        List<PapPolicyProvider.ActivePolicyDto> policies = papProvider.fetchActivePolicies();
        if (policies.isEmpty()) {
            DecisionResult result = new DecisionResult(
                    DecisionEffect.INDETERMINATE,
                    "; No active compliance policies configured in PAP",
                    "Indeterminate: No policies matching the scope."
            );
            auditRepository.logDecision(request, result);
            return mapToResponse(result);
        }

        // 2. Resolve request context attributes from Module 4 PIP
        PipAttributeProvider.ResolvedContextDto context = pipProvider.resolveContext(
                command.subjectId(),
                command.resourceId(),
                command.actionId(),
                command.sourceCountry(),
                command.targetCountry()
        );

        // 3. Compile SMT-LIB2 formula assertions
        String smtFormula = compileSmtFormula(request, context, policies);

        // 4. Invoke Z3 solver to verify satisfiability
        DecisionResult result = solver.solve(smtFormula);

        // 5. Log verification audit trail
        auditRepository.logDecision(request, result);

        return mapToResponse(result);
    }

    @Override
    public List<DecisionAuditLog> listAuditLogs() {
        return auditRepository.fetchAllAuditLogs();
    }

    private String compileSmtFormula(
            DecisionRequest request,
            PipAttributeProvider.ResolvedContextDto context,
            List<PapPolicyProvider.ActivePolicyDto> policies) {

        StringBuilder smt = new StringBuilder();
        smt.append("; PQVCF Auto-Generated SMT-LIB2 Compliance Verification Model\n");
        
        // Declare standard constants
        smt.append("(declare-const subject String)\n");
        smt.append("(declare-const resource String)\n");
        smt.append("(declare-const action String)\n");
        smt.append("(declare-const source_country String)\n");
        smt.append("(declare-const target_country String)\n");
        smt.append("(declare-const transitive_adequate Bool)\n");

        // Assert request context variables
        smt.append(String.format("(assert (= subject \"%s\"))\n", request.getSubjectId()));
        smt.append(String.format("(assert (= resource \"%s\"))\n", request.getResourceId()));
        smt.append(String.format("(assert (= action \"%s\"))\n", request.getActionId()));
        smt.append(String.format("(assert (= source_country \"%s\"))\n", request.getSourceCountry()));
        smt.append(String.format("(assert (= target_country \"%s\"))\n", request.getTargetCountry()));
        smt.append(String.format("(assert (= transitive_adequate %b))\n", context.isTransitiveAdequate()));

        // Assert typed attributes resolved by PIP
        for (PipAttributeProvider.AttributeDto attr : context.attributes()) {
            if ("Boolean".equalsIgnoreCase(attr.dataType())) {
                smt.append(String.format("(declare-const %s Bool)\n", attr.key()));
                smt.append(String.format("(assert (= %s %s))\n", attr.key(), attr.value()));
            } else if ("Integer".equalsIgnoreCase(attr.dataType())) {
                smt.append(String.format("(declare-const %s Int)\n", attr.key()));
                smt.append(String.format("(assert (= %s %s))\n", attr.key(), attr.value()));
            } else {
                smt.append(String.format("(declare-const %s String)\n", attr.key()));
                smt.append(String.format("(assert (= %s \"%s\"))\n", attr.key(), attr.value()));
            }
        }

        // Fetch and append all linked regulatory rule SMT constraints
        smt.append("\n; ---- Regulatory Rules Constraints ----\n");
        for (PapPolicyProvider.ActivePolicyDto policy : policies) {
            // If target policy filter is specified, skip non-matching policies
            if (!request.getPolicyName().isBlank() && !policy.name().equalsIgnoreCase(request.getPolicyName())) {
                continue;
            }

            for (PapPolicyProvider.ActiveRuleLinkDto link : policy.ruleLinks()) {
                String smtSpec = ruleProvider.fetchSmtSpec(link.regulatoryRuleId());
                if (smtSpec != null && !smtSpec.isBlank()) {
                    smt.append(String.format("; Linked Rule: %s\n", link.organizationalRuleName()));
                    smt.append(smtSpec).append("\n");
                }
            }
        }

        smt.append("\n(check-sat)\n");
        smt.append("(get-model)\n");

        return smt.toString();
    }

    private EvaluateResponse mapToResponse(DecisionResult result) {
        return new EvaluateResponse(
                result.getEffect().name(),
                result.getProofTrace(),
                result.getValidationLog(),
                result.getSolvedAt().toString()
        );
    }
}
