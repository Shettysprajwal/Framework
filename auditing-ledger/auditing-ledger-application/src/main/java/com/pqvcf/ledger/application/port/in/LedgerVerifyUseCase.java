package com.pqvcf.ledger.application.port.in;

import com.pqvcf.ledger.application.port.in.SealRecordUseCase.RecordDto;
import com.pqvcf.ledger.application.port.in.SealRecordUseCase.VerificationDto;
import java.util.List;

public interface LedgerVerifyUseCase {
    VerificationDto verifyIntegrity();
    List<RecordDto> listRecords();
    void tamperRecordAt(int index, String value);
    void reset();
}
