package com.pqvcf.ledger.application.port.in;

import java.util.List;

public interface SealRecordUseCase {

    RecordDto seal(SealRecordCommand command);

    record SealRecordCommand(
            String action,
            String actor,
            String target,
            String decision
    ) {}

    record RecordDto(
            String id,
            String timestamp,
            String action,
            String actor,
            String target,
            String decision,
            String previousHash,
            String currentHash
    ) {}

    record VerificationDto(
            boolean valid,
            int tamperedIndex,
            String details
    ) {}
}
