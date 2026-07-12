package com.pqvcf.pqc.application.port.in;

import java.time.Instant;

public interface GenerateKeyUseCase {

    KeyResponseDto generateKey(GenerateKeyCommand command);

    record GenerateKeyCommand(
            String algorithm,
            String alias
    ) {}

    record KeyResponseDto(
            String keyId,
            String algorithm,
            String publicKeyHex,
            Instant createdAt,
            Instant expiresAt
    ) {}
}
