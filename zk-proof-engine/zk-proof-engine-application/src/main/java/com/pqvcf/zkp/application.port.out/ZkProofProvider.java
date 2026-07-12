package com.pqvcf.zkp.application.port.out;

import com.pqvcf.zkp.domain.model.PedersenCommitment;
import com.pqvcf.zkp.domain.model.ZkProof;
import com.pqvcf.zkp.domain.model.ZkProofType;

public interface ZkProofProvider {
    
    PedersenCommitment commitPedersen(long secretValue);

    ZkProof proveSigma(
            ZkProofType type,
            PedersenCommitment commitment,
            String publicInputsJson
    );

    boolean verifySigma(ZkProof proof);
}
