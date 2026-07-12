package com.pqvcf.zkp.api.rest;

import com.pqvcf.zkp.application.port.in.GenerateProofUseCase;
import com.pqvcf.zkp.application.port.in.GenerateProofUseCase.GenerateProofCommand;
import com.pqvcf.zkp.application.port.in.GenerateProofUseCase.ProofResponseDto;
import com.pqvcf.zkp.application.port.in.VerifyProofUseCase;
import com.pqvcf.zkp.application.port.in.VerifyProofUseCase.VerifyProofCommand;
import com.pqvcf.zkp.application.port.in.ZkProofQueryUseCase;
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
@RequestMapping("/api/v1/zkp")
@Tag(name = "Zero-Knowledge Proof Engine API", description = "Management endpoints for generating Pedersen commitments, creating compliance proof certificates, and verifying Fiat-Shamir Sigma provers")
public class ZkProofController {

    private final GenerateProofUseCase generateUseCase;
    private final VerifyProofUseCase verifyUseCase;
    private final ZkProofQueryUseCase queryUseCase;

    public ZkProofController(
            GenerateProofUseCase generateUseCase,
            VerifyProofUseCase verifyUseCase,
            ZkProofQueryUseCase queryUseCase) {
        this.generateUseCase = generateUseCase;
        this.verifyUseCase = verifyUseCase;
        this.queryUseCase = queryUseCase;
    }

    @PostMapping("/prove")
    @Operation(summary = "Generate a Sigma protocol zero-knowledge proof of compliance")
    public ResponseEntity<ProofResponseDto> prove(@RequestBody GenerateProofCommand command) {
        return ResponseEntity.status(HttpStatus.CREATED).body(generateUseCase.generateProof(command));
    }

    @GetMapping("/proofs")
    @Operation(summary = "List all zero-knowledge proofs registered in the auditing ledger")
    public ResponseEntity<List<ProofResponseDto>> listAll() {
        return ResponseEntity.ok(queryUseCase.listAllProofs());
    }

    @GetMapping("/proofs/{id}")
    @Operation(summary = "Get zero-knowledge proof details by ID")
    public ResponseEntity<ProofResponseDto> getById(@PathVariable String id) {
        return queryUseCase.getProofDetails(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/verify")
    @Operation(summary = "Verify Sigma protocol proof challenge responses")
    public ResponseEntity<Boolean> verify(@RequestBody VerifyProofCommand command) {
        return ResponseEntity.ok(verifyUseCase.verifyProof(command));
    }

    @DeleteMapping("/proofs/{id}")
    @Operation(summary = "Delete zero-knowledge proof from auditing ledger")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        queryUseCase.deleteProof(id);
        return ResponseEntity.noContent().build();
    }
}
