package com.pqvcf.pqc.application.service;

import com.pqvcf.pqc.application.port.in.GenerateKeyUseCase;
import com.pqvcf.pqc.application.port.in.KmsKeyQueryUseCase;
import com.pqvcf.pqc.application.port.in.SignPayloadUseCase;
import com.pqvcf.pqc.application.port.out.PqcCryptoProvider;
import com.pqvcf.pqc.domain.model.PqcKeyPair;
import com.pqvcf.pqc.domain.model.PqcKeyType;
import com.pqvcf.pqc.domain.repository.PqcKmsRepository;
import com.pqvcf.shared.crypto.ByteArrayEncoder; // Or standard hex utils

import java.time.Instant;
import java.util.HexFormat; // Java 17+ Standard Hex
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PqcCryptographyService implements GenerateKeyUseCase, SignPayloadUseCase, KmsKeyQueryUseCase {

    private final PqcCryptoProvider cryptoProvider;
    private final PqcKmsRepository kmsRepository;

    public PqcCryptographyService(
            PqcCryptoProvider cryptoProvider,
            PqcKmsRepository kmsRepository) {
        this.cryptoProvider = cryptoProvider;
        this.kmsRepository = kmsRepository;
    }

    @Override
    public KeyResponseDto generateKey(GenerateKeyCommand command) {
        PqcKeyType type = PqcKeyType.valueOf(command.algorithm().toUpperCase().replace("-", "_"));
        
        PqcKeyPair pair = cryptoProvider.generatePqcKeyPair(type, command.alias());
        kmsRepository.save(pair);

        return mapToResponse(pair);
    }

    @Override
    public SignResponseDto sign(SignCommand command) {
        PqcKeyPair pair = kmsRepository.findByKeyId(command.keyId())
                .orElseThrow(() -> new IllegalArgumentException("Key not found in KMS vault: " + command.keyId()));

        byte[] payload = HexFormat.of().parseHex(command.payloadHex());
        byte[] sigBytes = cryptoProvider.sign(pair, payload);

        return new SignResponseDto(
                HexFormat.of().formatHex(sigBytes),
                pair.getKeyType().name(),
                pair.getKeyId(),
                sigBytes.length
        );
    }

    @Override
    public boolean verify(VerifyCommand command) {
        PqcKeyPair pair = kmsRepository.findByKeyId(command.keyId())
                .orElseThrow(() -> new IllegalArgumentException("Key not found in KMS vault: " + command.keyId()));

        byte[] payload = HexFormat.of().parseHex(command.payloadHex());
        byte[] signature = HexFormat.of().parseHex(command.signatureHex());

        return cryptoProvider.verify(pair, payload, signature);
    }

    @Override
    public List<KeyResponseDto> listAllKeys() {
        return kmsRepository.listAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<KeyResponseDto> getKeyDetails(String keyId) {
        return kmsRepository.findByKeyId(keyId).map(this::mapToResponse);
    }

    @Override
    public void deleteKey(String keyId) {
        kmsRepository.deleteByKeyId(keyId);
    }

    private KeyResponseDto mapToResponse(PqcKeyPair pair) {
        return new KeyResponseDto(
                pair.getKeyId(),
                pair.getKeyType().name(),
                HexFormat.of().formatHex(pair.getPublicKeyBytes()),
                pair.getCreatedAt(),
                pair.getExpiresAt()
        );
    }
}
