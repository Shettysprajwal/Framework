package com.pqvcf.pqc.infrastructure.persistence;

import com.pqvcf.pqc.domain.model.PqcKeyPair;
import com.pqvcf.pqc.domain.repository.PqcKmsRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryPqcKmsRepositoryAdapter implements PqcKmsRepository {

    private final Map<String, PqcKeyPair> vault = new ConcurrentHashMap<>();

    @Override
    public void save(PqcKeyPair keyPair) {
        if (keyPair == null) return;
        vault.put(keyPair.getKeyId(), keyPair);
    }

    @Override
    public Optional<PqcKeyPair> findByKeyId(String keyId) {
        if (keyId == null) return Optional.empty();
        return Optional.ofNullable(vault.get(keyId));
    }

    @Override
    public List<PqcKeyPair> listAll() {
        return new ArrayList<>(vault.values());
    }

    @Override
    public void deleteByKeyId(String keyId) {
        if (keyId == null) return;
        vault.remove(keyId);
    }
}
