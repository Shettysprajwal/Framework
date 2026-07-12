package com.pqvcf.pdp.application.port.in;

import com.pqvcf.pdp.domain.repository.PolicyDecisionRepository.DecisionAuditLog;
import java.util.List;

public interface EvaluateRequestUseCase {

    EvaluateResponse evaluate(EvaluateCommand command);
    List<DecisionAuditLog> listAuditLogs();

    record EvaluateCommand(
            String subjectId,
            String resourceId,
            String actionId,
            String sourceCountry,
            String targetCountry,
            String policyName
    ) {}

    record EvaluateResponse(
            String effect,
            String proofTrace,
            String validationLog,
            String solvedAt
    ) {}
}
