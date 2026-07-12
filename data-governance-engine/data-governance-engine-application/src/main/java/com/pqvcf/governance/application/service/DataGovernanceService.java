package com.pqvcf.governance.application.service;

import com.pqvcf.governance.application.port.in.EvaluateFlowUseCase;
import com.pqvcf.governance.application.port.in.GovernanceQueryUseCase;
import com.pqvcf.governance.application.port.out.AdequacyResolver;
import com.pqvcf.governance.application.port.out.LocalizationChecker;
import com.pqvcf.governance.domain.model.DataFlow;
import com.pqvcf.governance.domain.model.GovernanceDecision;
import com.pqvcf.governance.domain.model.TransferDecision;
import com.pqvcf.governance.domain.repository.DataGovernanceRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class DataGovernanceService implements EvaluateFlowUseCase, GovernanceQueryUseCase {

    private final AdequacyResolver adequacyResolver;
    private final LocalizationChecker localizationChecker;
    private final DataGovernanceRepository governanceRepository;

    public DataGovernanceService(
            AdequacyResolver adequacyResolver,
            LocalizationChecker localizationChecker,
            DataGovernanceRepository governanceRepository) {
        this.adequacyResolver = adequacyResolver;
        this.localizationChecker = localizationChecker;
        this.governanceRepository = governanceRepository;
    }

    @Override
    public FlowDecisionResponseDto evaluateFlow(EvaluateFlowCommand command) {
        DataFlow flow = new DataFlow(
                command.sourceCountry(),
                command.targetCountry(),
                command.dataCategory(),
                command.processingPurpose()
        );

        TransferDecision decision;
        String reasoning;
        List<String> citations = new ArrayList<>();
        String evidenceLink = "evidence-" + UUID.randomUUID().toString().substring(0, 8);

        // 1. Check for localization requirements (BLOCKED path)
        if (localizationChecker.isLocalizationMandated(flow.getSourceCountry(), flow.getDataCategory())) {
            decision = TransferDecision.BLOCKED;
            reasoning = String.format("Blocked: Local residency rules in %s mandate local storage for %s data.", 
                    flow.getSourceCountry(), flow.getDataCategory());
            citations.add("DPDP Section 16 / Localization Rules");
            citations.add("Russia FFDL No. 242-FZ");
            citations.add("China PIPL Article 38/40");
        } 
        // 2. Check for adequacy whitelists (APPROVED path)
        else if (adequacyResolver.checkAdequacy(flow.getSourceCountry(), flow.getTargetCountry())) {
            decision = TransferDecision.APPROVED;
            reasoning = String.format("Approved: Target country %s provides adequate safeguards relative to source country %s.",
                    flow.getTargetCountry(), flow.getSourceCountry());
            citations.add("GDPR Article 45 Adequacy Decision");
            citations.add("DPDP Chapter VI Whitelisting");
        } 
        // 3. Else, conditional approval (CONDITIONAL path)
        else {
            decision = TransferDecision.CONDITIONAL;
            reasoning = String.format("Conditional: Transfer from %s to %s requires Article 46 standard safeguards (SCCs/BCRs) and Z3 verification proofs.",
                    flow.getSourceCountry(), flow.getTargetCountry());
            citations.add("GDPR Article 46 Transfer Safeguards");
            citations.add("HIPAA Business Associate Agreement (BAA)");
        }

        GovernanceDecision govDecision = new GovernanceDecision(
                UUID.randomUUID().toString(),
                flow,
                decision,
                reasoning,
                citations,
                evidenceLink
        );

        governanceRepository.save(govDecision);

        return mapToResponse(govDecision);
    }

    @Override
    public List<FlowDecisionResponseDto> listAllDecisions() {
        return governanceRepository.listAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<FlowDecisionResponseDto> getDecisionDetails(String id) {
        return governanceRepository.findById(id).map(this::mapToResponse);
    }

    @Override
    public void deleteDecision(String id) {
        governanceRepository.deleteById(id);
    }

    private FlowDecisionResponseDto mapToResponse(GovernanceDecision d) {
        return new FlowDecisionResponseDto(
                d.getId(),
                d.getFlow().getSourceCountry(),
                d.getFlow().getTargetCountry(),
                d.getFlow().getDataCategory(),
                d.getFlow().getProcessingPurpose(),
                d.getDecision().name(),
                d.getReasoning(),
                d.getCitations(),
                d.getEvidenceLink(),
                d.getCreatedAt().toString()
        );
    }
}
