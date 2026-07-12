package com.pqvcf.pqc.application.port.in;

import com.pqvcf.pqc.application.port.in.GenerateKeyUseCase.KeyResponseDto;
import java.util.List;
import java.util.Optional;

public interface KmsKeyQueryUseCase {
    List<KeyResponseDto> listAllKeys();
    Optional<KeyResponseDto> getKeyDetails(String keyId);
    void deleteKey(String keyId);
}
