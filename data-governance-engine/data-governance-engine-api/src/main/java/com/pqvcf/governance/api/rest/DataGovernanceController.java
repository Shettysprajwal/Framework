package com.pqvcf.governance.api.rest;

import com.pqvcf.governance.application.port.in.EvaluateFlowUseCase;
import com.pqvcf.governance.application.port.in.EvaluateFlowUseCase.EvaluateFlowCommand;
import com.pqvcf.governance.application.port.in.EvaluateFlowUseCase.FlowDecisionResponseDto;
import com.pqvcf.governance.application.port.in.GovernanceQueryUseCase;
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
@RequestMapping("/api/v1/governance")
@Tag(name = "Data Governance Engine API", description = "Management endpoints for evaluating legality of cross-border data flows")
public class DataGovernanceController {

    private final EvaluateFlowUseCase evaluateUseCase;
    private final GovernanceQueryUseCase queryUseCase;

    public DataGovernanceController(
            EvaluateFlowUseCase evaluateUseCase,
            GovernanceQueryUseCase queryUseCase) {
        this.evaluateUseCase = evaluateUseCase;
        this.queryUseCase = queryUseCase;
    }

    @PostMapping("/evaluate")
    @Operation(summary = "Evaluate cross-border data transfer legality against multiple international regulations")
    public ResponseEntity<FlowDecisionResponseDto> evaluate(@RequestBody EvaluateFlowCommand command) {
        return ResponseEntity.status(HttpStatus.CREATED).body(evaluateUseCase.evaluateFlow(command));
    }

    @GetMapping("/decisions")
    @Operation(summary = "List all data transfer decisions logged in the auditing ledger")
    public ResponseEntity<List<FlowDecisionResponseDto>> listAll() {
        return ResponseEntity.ok(queryUseCase.listAllDecisions());
    }

    @GetMapping("/decisions/{id}")
    @Operation(summary = "Get data transfer decision details by ID")
    public ResponseEntity<FlowDecisionResponseDto> getById(@PathVariable String id) {
        return queryUseCase.getDecisionDetails(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/decisions/{id}")
    @Operation(summary = "Delete data transfer decision from auditing ledger")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        queryUseCase.deleteDecision(id);
        return ResponseEntity.noContent().build();
    }
}
