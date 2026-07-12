package com.pqvcf.zkp.application.port.in;

public interface VerifyProofUseCase {

    boolean verifyProof(VerifyProofCommand command);

    record VerifyProofCommand(
            String proofId,
            String challengeHex,
            String responseHex,
            String commitmentHex
    ) {}
}
