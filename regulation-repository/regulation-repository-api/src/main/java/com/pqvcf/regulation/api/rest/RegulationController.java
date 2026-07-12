package com.pqvcf.regulation.api.rest;

import com.pqvcf.regulation.application.dto.RegulationResponse;
import com.pqvcf.regulation.application.port.in.GetRegulationUseCase;
import com.pqvcf.regulation.application.port.in.ListRegulationsUseCase;
import com.pqvcf.regulation.application.port.in.RegisterRegulationUseCase;
import com.pqvcf.regulation.application.port.in.UpdateRegulationUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/regulations")
@Tag(name = "Regulations API", description = "Management endpoints for the machine-readable regulations repository")
public class RegulationController {

    private final RegisterRegulationUseCase registerUseCase;
    private final GetRegulationUseCase getUseCase;
    private final ListRegulationsUseCase listUseCase;
    private final UpdateRegulationUseCase updateUseCase;

    public RegulationController(
            RegisterRegulationUseCase registerUseCase,
            GetRegulationUseCase getUseCase,
            ListRegulationsUseCase listUseCase,
            UpdateRegulationUseCase updateUseCase) {
        this.registerUseCase = registerUseCase;
        this.getUseCase = getUseCase;
        this.listUseCase = listUseCase;
        this.updateUseCase = updateUseCase;
    }

    @PostMapping
    @Operation(summary = "Register a new regulation (initially in DRAFT status)")
    public ResponseEntity<RegulationResponse> register(@RequestBody RegisterRegulationUseCase.RegisterRegulationCommand command) {
        return ResponseEntity.status(HttpStatus.CREATED).body(registerUseCase.register(command));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a regulation details by UUID")
    public ResponseEntity<RegulationResponse> getById(@PathVariable String id) {
        return getUseCase.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/short/{shortName}")
    @Operation(summary = "Get a regulation details by normalized short name (e.g. GDPR)")
    public ResponseEntity<RegulationResponse> getByShortName(@PathVariable String shortName) {
        return getUseCase.getByShortName(shortName)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "List all regulations, optionally filtered by jurisdiction")
    public ResponseEntity<List<RegulationResponse>> list(
            @RequestParam(required = false) String jurisdiction,
            @RequestParam(required = false) String search) {
        if (search != null && !search.isBlank()) {
            return ResponseEntity.ok(listUseCase.search(search));
        }
        if (jurisdiction != null && !jurisdiction.isBlank()) {
            return ResponseEntity.ok(listUseCase.listByJurisdiction(jurisdiction));
        }
        return ResponseEntity.ok(listUseCase.listAll());
    }

    @PutMapping("/{id}/metadata")
    @Operation(summary = "Update metadata of an existing regulation")
    public ResponseEntity<RegulationResponse> updateMetadata(
            @PathVariable String id,
            @RequestBody UpdateRegulationUseCase.UpdateMetadataCommand command) {
        return ResponseEntity.ok(updateUseCase.updateMetadata(id, command));
    }

    @PutMapping("/{id}/formal-spec")
    @Operation(summary = "Update the SMT-LIB2 formal specification of a regulation")
    public ResponseEntity<RegulationResponse> updateFormalSpec(
            @PathVariable String id,
            @RequestBody String formalSpec) {
        return ResponseEntity.ok(updateUseCase.updateFormalSpec(id, formalSpec));
    }

    @PostMapping("/{id}/activate")
    @Operation(summary = "Transition a regulation status to ACTIVE")
    public ResponseEntity<RegulationResponse> activate(@PathVariable String id) {
        return ResponseEntity.ok(updateUseCase.activate(id));
    }

    @PostMapping("/{id}/deprecate")
    @Operation(summary = "Transition a regulation status to DEPRECATED")
    public ResponseEntity<RegulationResponse> deprecate(@PathVariable String id) {
        return ResponseEntity.ok(updateUseCase.deprecate(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a DRAFT regulation from the repository")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        updateUseCase.deleteDraft(id);
        return ResponseEntity.noContent().build();
    }
}
