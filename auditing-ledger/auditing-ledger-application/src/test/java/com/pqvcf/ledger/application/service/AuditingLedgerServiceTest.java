package com.pqvcf.ledger.application.service;

import com.pqvcf.ledger.application.port.in.SealRecordUseCase.RecordDto;
import com.pqvcf.ledger.application.port.in.SealRecordUseCase.SealRecordCommand;
import com.pqvcf.ledger.application.port.in.SealRecordUseCase.VerificationDto;
import com.pqvcf.ledger.application.port.out.LedgerHasher;
import com.pqvcf.ledger.domain.model.AuditRecord;
import com.pqvcf.ledger.domain.repository.AuditingLedgerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditingLedgerServiceTest {

    @Mock
    private LedgerHasher hasher;

    @Mock
    private AuditingLedgerRepository repository;

    private AuditingLedgerService service;

    @BeforeEach
    void setUp() {
        service = new AuditingLedgerService(hasher, repository);
    }

    @Test
    @DisplayName("Should successfully seal a record with correct parent linkage previous hash")
    void shouldSealRecord() {
        SealRecordCommand command = new SealRecordCommand("EVALUATE", "ADMIN", "Policy-1", "PERMIT");

        when(repository.getLatestRecord()).thenReturn(Optional.empty());
        when(hasher.calculateHash(eq("GENESIS_HASH"), anyString(), eq("EVALUATE"), eq("ADMIN"), eq("Policy-1"), eq("PERMIT")))
                .thenReturn("calculated-hash");

        RecordDto dto = service.seal(command);

        assertThat(dto).isNotNull();
        assertThat(dto.previousHash()).isEqualTo("GENESIS_HASH");
        assertThat(dto.currentHash()).isEqualTo("calculated-hash");

        verify(repository).append(any(AuditRecord.class));
    }

    @Test
    @DisplayName("Should successfully verify chain validation when hashes match expected values")
    void shouldVerifyValidLedger() {
        AuditRecord record = new AuditRecord("1", null, "EVALUATE", "ADMIN", "P-1", "PERMIT", "GENESIS_HASH", "current-hash");
        when(repository.listAll()).thenReturn(List.of(record));
        when(hasher.calculateHash(eq("GENESIS_HASH"), anyString(), eq("EVALUATE"), eq("ADMIN"), eq("P-1"), eq("PERMIT")))
                .thenReturn("current-hash");

        VerificationDto dto = service.verifyIntegrity();

        assertThat(dto.valid()).isTrue();
        assertThat(dto.tamperedIndex()).isEqualTo(-1);
    }
}
