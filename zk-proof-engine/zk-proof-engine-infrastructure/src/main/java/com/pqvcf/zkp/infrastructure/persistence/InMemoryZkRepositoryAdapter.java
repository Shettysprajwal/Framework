package com.pqvcf.zkp.infrastructure.persistence;

import com.pqvcf.zkp.domain.model.ZkProof;
import com.pqvcf.zkp.domain.repository.ZkProofRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryZkRepositoryAdapter implements ZkProofRepository {

    private final Map<String, ZkProof> ledger = new ConcurrentHashMap<>();

    @Override
    public void save(ZkProof proof) {
        if (proof == null) return;
        ledger.put(proof.getProofId(), proof);
    }

    @Override
    public Optional<ZkProof> findByProofId(String proofId) {
        if (proofId == null) return Optional.empty();
        return Optional.ofNullable(ledger.get(proofId));
    }

    @Override
    public List<ZkProof> listAll() {
        return new ArrayList<>(ledger.values());
    }

    @Override
    public void deleteByProofId(String proofId) {
        if (proofId == null) return;
        ledger.remove(proofId);
    }
}
