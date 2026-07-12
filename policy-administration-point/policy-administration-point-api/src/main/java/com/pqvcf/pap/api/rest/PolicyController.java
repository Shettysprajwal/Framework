package com.pqvcf.pap.api.rest;

import com.pqvcf.pap.application.port.in.CreatePolicyUseCase;
import com.pqvcf.pap.application.port.in.CreatePolicyUseCase.PolicyResponse;
import com.pqvcf.pap.application.port.in.GetPolicyUseCase;
import com.pqvcf.pap.application.port.in.LinkRuleUseCase;
import com.pqvcf.pap.application.port.in.LinkRuleUseCase.LinkRuleCommand;
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
@RequestMapping("/api/v1/policies")
@Tag(name = "Policy Administration API", description = "Management endpoints for authoring organizational compliance policies and mapping them to regulations")
public class PolicyController {

    private final CreatePolicyUseCase createUseCase;
    private final LinkRuleUseCase linkUseCase;
    private final GetPolicyUseCase getUseCase;

    public PolicyController(
            CreatePolicyUseCase createUseCase,
            LinkRuleUseCase linkUseCase,
            GetPolicyUseCase getUseCase) {
        this.createUseCase = createUseCase;
        this.linkUseCase = linkUseCase;
        this.getUseCase = getUseCase;
    }

    @PostMapping
    @Operation(summary = "Create a new organizational policy (initially in DRAFT status)")
    public ResponseEntity<PolicyResponse> create(@RequestBody CreatePolicyUseCase.CreatePolicyCommand command) {
        return ResponseEntity.status(HttpStatus.CREATED).body(createUseCase.create(command));
    }

    @GetMapping
    @Operation(summary = "List all organizational policies")
    public ResponseEntity<List<PolicyResponse>> listAll() {
        return ResponseEntity.ok(getUseCase.listAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get organizational policy details by UUID, including rule links mapping")
    public ResponseEntity<PolicyResponse> getById(@PathVariable String id) {
        return getUseCase.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/links")
    @Operation(summary = "Bind/link a regulatory rule from Module 2 Rule Translation to this organizational policy")
    public ResponseEntity<PolicyResponse> linkRule(
            @PathVariable String id,
            @RequestBody LinkRuleCommandRequestBody requestBody) {
        
        LinkRuleCommand command = new LinkRuleCommand(
                id,
                requestBody.organizationalRuleName(),
                requestBody.regulatoryRuleId(),
                requestBody.description()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(linkUseCase.linkRule(command));
    }

    @DeleteMapping("/{id}/links/{linkId}")
    @Operation(summary = "Remove regulatory rule binding link mapping from organizational policy")
    public ResponseEntity<PolicyResponse> unlinkRule(
            @PathVariable String id,
            @PathVariable String linkId) {
        return ResponseEntity.ok(linkUseCase.unlinkRule(id, linkId));
    }

    @PostMapping("/{id}/activate")
    @Operation(summary = "Transition policy status to ACTIVE (notifies PDP for compliance enforcement)")
    public ResponseEntity<PolicyResponse> activate(@PathVariable String id) {
        return ResponseEntity.ok(getUseCase.activate(id));
    }

    @PostMapping("/{id}/deprecate")
    @Operation(summary = "Transition policy status to DEPRECATED")
    public ResponseEntity<PolicyResponse> deprecate(@PathVariable String id) {
        return ResponseEntity.ok(getUseCase.deprecate(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete DRAFT policy from administration point")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        getUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }

    public record LinkRuleCommandRequestBody(
            String organizationalRuleName,
            String regulatoryRuleId,
            String description
    ) {}
}
