package com.pqvcf.governance.infrastructure.persistence;

import com.pqvcf.governance.domain.model.GovernanceDecision;
import com.pqvcf.governance.domain.repository.DataGovernanceRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryDataGovernanceRepositoryAdapter implements DataGovernanceRepository {

    private final Map<String, GovernanceDecision> ledger = new ConcurrentHashMap<>();

    @Override
    public void save(GovernanceDecision decision) {
        if (decision == null) return;
        ledger.put(decision.getId(), decision);
    }

    @Override
    public Optional<GovernanceDecision> findById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(ledger.get(id));
    }

    @Override
    public List<GovernanceDecision> listAll() {
        return new ArrayList<>(ledger.values());
    }

    @Override
    public void deleteById(String id) {
        if (id == null) return;
        ledger.remove(id);
    }
}
