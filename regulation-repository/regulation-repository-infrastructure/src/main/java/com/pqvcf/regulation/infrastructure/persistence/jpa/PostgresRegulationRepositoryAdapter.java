package com.pqvcf.regulation.infrastructure.persistence.jpa;

import com.pqvcf.regulation.domain.model.Article;
import com.pqvcf.regulation.domain.model.Clause;
import com.pqvcf.regulation.domain.model.ClauseType;
import com.pqvcf.regulation.domain.model.Regulation;
import com.pqvcf.regulation.domain.model.RegulationId;
import com.pqvcf.regulation.domain.model.RegulationStatus;
import com.pqvcf.regulation.domain.repository.RegulationRepository;
import com.pqvcf.shared.types.JurisdictionCode;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class PostgresRegulationRepositoryAdapter implements RegulationRepository {

    private final SpringDataRegulationRepository springRepository;
    private final org.springframework.context.ApplicationEventPublisher eventPublisher;

    public PostgresRegulationRepositoryAdapter(
            SpringDataRegulationRepository springRepository,
            org.springframework.context.ApplicationEventPublisher eventPublisher) {
        this.springRepository = springRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public Regulation save(Regulation regulation) {
        List<com.pqvcf.shared.domain.DomainEvent> events = regulation.pullDomainEvents();
        RegulationJpaEntity jpaEntity = mapToJpa(regulation);
        RegulationJpaEntity saved = springRepository.save(jpaEntity);
        
        // Publish domain events to Spring context
        events.forEach(eventPublisher::publishEvent);
        
        return mapToDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Regulation> findById(RegulationId id) {
        return springRepository.findById(id.getValue())
                .map(this::mapToDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Regulation> findByShortName(String shortName) {
        return springRepository.findByShortName(shortName.toUpperCase())
                .map(this::mapToDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Regulation> findByJurisdiction(JurisdictionCode jurisdictionCode) {
        return springRepository.findByPrimaryJurisdiction(jurisdictionCode.getCode()).stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Regulation> findByStatus(RegulationStatus status) {
        return springRepository.findByStatus(status.name()).stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Regulation> findAll() {
        return springRepository.findAll(Sort.by(Sort.Direction.ASC, "shortName")).stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Regulation> findAll(int page, int pageSize) {
        PageRequest pageRequest = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.ASC, "shortName"));
        return springRepository.findAll(pageRequest).getContent().stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long count() {
        return springRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByShortName(String shortName) {
        return springRepository.existsByShortName(shortName.toUpperCase());
    }

    @Override
    @Transactional
    public void deleteById(RegulationId id) {
        springRepository.deleteById(id.getValue());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Regulation> search(String query) {
        return springRepository.search(query).stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    // ---- Mapping helper methods ----

    private RegulationJpaEntity mapToJpa(Regulation domain) {
        if (domain == null) return null;
        RegulationJpaEntity jpa = new RegulationJpaEntity();
        jpa.setId(domain.getId().getValue());
        jpa.setName(domain.getName());
        jpa.setShortName(domain.getShortName().toUpperCase());
        jpa.setPrimaryJurisdiction(domain.getPrimaryJurisdiction().getCode());
        jpa.setVersion(domain.getVersion());
        jpa.setEffectiveDate(domain.getEffectiveDate());
        jpa.setDescription(domain.getDescription());
        jpa.setStatus(domain.getStatus().name());
        jpa.setFormalSpec(domain.getFormalSpec());
        jpa.setCreatedAt(domain.getCreatedAt());
        jpa.setUpdatedAt(domain.getUpdatedAt());

        List<ArticleJpaEntity> articles = domain.getArticles().stream()
                .map(art -> {
                    ArticleJpaEntity artJpa = new ArticleJpaEntity();
                    artJpa.setId(art.getId());
                    artJpa.setRegulation(jpa);
                    artJpa.setArticleNumber(art.getArticleNumber());
                    artJpa.setTitle(art.getTitle());
                    artJpa.setContent(art.getContent());
                    artJpa.setDeonticFormula(art.getDeonticFormula());
                    artJpa.setOdrlPolicy(art.getOdrlPolicy());
                    artJpa.setCreatedAt(art.getCreatedAt());
                    artJpa.setUpdatedAt(art.getUpdatedAt());

                    List<ClauseJpaEntity> clauses = art.getClauses().stream()
                            .map(cls -> {
                                ClauseJpaEntity clsJpa = new ClauseJpaEntity();
                                clsJpa.setId(cls.getId());
                                clsJpa.setArticle(artJpa);
                                clsJpa.setClauseNumber(cls.getClauseNumber());
                                clsJpa.setContent(cls.getContent());
                                clsJpa.setClauseType(cls.getClauseType().name());
                                return clsJpa;
                            }).collect(Collectors.toList());

                    artJpa.setClauses(clauses);
                    return artJpa;
                }).collect(Collectors.toList());

        jpa.setArticles(articles);
        return jpa;
    }

    private Regulation mapToDomain(RegulationJpaEntity jpa) {
        if (jpa == null) return null;

        List<Article> domainArticles = jpa.getArticles().stream()
                .map(artJpa -> {
                    List<Clause> domainClauses = artJpa.getClauses().stream()
                            .map(clsJpa -> Clause.reconstitute(
                                    clsJpa.getId(),
                                    artJpa.getId(),
                                    clsJpa.getClauseNumber(),
                                    clsJpa.getContent(),
                                    ClauseType.valueOf(clsJpa.getClauseType().toUpperCase())
                            )).collect(Collectors.toList());

                    return Article.reconstitute(
                            artJpa.getId(),
                            jpa.getId(),
                            artJpa.getArticleNumber(),
                            artJpa.getTitle(),
                            artJpa.getContent(),
                            artJpa.getDeonticFormula(),
                            artJpa.getOdrlPolicy(),
                            domainClauses,
                            artJpa.getCreatedAt(),
                            artJpa.getUpdatedAt()
                    );
                }).collect(Collectors.toList());

        return Regulation.reconstitute(
                RegulationId.of(jpa.getId()),
                jpa.getName(),
                jpa.getShortName(),
                JurisdictionCode.of(jpa.getPrimaryJurisdiction()),
                jpa.getVersion(),
                jpa.getEffectiveDate(),
                jpa.getDescription(),
                RegulationStatus.valueOf(jpa.getStatus().toUpperCase()),
                jpa.getFormalSpec(),
                domainArticles,
                jpa.getCreatedAt(),
                jpa.getUpdatedAt()
        );
    }
}
