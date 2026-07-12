package com.pqvcf.ledger.infrastructure.persistence;

import com.pqvcf.ledger.domain.model.AuditRecord;
import com.pqvcf.ledger.domain.repository.AuditingLedgerRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class InMemoryAuditingLedgerRepositoryAdapter implements AuditingLedgerRepository {

    private final List<AuditRecord> blocks = new CopyOnWriteArrayList<>();

    @Override
    public void append(AuditRecord record) {
        if (record == null) return;
        blocks.add(record);
    }

    @Override
    public List<AuditRecord> listAll() {
        return new ArrayList<>(blocks);
    }

    @Override
    public Optional<AuditRecord> getLatestRecord() {
        if (blocks.isEmpty()) return Optional.empty();
        return Optional.of(blocks.get(blocks.size() - 1));
    }

    @Override
    public void tamperRecord(int index, String tamperedValue) {
        if (index < 0 || index >= blocks.size()) {
            throw new IllegalArgumentException("Index out of ledger bounds: " + index);
        }
        AuditRecord record = blocks.get(index);
        record.tamperDecision(tamperedValue);
    }

    @Override
    public void reset() {
        blocks.clear();
    }
}
