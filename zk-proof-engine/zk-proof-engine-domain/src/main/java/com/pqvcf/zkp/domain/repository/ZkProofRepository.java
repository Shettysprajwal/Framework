package com.pqvcf.zkp.domain.repository;

import com.pqvcf.zkp.domain.model.ZkProof;
import java.util.List;
import java.util.Optional;

/**
 * Output port interface for managing ZK proofs in database audits logs.
 *
 * @author PQVCF Research Team
 * @version 1.0.0
 */
public interface ZkProofRepository {
    void save(ZkProof proof);
    Optional<ZkProof> findByProofId(String proofId);
    List<ZkProof> listAll();
    void deleteByProofId(String proofId);
}
