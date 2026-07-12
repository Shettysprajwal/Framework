package com.pqvcf.pdp.infrastructure.persistence.jpa;

import com.pqvcf.pdp.domain.model.DecisionRequest;
import com.pqvcf.pdp.domain.model.DecisionResult;
import com.pqvcf.pdp.domain.repository.PolicyDecisionRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class LoggingPolicyDecisionRepositoryAdapter implements PolicyDecisionRepository {

    private final SpringDataPolicyDecisionRepository repository;

    public LoggingPolicyDecisionRepositoryAdapter(SpringDataPolicyDecisionRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void logDecision(DecisionRequest request, DecisionResult result) {
        DecisionAuditLogJpaEntity entity = new DecisionAuditLogJpaEntity();
        entity.setId(UUID.randomUUID());
        entity.setSubjectId(request.getSubjectId());
        entity.setResourceId(request.getResourceId());
        entity.setActionId(request.getActionId());
        entity.setSourceCountry(request.getSourceCountry());
        entity.setTargetCountry(request.getTargetCountry());
        entity.setPolicyName(request.getPolicyName());
        entity.setEffect(result.getEffect().name());
        entity.setProofTrace(result.getProofTrace());
        entity.setValidationLog(result.getValidationLog());
        entity.setSolvedAt(result.getSolvedAt());

        repository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DecisionAuditLog> fetchAllAuditLogs() {
        return repository.findAll(Sort.by(Sort.Direction.DESC, "solvedAt")).stream()
                .map(entity -> new DecisionAuditLog(
                        entity.getId().toString(),
                        entity.getSubjectId(),
                        entity.getResourceId(),
                        entity.getActionId(),
                        entity.getSourceCountry(),
                        entity.getTargetCountry(),
                        entity.getPolicyName(),
                        entity.getEffect(),
                        entity.getProofTrace(),
                        entity.getValidationLog(),
                        entity.getSolvedAt().toString()
                )).collect(Collectors.toList());
    }
}
