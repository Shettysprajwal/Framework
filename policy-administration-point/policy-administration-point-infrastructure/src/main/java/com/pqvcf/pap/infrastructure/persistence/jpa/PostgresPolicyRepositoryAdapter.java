package com.pqvcf.pap.infrastructure.persistence.jpa;

import com.pqvcf.pap.domain.model.Policy;
import com.pqvcf.pap.domain.model.PolicyId;
import com.pqvcf.pap.domain.model.PolicyStatus;
import com.pqvcf.pap.domain.model.RuleLink;
import com.pqvcf.pap.domain.repository.PolicyRepository;
import com.pqvcf.shared.domain.DomainEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class PostgresPolicyRepositoryAdapter implements PolicyRepository {

    private final SpringDataPolicyRepository springRepository;
    private final ApplicationEventPublisher eventPublisher;

    public PostgresPolicyRepositoryAdapter(
            SpringDataPolicyRepository springRepository,
            ApplicationEventPublisher eventPublisher) {
        this.springRepository = springRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public Policy save(Policy policy) {
        List<DomainEvent> events = policy.pullDomainEvents();
        PolicyJpaEntity jpaEntity = mapToJpa(policy);
        PolicyJpaEntity saved = springRepository.save(jpaEntity);

        // Publish events to spring context after transaction persistence
        events.forEach(eventPublisher::publishEvent);

        return mapToDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Policy> findById(PolicyId id) {
        return springRepository.findById(id.getValue()).map(this::mapToDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Policy> findAll() {
        return springRepository.findAll().stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteById(PolicyId id) {
        springRepository.deleteById(id.getValue());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return springRepository.existsByNameIgnoreCase(name);
    }

    // ---- Mappings ----

    private PolicyJpaEntity mapToJpa(Policy domain) {
        PolicyJpaEntity jpa = new PolicyJpaEntity();
        jpa.setId(domain.getId().getValue());
        jpa.setName(domain.getName());
        jpa.setOwner(domain.getOwner());
        jpa.setDescription(domain.getDescription());
        jpa.setStatus(domain.getStatus().name());
        jpa.setCreatedAt(domain.getCreatedAt());
        jpa.setUpdatedAt(domain.getUpdatedAt());

        List<RuleLinkJpaEntity> links = domain.getRuleLinks().stream()
                .map(link -> {
                    RuleLinkJpaEntity linkJpa = new RuleLinkJpaEntity();
                    linkJpa.setId(link.getId());
                    linkJpa.setPolicy(jpa);
                    linkJpa.setOrganizationalRuleName(link.getOrganizationalRuleName());
                    linkJpa.setRegulatoryRuleId(link.getRegulatoryRuleId());
                    linkJpa.setDescription(link.getDescription());
                    return linkJpa;
                }).collect(Collectors.toList());

        jpa.setRuleLinks(links);
        return jpa;
    }

    private Policy mapToDomain(PolicyJpaEntity jpa) {
        List<RuleLink> domainLinks = jpa.getRuleLinks().stream()
                .map(linkJpa -> RuleLink.reconstitute(
                        linkJpa.getId(),
                        jpa.getId(),
                        linkJpa.getOrganizationalRuleName(),
                        linkJpa.getRegulatoryRuleId(),
                        linkJpa.getDescription()
                )).collect(Collectors.toList());

        return Policy.reconstitute(
                PolicyId.of(jpa.getId()),
                jpa.getName(),
                jpa.getOwner(),
                jpa.getDescription(),
                PolicyStatus.valueOf(jpa.getStatus().toUpperCase()),
                domainLinks,
                jpa.getCreatedAt(),
                jpa.getUpdatedAt()
        );
    }
}
