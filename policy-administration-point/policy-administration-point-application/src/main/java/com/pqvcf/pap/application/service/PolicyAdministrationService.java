package com.pqvcf.pap.application.service;

import com.pqvcf.pap.application.dto.DtoMapper;
import com.pqvcf.pap.application.port.in.CreatePolicyUseCase;
import com.pqvcf.pap.application.port.in.GetPolicyUseCase;
import com.pqvcf.pap.application.port.in.LinkRuleUseCase;
import com.pqvcf.pap.domain.model.Policy;
import com.pqvcf.pap.domain.model.PolicyId;
import com.pqvcf.pap.domain.model.PolicyStatus;
import com.pqvcf.pap.domain.repository.PolicyRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class PolicyAdministrationService implements CreatePolicyUseCase, LinkRuleUseCase, GetPolicyUseCase {

    private final PolicyRepository repository;

    public PolicyAdministrationService(PolicyRepository repository) {
        this.repository = repository;
    }

    @Override
    public PolicyResponse create(CreatePolicyCommand command) {
        if (repository.existsByName(command.name())) {
            throw new IllegalArgumentException("Policy with name '" + command.name() + "' already exists.");
        }

        Policy policy = Policy.create(
                command.name(),
                command.owner(),
                command.description()
        );

        Policy saved = repository.save(policy);
        return DtoMapper.toResponse(saved);
    }

    @Override
    public PolicyResponse linkRule(LinkRuleCommand command) {
        Policy policy = repository.findById(PolicyId.fromString(command.policyId()))
                .orElseThrow(() -> new IllegalArgumentException("Policy not found with ID: " + command.policyId()));

        if (policy.getStatus() != PolicyStatus.DRAFT) {
            throw new IllegalStateException("Can only link rules to DRAFT policies.");
        }

        policy.addRuleLink(
                command.organizationalRuleName(),
                UUID.fromString(command.regulatoryRuleId()),
                command.description()
        );

        Policy saved = repository.save(policy);
        return DtoMapper.toResponse(saved);
    }

    @Override
    public PolicyResponse unlinkRule(String policyId, String ruleLinkId) {
        Policy policy = repository.findById(PolicyId.fromString(policyId))
                .orElseThrow(() -> new IllegalArgumentException("Policy not found with ID: " + policyId));

        if (policy.getStatus() != PolicyStatus.DRAFT) {
            throw new IllegalStateException("Can only unlink rules from DRAFT policies.");
        }

        policy.removeRuleLink(UUID.fromString(ruleLinkId));
        Policy saved = repository.save(policy);
        return DtoMapper.toResponse(saved);
    }

    @Override
    public Optional<PolicyResponse> getById(String id) {
        return repository.findById(PolicyId.fromString(id)).map(DtoMapper::toResponse);
    }

    @Override
    public List<PolicyResponse> listAll() {
        return repository.findAll().stream()
                .map(DtoMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(String id) {
        PolicyId policyId = PolicyId.fromString(id);
        Policy policy = repository.findById(policyId)
                .orElseThrow(() -> new IllegalArgumentException("Policy not found with ID: " + id));
        if (policy.getStatus() != PolicyStatus.DRAFT) {
            throw new IllegalStateException("Only policies in DRAFT status can be deleted.");
        }
        repository.deleteById(policyId);
    }

    @Override
    public PolicyResponse activate(String id) {
        Policy policy = repository.findById(PolicyId.fromString(id))
                .orElseThrow(() -> new IllegalArgumentException("Policy not found with ID: " + id));
        
        policy.activate();
        Policy saved = repository.save(policy);
        return DtoMapper.toResponse(saved);
    }

    @Override
    public PolicyResponse deprecate(String id) {
        Policy policy = repository.findById(PolicyId.fromString(id))
                .orElseThrow(() -> new IllegalArgumentException("Policy not found with ID: " + id));

        policy.deprecate();
        Policy saved = repository.save(policy);
        return DtoMapper.toResponse(saved);
    }
}
