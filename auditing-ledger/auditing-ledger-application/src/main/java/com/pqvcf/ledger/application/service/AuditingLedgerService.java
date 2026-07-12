package com.pqvcf.ledger.application.service;

import com.pqvcf.ledger.application.port.in.LedgerVerifyUseCase;
import com.pqvcf.ledger.application.port.in.SealRecordUseCase;
import com.pqvcf.ledger.application.port.out.LedgerHasher;
import com.pqvcf.ledger.domain.model.AuditRecord;
import com.pqvcf.ledger.domain.repository.AuditingLedgerRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class AuditingLedgerService implements SealRecordUseCase, LedgerVerifyUseCase {

    private final LedgerHasher hasher;
    private final AuditingLedgerRepository repository;

    public AuditingLedgerService(LedgerHasher hasher, AuditingLedgerRepository repository) {
        this.hasher = hasher;
        this.repository = repository;
    }

    @Override
    public RecordDto seal(SealRecordCommand command) {
        Optional<AuditRecord> latest = repository.getLatestRecord();
        String prevHash = latest.map(AuditRecord::getCurrentHash).orElse("GENESIS_HASH");

        Instant timestamp = Instant.now();

        String currHash = hasher.calculateHash(
                prevHash,
                timestamp.toString(),
                command.action(),
                command.actor(),
                command.target(),
                command.decision()
        );

        AuditRecord record = new AuditRecord(
                UUID.randomUUID().toString(),
                timestamp,
                command.action(),
                command.actor(),
                command.target(),
                command.decision(),
                prevHash,
                currHash
        );

        repository.append(record);

        return mapToDto(record);
    }

    @Override
    public VerificationDto verifyIntegrity() {
        List<AuditRecord> chain = repository.listAll();
        
        String expectedPrevHash = "GENESIS_HASH";

        for (int i = 0; i < chain.size(); i++) {
            AuditRecord node = chain.get(i);

            // 1. Verify links mapping consistency
            if (!node.getPreviousHash().equals(expectedPrevHash)) {
                return new VerificationDto(
                        false,
                        i,
                        String.format("Hash chain broken: Record at index %d expects previous hash '%s' but parent block has '%s'",
                                i, node.getPreviousHash(), expectedPrevHash)
                );
            }

            // 2. Recalculate node digest to check tampering
            String calculatedHash = hasher.calculateHash(
                    node.getPreviousHash(),
                    node.getTimestamp().toString(),
                    node.getAction(),
                    node.getActor(),
                    node.getTarget(),
                    node.getDecision()
            );

            if (!node.getCurrentHash().equals(calculatedHash)) {
                return new VerificationDto(
                        false,
                        i,
                        String.format("Tampered data detected at index %d: Stored hash is '%s' but recalculated data yields '%s'",
                                i, node.getCurrentHash(), calculatedHash)
                );
            }

            expectedPrevHash = node.getCurrentHash();
        }

        return new VerificationDto(true, -1, "Ledger integrity validated: Hash chain is unbroken and cryptographically secure.");
    }

    @Override
    public List<RecordDto> listRecords() {
        return repository.listAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public void tamperRecordAt(int index, String value) {
        repository.tamperRecord(index, value);
    }

    @Override
    public void reset() {
        repository.reset();
    }

    private RecordDto mapToDto(AuditRecord r) {
        return new RecordDto(
                r.getId(),
                r.getTimestamp().toString(),
                r.getAction(),
                r.getActor(),
                r.getTarget(),
                r.getDecision(),
                r.getPreviousHash(),
                r.getCurrentHash()
        );
    }
}
