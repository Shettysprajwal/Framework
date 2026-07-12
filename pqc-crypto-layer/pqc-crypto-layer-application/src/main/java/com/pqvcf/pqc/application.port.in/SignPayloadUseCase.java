package com.pqvcf.pqc.application.port.in;

public interface SignPayloadUseCase {

    SignResponseDto sign(SignCommand command);
    boolean verify(VerifyCommand command);

    record SignCommand(
            String keyId,
            String payloadHex
    ) {}

    record VerifyCommand(
            String keyId,
            String payloadHex,
            String signatureHex
    ) {}

    record SignResponseDto(
            String signatureHex,
            String algorithm,
            String keyId,
            int length
    ) {}
}
