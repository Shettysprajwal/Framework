package com.pqvcf.zkp.application.port.in;

import com.pqvcf.zkp.application.port.in.GenerateProofUseCase.ProofResponseDto;
import java.util.List;
import java.util.Optional;

public interface ZkProofQueryUseCase {
    List<ProofResponseDto> listAllProofs();
    Optional<ProofResponseDto> getProofDetails(String proofId);
    void deleteProof(String proofId);
}
