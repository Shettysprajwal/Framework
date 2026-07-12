package com.pqvcf.governance.application.port.in;

import com.pqvcf.governance.application.port.in.EvaluateFlowUseCase.FlowDecisionResponseDto;
import java.util.List;
import java.util.Optional;

public interface GovernanceQueryUseCase {
    List<FlowDecisionResponseDto> listAllDecisions();
    Optional<FlowDecisionResponseDto> getDecisionDetails(String id);
    void deleteDecision(String id);
}
