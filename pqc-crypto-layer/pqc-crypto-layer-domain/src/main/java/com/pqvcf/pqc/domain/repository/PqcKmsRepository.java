package com.pqvcf.pqc.domain.repository;

import com.pqvcf.pqc.domain.model.PqcKeyPair;
import java.util.List;
import java.util.Optional;

/**
 * Output port interface for managing PQC keys in KMS storage.
 *
 * @author PQVCF Research Team
 * @version 1.0.0
 */
public interface PqcKmsRepository {
    void save(PqcKeyPair keyPair);
    Optional<PqcKeyPair> findByKeyId(String keyId);
    List<PqcKeyPair> listAll();
    void deleteByKeyId(String keyId);
}
