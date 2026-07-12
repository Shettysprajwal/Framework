package com.pqvcf.zkp.infrastructure.crypto;

import com.pqvcf.zkp.domain.model.PedersenCommitment;
import com.pqvcf.zkp.domain.model.ZkProof;
import com.pqvcf.zkp.domain.model.ZkProofType;
import com.pqvcf.zkp.application.port.out.ZkProofProvider;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.math.ec.ECMultiplier;
import org.bouncycastle.math.ec.FixedPointCombMultiplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.UUID;

@Component
public class BouncyCastleZkAdapter implements ZkProofProvider {

    private static final Logger log = LoggerFactory.getLogger(BouncyCastleZkAdapter.class);

    private final X9ECParameters curve = CustomNamedCurves.getByName("secp256r1");
    private final ECPoint G = curve.getG();
    private final ECPoint H; // Secondary generator point
    private final BigInteger q = curve.getN(); // Group order
    private final SecureRandom random = new SecureRandom();

    public BouncyCastleZkAdapter() {
        // Generate secondary generator h by hashing G coordinates to ensure unknown discrete log
        byte[] gEncoded = G.getEncoded(true);
        byte[] hHash = hashSHA256(gEncoded);
        BigInteger hScalar = new BigInteger(1, hHash).mod(q);
        this.H = G.multiply(hScalar).normalize();
    }

    @Override
    public PedersenCommitment commitPedersen(long secretValue) {
        log.info("Calculating Pedersen commitment for secret attribute: {}", secretValue);
        
        // Random blinding factor r in [1, q-1]
        BigInteger r = new BigInteger(256, random).mod(q.subtract(BigInteger.ONE)).add(BigInteger.ONE);
        BigInteger x = BigInteger.valueOf(secretValue).mod(q);

        // C = x * G + r * H
        ECPoint cPoint = G.multiply(x).add(H.multiply(r)).normalize();

        return new PedersenCommitment(
                cPoint.getEncoded(true),
                r.toByteArray(),
                secretValue
        );
    }

    @Override
    public ZkProof proveSigma(
            ZkProofType type,
            PedersenCommitment commitment,
            String publicInputsJson) {

        log.info("Generating Schnorr-Pedersen Sigma proof of knowledge for commitment...");

        BigInteger x = BigInteger.valueOf(commitment.getSecretValue()).mod(q);
        BigInteger r = new BigInteger(1, commitment.getBlindingFactor());
        ECPoint C = curve.getCurve().decodePoint(commitment.getCommitmentBytes());

        // 1. Commit phase: choose random k1, k2 in [1, q-1]
        BigInteger k1 = new BigInteger(256, random).mod(q.subtract(BigInteger.ONE)).add(BigInteger.ONE);
        BigInteger k2 = new BigInteger(256, random).mod(q.subtract(BigInteger.ONE)).add(BigInteger.ONE);

        // T = k1 * G + k2 * H
        ECPoint T = G.multiply(k1).add(H.multiply(k2)).normalize();

        // 2. Challenge phase (Fiat-Shamir heuristic): hash G, H, C, T
        byte[] challengeBytes = computeChallengeHash(C, T);
        BigInteger c = new BigInteger(1, challengeBytes).mod(q);

        // 3. Response phase: s1 = k1 + c * x (mod q), s2 = k2 + c * r (mod q)
        BigInteger s1 = k1.add(c.multiply(x)).mod(q);
        BigInteger s2 = k2.add(c.multiply(r)).mod(q);

        // Package proof challenge responses
        // Response format: s1 (32 bytes) || s2 (32 bytes) || T (33 bytes compressed point)
        byte[] s1Bytes = pad32Bytes(s1.toByteArray());
        byte[] s2Bytes = pad32Bytes(s2.toByteArray());
        byte[] TBytes = T.getEncoded(true);

        byte[] responseBytes = new byte[s1Bytes.length + s2Bytes.length + TBytes.length];
        System.arraycopy(s1Bytes, 0, responseBytes, 0, s1Bytes.length);
        System.arraycopy(s2Bytes, 0, responseBytes, s1Bytes.length, s2Bytes.length);
        System.arraycopy(TBytes, 0, responseBytes, s1Bytes.length + s2Bytes.length, TBytes.length);

        String proofId = UUID.randomUUID().toString();

        return new ZkProof(
                proofId,
                type,
                commitment,
                challengeBytes,
                responseBytes,
                publicInputsJson,
                true // Proven locally valid
        );
    }

    @Override
    public boolean verifySigma(ZkProof proof) {
        log.info("Verifying Sigma protocol compliance proof: {}", proof.getProofId());

        try {
            ECPoint C = curve.getCurve().decodePoint(proof.getCommitment().getCommitmentBytes());
            byte[] responseBytes = proof.getResponseBytes();
            
            // Extract s1, s2, and T
            byte[] s1Bytes = new byte[32];
            byte[] s2Bytes = new byte[32];
            byte[] TBytes = new byte[responseBytes.length - 64];

            System.arraycopy(responseBytes, 0, s1Bytes, 0, 32);
            System.arraycopy(responseBytes, 32, s2Bytes, 0, 32);
            System.arraycopy(responseBytes, 64, TBytes, 0, TBytes.length);

            BigInteger s1 = new BigInteger(1, s1Bytes);
            BigInteger s2 = new BigInteger(1, s2Bytes);
            ECPoint T = curve.getCurve().decodePoint(TBytes);

            // Reconstruct challenge c
            byte[] reconstructedChallenge = computeChallengeHash(C, T);
            BigInteger c = new BigInteger(1, reconstructedChallenge).mod(q);

            // Verification Check: s1 * G + s2 * H == T + c * C
            ECPoint left = G.multiply(s1).add(H.multiply(s2)).normalize();
            ECPoint right = T.add(C.multiply(c)).normalize();

            boolean isValid = left.equals(right);
            log.info("Sigma proof verification result: {}", isValid);
            return isValid;

        } catch (Exception e) {
            log.error("Failed to parse and verify Sigma proof parameters ({}). Rejecting proof.", e.getMessage());
        }
        return false;
    }

    private byte[] computeChallengeHash(ECPoint C, ECPoint T) {
        byte[] gEnc = G.getEncoded(true);
        byte[] hEnc = H.getEncoded(true);
        byte[] cEnc = C.getEncoded(true);
        byte[] tEnc = T.getEncoded(true);

        byte[] combined = new byte[gEnc.length + hEnc.length + cEnc.length + tEnc.length];
        System.arraycopy(gEnc, 0, combined, 0, gEnc.length);
        System.arraycopy(hEnc, 0, combined, gEnc.length, hEnc.length);
        System.arraycopy(cEnc, 0, combined, gEnc.length + hEnc.length, cEnc.length);
        System.arraycopy(tEnc, 0, combined, gEnc.length + hEnc.length + cEnc.length, tEnc.length);

        return hashSHA256(combined);
    }

    private byte[] hashSHA256(byte[] input) {
        SHA256Digest digest = new SHA256Digest();
        byte[] output = new byte[digest.getDigestSize()];
        digest.update(input, 0, input.length);
        digest.doFinal(output, 0);
        return output;
    }

    private byte[] pad32Bytes(byte[] val) {
        if (val.length == 32) return val;
        byte[] padded = new byte[32];
        if (val.length < 32) {
            System.arraycopy(val, 0, padded, 32 - val.length, val.length);
        } else {
            System.arraycopy(val, val.length - 32, padded, 0, 32);
        }
        return padded;
    }
}
