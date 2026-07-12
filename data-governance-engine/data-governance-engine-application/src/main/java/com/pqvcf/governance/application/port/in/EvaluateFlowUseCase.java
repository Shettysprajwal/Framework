package com.pqvcf.governance.application.port.in;

import java.util.List;

public interface EvaluateFlowUseCase {

    FlowDecisionResponseDto evaluateFlow(EvaluateFlowCommand command);

    record EvaluateFlowCommand(
            String sourceCountry,
            String targetCountry,
            String dataCategory,
            String processingPurpose
    ) {}

    record FlowDecisionResponseDto(
            String decisionId,
            String sourceCountry,
            String targetCountry,
            String dataCategory,
            String processingPurpose,
            String decision, // APPROVED, BLOCKED, CONDITIONAL
            String reasoning,
            List<String> citations,
            String evidenceLink,
            String createdAt
    ) {}
}
