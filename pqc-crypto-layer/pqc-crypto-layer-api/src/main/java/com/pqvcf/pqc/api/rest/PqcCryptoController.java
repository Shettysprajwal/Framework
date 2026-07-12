package com.pqvcf.pqc.api.rest;

import com.pqvcf.pqc.application.port.in.GenerateKeyUseCase;
import com.pqvcf.pqc.application.port.in.GenerateKeyUseCase.KeyResponseDto;
import com.pqvcf.pqc.application.port.in.KmsKeyQueryUseCase;
import com.pqvcf.pqc.application.port.in.SignPayloadUseCase;
import com.pqvcf.pqc.application.port.in.SignPayloadUseCase.SignCommand;
import com.pqvcf.pqc.application.port.in.SignPayloadUseCase.SignResponseDto;
import com.pqvcf.pqc.application.port.in.SignPayloadUseCase.VerifyCommand;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pqc")
@Tag(name = "Post-Quantum Cryptography API", description = "Management endpoints for generating ML-KEM/ML-DSA keys, signing compliance bundles, and verifying signatures")
public class PqcCryptoController {

    private final GenerateKeyUseCase generateUseCase;
    private final SignPayloadUseCase signUseCase;
    private final KmsKeyQueryUseCase queryUseCase;

    public PqcCryptoController(
            GenerateKeyUseCase generateUseCase,
            SignPayloadUseCase signUseCase,
            KmsKeyQueryUseCase queryUseCase) {
        this.generateUseCase = generateUseCase;
        this.signUseCase = signUseCase;
        this.queryUseCase = queryUseCase;
    }

    @PostMapping("/keys")
    @Operation(summary = "Generate a post-quantum key pair (ML_KEM_768, ML_DSA_65, SLH_DSA_256)")
    public ResponseEntity<KeyResponseDto> generate(@RequestBody GenerateKeyUseCase.GenerateKeyCommand command) {
        return ResponseEntity.status(HttpStatus.CREATED).body(generateUseCase.generateKey(command));
    }

    @GetMapping("/keys")
    @Operation(summary = "List all post-quantum keys registered in KMS storage")
    public ResponseEntity<List<KeyResponseDto>> listAll() {
        return ResponseEntity.ok(queryUseCase.listAllKeys());
    }

    @GetMapping("/keys/{id}")
    @Operation(summary = "Get post-quantum key details by ID")
    public ResponseEntity<KeyResponseDto> getById(@PathVariable String id) {
        return queryUseCase.getKeyDetails(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/sign")
    @Operation(summary = "Generate post-quantum signature on payloadHex using ML-DSA or SLH-DSA key")
    public ResponseEntity<SignResponseDto> sign(@RequestBody SignCommand command) {
        return ResponseEntity.ok(signUseCase.sign(command));
    }

    @PostMapping("/verify")
    @Operation(summary = "Verify post-quantum signature on payloadHex")
    public ResponseEntity<Boolean> verify(@RequestBody VerifyCommand command) {
        return ResponseEntity.ok(signUseCase.verify(command));
    }

    @DeleteMapping("/keys/{id}")
    @Operation(summary = "Delete post-quantum key from KMS vault")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        queryUseCase.deleteKey(id);
        return ResponseEntity.noContent().build();
    }
}
