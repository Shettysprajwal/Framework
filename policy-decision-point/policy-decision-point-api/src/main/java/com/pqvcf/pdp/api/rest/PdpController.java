package com.pqvcf.pdp.api.rest;

import com.pqvcf.pdp.application.port.in.EvaluateRequestUseCase;
import com.pqvcf.pdp.application.port.in.EvaluateRequestUseCase.EvaluateCommand;
import com.pqvcf.pdp.application.port.in.EvaluateRequestUseCase.EvaluateResponse;
import com.pqvcf.pdp.domain.repository.PolicyDecisionRepository.DecisionAuditLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pdp")
@Tag(name = "Policy Decision Point API", description = "Evaluation endpoints for access authorization and audit trail tracking")
public class PdpController {

    private final EvaluateRequestUseCase evaluateUseCase;

    public PdpController(EvaluateRequestUseCase evaluateUseCase) {
        this.evaluateUseCase = evaluateUseCase;
    }

    @PostMapping("/evaluate")
    @Operation(summary = "Evaluate compliance request and verify logic via Z3 SMT solver")
    public ResponseEntity<EvaluateResponse> evaluate(@RequestBody EvaluateCommand command) {
        return ResponseEntity.ok(evaluateUseCase.evaluate(command));
    }

    @GetMapping("/audits")
    @Operation(summary = "Retrieve log audit trails of compliance decisions")
    public ResponseEntity<List<DecisionAuditLog>> listAuditLogs() {
        return ResponseEntity.ok(evaluateUseCase.listAuditLogs());
    }
}
