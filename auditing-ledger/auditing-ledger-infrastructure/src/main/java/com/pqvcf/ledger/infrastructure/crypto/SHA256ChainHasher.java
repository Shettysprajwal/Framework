package com.pqvcf.ledger.infrastructure.crypto;

import com.pqvcf.ledger.application.port.out.LedgerHasher;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Component
public class SHA256ChainHasher implements LedgerHasher {

    @Override
    public String calculateHash(
            String prevHash,
            String timestamp,
            String action,
            String actor,
            String target,
            String decision) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            
            // Concatenate all elements to establish cryptographical bindings
            String input = prevHash + timestamp + action + actor + target + decision;
            
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
            
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 hashing algorithm not available", e);
        }
    }
}
