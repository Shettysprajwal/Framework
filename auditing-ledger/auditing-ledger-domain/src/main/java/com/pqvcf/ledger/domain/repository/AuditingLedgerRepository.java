package com.pqvcf.ledger.domain.repository;

import com.pqvcf.ledger.domain.model.AuditRecord;
import java.util.List;
import java.util.Optional;

/**
 * Output port interface for managing auditing blocks chain.
 *
 * @author PQVCF Research Team
 * @version 1.0.0
 */
public interface AuditingLedgerRepository {
    void append(AuditRecord record);
    List<AuditRecord> listAll();
    Optional<AuditRecord> getLatestRecord();
    void tamperRecord(int index, String tamperedValue);
    void reset();
}
