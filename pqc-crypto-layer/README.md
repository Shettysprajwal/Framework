# Module 6 — Post-Quantum Cryptography (PQC) Layer

## 1. Objective
Establish a quantum-resistant cryptographic key vault management and digital signatures infrastructure for compliance proofs signing and key exchanges using NIST FIPS 203, 204, and 205 algorithms.

## 2. Research Motivation
Classical digital signature algorithms (RSA, ECDSA) rely on the hardness of factoring or finding discrete logarithms, which Shor's algorithm can solve in polynomial time using a sufficiently large quantum computer. By incorporating post-quantum primitives (ML-DSA, ML-KEM) early in systems architectures, compliance evidence guarantees remain mathematically secure against quantum adversaries.

## 3. Mathematical Model (Lattice-Based Cryptography)
1. **ML-KEM-768 (Kyber-768)**: Security is based on the hardness of the Module Learning With Errors ($\text{M-LWE}$) problem in modular lattices. Key exchange generates shared secret key seed $K \in \{0,1\}^{256}$ and ciphertext $c$ from recipient's public key $pk$.
2. **ML-DSA-65 (Dilithium3)**: Security is based on the hardness of the Module Short Integer Solution ($\text{M-SIS}$) and $\text{M-LWE}$ problems. Digital signing generates a high-dimensional vector signature $\sigma$ from private key $sk$ such that a verification polynomial check satisfies:
$$A \cdot z - c \cdot t \approx w$$
where $z$ is a short vector, $c$ is a hash challenge, and $t$ is the public key vector.

## 4. Assumptions
1. Bouncy Castle's direct implementation of FIPS standards matches the official NIST test vectors correctly.
2. The local system entropy source (`java.security.SecureRandom`) is robust and non-deterministic.

## 5. Inputs & Outputs
- **Key Generation**: Inputs: Algorithm Type, Alias. Outputs: Public key hex, expiration.
- **Signing**: Inputs: Key ID, Payload Hex. Outputs: Signature Hex, signature length.
- **Verification**: Inputs: Key ID, Payload Hex, Signature Hex. Outputs: Boolean validity.

## 6. Algorithms
### ML-DSA Digital Signature Verification
```text
Algorithm VerifyMldsa(PublicKey, Payload, Signature):
    // Parse signature vector parameters
    (z, c) = DecodeSignature(Signature)
    if ||z|| > limit:
        return False
    // Compute random challenge mapping
    w1 = ReconstructHighBits(A, z, c, PublicKey)
    return c == Hash(w1 || Payload)
```

## 7. Complexity Analysis
- **Key Generation**: $O(d^2 \cdot \log q)$ where $d$ is lattice dimension. Takes $< 1\text{ms}$.
- **Signing / Verification**: $O(d^2)$ matrix-vector multiplications. Significantly faster than equivalent RSA key iterations, verifying in $<3\text{ms}$.

## 8. Security Analysis
- Key material is maintained securely in an in-memory vault. Private keys are never returned to UI clients.
- Digital signatures provide non-repudiation and tamper-proofing for compliance audit trails.

## 9. Design Decisions
- **Direct Bouncy Castle Engine API**: Avoided standard JCA provider dynamic registrations to prevent JVM security policy blocks. Direct class constructors run faster and are more robust in cloud microservices.
- **Stateless Verification**: The verification endpoint is fully stateless, taking public key information or looking it up dynamically.

## 10. APIs
- **REST Endpoints**:
  - `POST /api/v1/pqc/keys` (Generate keys)
  - `GET /api/v1/pqc/keys` (List ledger)
  - `POST /api/v1/pqc/sign` (Generate signature)
  - `POST /api/v1/pqc/verify` (Verify signature)
- **gRPC Services**:
  - `GenerateKeyPair`
  - `SignPayload`
  - `VerifySignature`

## 11. Unit & Integration Tests
- **PqcKeyPairTest**: Domain model checks.
- **PqcCryptographyServiceTest**: Service operations check.
- **PqcCryptoControllerIT**: End-to-end Bouncy Castle JCA signature round-trips checks.

## 12. Example Execution
1. Client generates `ML_DSA_65` key with alias `audit-signer`.
2. Client sends payload hex `68656c6c6f` to `/sign`.
3. Service calls `DilithiumSigner` and returns 3,293-byte signature.
4. Verifier calls `/verify` and returns `true`.

## 13. Limitations
- Large signature size (3.2KB for ML-DSA, 17KB for SLH-DSA) can introduce network overhead if appended to every REST request header.

## 14. Future Improvements
- Integrate hardware token interfaces (PKCS11) for secure cryptographic key storage on local hardware security modules (HSM).
