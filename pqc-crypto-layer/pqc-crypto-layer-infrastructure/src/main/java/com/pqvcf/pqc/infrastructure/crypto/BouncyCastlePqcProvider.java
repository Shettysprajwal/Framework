package com.pqvcf.pqc.infrastructure.crypto;

import com.pqvcf.pqc.domain.model.PqcKeyPair;
import com.pqvcf.pqc.domain.model.PqcKeyType;
import com.pqvcf.pqc.application.port.out.PqcCryptoProvider;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.pqc.crypto.crystals.dilithium.*;
import org.bouncycastle.pqc.crypto.crystals.kyber.*;
import org.bouncycastle.pqc.crypto.sphincsplus.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Component
public class BouncyCastlePqcProvider implements PqcCryptoProvider {

    private static final Logger log = LoggerFactory.getLogger(BouncyCastlePqcProvider.class);
    private final SecureRandom random = new SecureRandom();

    @Override
    public PqcKeyPair generatePqcKeyPair(PqcKeyType type, String alias) {
        log.info("Generating post-quantum key pair for type: {}, alias: {}", type, alias);
        
        String keyId = (alias != null && !alias.isBlank()) ? alias.trim() : UUID.randomUUID().toString();
        byte[] pubBytes;
        byte[] privBytes;

        if (type == PqcKeyType.ML_KEM_768) {
            KyberKeyPairGenerator generator = new KyberKeyPairGenerator();
            generator.init(new KyberKeyGenerationParameters(random, KyberParameters.kyber768));
            AsymmetricCipherKeyPair pair = generator.generateKeyPair();
            
            KyberPublicKeyParameters pub = (KyberPublicKeyParameters) pair.getPublic();
            KyberPrivateKeyParameters priv = (KyberPrivateKeyParameters) pair.getPrivate();
            
            pubBytes = pub.getEncoded();
            privBytes = priv.getEncoded();

        } else if (type == PqcKeyType.ML_DSA_65) {
            DilithiumKeyPairGenerator generator = new DilithiumKeyPairGenerator();
            generator.init(new DilithiumKeyGenerationParameters(random, DilithiumParameters.dilithium3));
            AsymmetricCipherKeyPair pair = generator.generateKeyPair();

            DilithiumPublicKeyParameters pub = (DilithiumPublicKeyParameters) pair.getPublic();
            DilithiumPrivateKeyParameters priv = (DilithiumPrivateKeyParameters) pair.getPrivate();

            pubBytes = pub.getEncoded();
            privBytes = priv.getEncoded();

        } else if (type == PqcKeyType.SLH_DSA_256) {
            SPHINCSPlusKeyPairGenerator generator = new SPHINCSPlusKeyPairGenerator();
            generator.init(new SPHINCSPlusKeyGenerationParameters(random, SPHINCSPlusParameters.sha2_256f));
            AsymmetricCipherKeyPair pair = generator.generateKeyPair();

            SPHINCSPlusPublicKeyParameters pub = (SPHINCSPlusPublicKeyParameters) pair.getPublic();
            SPHINCSPlusPrivateKeyParameters priv = (SPHINCSPlusPrivateKeyParameters) pair.getPrivate();

            pubBytes = pub.getEncoded();
            privBytes = priv.getEncoded();

        } else {
            throw new IllegalArgumentException("Unsupported PQC algorithm: " + type);
        }

        Instant created = Instant.now();
        Instant expires = created.plus(90, ChronoUnit.DAYS); // rotated keys

        return new PqcKeyPair(keyId, type, pubBytes, privBytes, created, expires);
    }

    @Override
    public byte[] sign(PqcKeyPair keyPair, byte[] payload) {
        log.info("Generating PQC digital signature with key: {}", keyPair.getKeyId());
        
        if (keyPair.getKeyType() == PqcKeyType.ML_DSA_65) {
            DilithiumPrivateKeyParameters privParams = new DilithiumPrivateKeyParameters(
                    DilithiumParameters.dilithium3,
                    keyPair.getPrivateKeyBytes()
            );
            DilithiumSigner signer = new DilithiumSigner();
            signer.init(true, privParams);
            return signer.generateSignature(payload);

        } else if (keyPair.getKeyType() == PqcKeyType.SLH_DSA_256) {
            SPHINCSPlusPrivateKeyParameters privParams = new SPHINCSPlusPrivateKeyParameters(
                    SPHINCSPlusParameters.sha2_256f,
                    keyPair.getPrivateKeyBytes()
            );
            SPHINCSPlusSigner signer = new SPHINCSPlusSigner();
            signer.init(true, privParams);
            return signer.generateSignature(payload);
        }

        throw new UnsupportedOperationException("Key type does not support digital signing: " + keyPair.getKeyType());
    }

    @Override
    public boolean verify(PqcKeyPair keyPair, byte[] payload, byte[] signature) {
        log.info("Verifying PQC signature with key: {}", keyPair.getKeyId());

        try {
            if (keyPair.getKeyType() == PqcKeyType.ML_DSA_65) {
                DilithiumPublicKeyParameters pubParams = new DilithiumPublicKeyParameters(
                        DilithiumParameters.dilithium3,
                        keyPair.getPublicKeyBytes()
                );
                DilithiumSigner signer = new DilithiumSigner();
                signer.init(false, pubParams);
                return signer.verifySignature(payload, signature);

            } else if (keyPair.getKeyType() == PqcKeyType.SLH_DSA_256) {
                SPHINCSPlusPublicKeyParameters pubParams = new SPHINCSPlusPublicKeyParameters(
                        SPHINCSPlusParameters.sha2_256f,
                        keyPair.getPublicKeyBytes()
                );
                SPHINCSPlusSigner signer = new SPHINCSPlusSigner();
                signer.init(false, pubParams);
                return signer.verifySignature(payload, signature);
            }
        } catch (Exception e) {
            log.error("Signature verification aborted: {}", e.getMessage());
        }

        return false;
    }
}
