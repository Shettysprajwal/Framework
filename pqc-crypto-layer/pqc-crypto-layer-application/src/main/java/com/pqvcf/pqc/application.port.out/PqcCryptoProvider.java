package com.pqvcf.pqc.application.port.out;

import com.pqvcf.pqc.domain.model.PqcKeyPair;
import com.pqvcf.pqc.domain.model.PqcKeyType;

public interface PqcCryptoProvider {
    
    PqcKeyPair generatePqcKeyPair(PqcKeyType type, String alias);

    byte[] sign(PqcKeyPair keyPair, byte[] payload);

    boolean verify(PqcKeyPair keyPair, byte[] payload, byte[] signature);
}
