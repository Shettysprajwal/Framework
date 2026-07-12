package com.pqvcf.ledger.api.rest;

import com.pqvcf.ledger.application.port.in.LedgerVerifyUseCase;
import com.pqvcf.ledger.application.port.in.SealRecordUseCase;
import com.pqvcf.ledger.application.port.in.SealRecordUseCase.RecordDto;
import com.pqvcf.ledger.application.port.in.SealRecordUseCase.SealRecordCommand;
import com.pqvcf.ledger.application.port.in.SealRecordUseCase.VerificationDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ledger")
@Tag(name = "Compliance Auditing Ledger API", description = "Management endpoints for cryptographically sealing compliance logs, checking chain validation, and simulating data tampering")
public class AuditingLedgerController {

    private final SealRecordUseCase sealUseCase;
    private final LedgerVerifyUseCase verifyUseCase;

    public AuditingLedgerController(
            SealRecordUseCase sealUseCase,
            LedgerVerifyUseCase verifyUseCase) {
        this.sealUseCase = sealUseCase;
        this.verifyUseCase = verifyUseCase;
    }

    @PostMapping("/seal")
    @Operation(summary = "Seal a new compliance log entry and cryptographically bind it to the hash chain")
    public ResponseEntity<RecordDto> seal(@RequestBody SealRecordCommand command) {
        return ResponseEntity.ok(sealUseCase.seal(command));
    }

    @GetMapping("/records")
    @Operation(summary = "List all blocks registered in the auditing ledger")
    public ResponseEntity<List<RecordDto>> listRecords() {
        return ResponseEntity.ok(verifyUseCase.listRecords());
    }

    @GetMapping("/verify")
    @Operation(summary = "Verify the cryptographic integrity of the hash chain")
    public ResponseEntity<VerificationDto> verifyLedger() {
        return ResponseEntity.ok(verifyUseCase.verifyIntegrity());
    }

    @PostMapping("/tamper/{index}")
    @Operation(summary = "Simulate data tampering at index to test auditing alarms triggers")
    public ResponseEntity<Void> tamper(
            @PathVariable int index,
            @RequestParam String tamperedValue) {
        verifyUseCase.tamperRecordAt(index, tamperedValue);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset")
    @Operation(summary = "Clear ledger blocks logs history")
    public ResponseEntity<Void> reset() {
        verifyUseCase.reset();
        return ResponseEntity.ok().build();
    }
}
