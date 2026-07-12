package com.pqvcf.regulation.application.service;

import com.pqvcf.regulation.application.dto.ArticleResponse;
import com.pqvcf.regulation.application.dto.DtoMapper;
import com.pqvcf.regulation.application.dto.RegulationResponse;
import com.pqvcf.regulation.application.port.in.AddArticleUseCase;
import com.pqvcf.regulation.application.port.in.GetRegulationUseCase;
import com.pqvcf.regulation.application.port.in.ListRegulationsUseCase;
import com.pqvcf.regulation.application.port.in.RegisterRegulationUseCase;
import com.pqvcf.regulation.application.port.in.UpdateRegulationUseCase;
import com.pqvcf.regulation.domain.model.Article;
import com.pqvcf.regulation.domain.model.ClauseType;
import com.pqvcf.regulation.domain.model.Regulation;
import com.pqvcf.regulation.domain.model.RegulationId;
import com.pqvcf.regulation.domain.model.RegulationStatus;
import com.pqvcf.regulation.domain.repository.RegulationRepository;
import com.pqvcf.shared.types.JurisdictionCode;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class RegulationService implements
        RegisterRegulationUseCase,
        GetRegulationUseCase,
        ListRegulationsUseCase,
        AddArticleUseCase,
        UpdateRegulationUseCase {

    private final RegulationRepository repository;

    public RegulationService(RegulationRepository repository) {
        this.repository = repository;
    }

    @Override
    public RegulationResponse register(RegisterRegulationCommand command) {
        if (repository.existsByShortName(command.shortName().toUpperCase())) {
            throw new IllegalArgumentException("Regulation with short name " + command.shortName() + " already exists.");
        }

        Regulation regulation = Regulation.create(
                command.name(),
                command.shortName(),
                JurisdictionCode.of(command.jurisdiction()),
                command.version(),
                command.description()
        );

        Regulation saved = repository.save(regulation);
        return DtoMapper.toResponse(saved);
    }

    @Override
    public Optional<RegulationResponse> getById(String id) {
        return repository.findById(RegulationId.fromString(id))
                .map(DtoMapper::toResponse);
    }

    @Override
    public Optional<RegulationResponse> getByShortName(String shortName) {
        return repository.findByShortName(shortName.toUpperCase())
                .map(DtoMapper::toResponse);
    }

    @Override
    public List<RegulationResponse> listAll() {
        return repository.findAll().stream()
                .map(DtoMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<RegulationResponse> listByJurisdiction(String jurisdiction) {
        return repository.findByJurisdiction(JurisdictionCode.of(jurisdiction)).stream()
                .map(DtoMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<RegulationResponse> search(String query) {
        return repository.search(query).stream()
                .map(DtoMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ArticleResponse addArticle(AddArticleCommand command) {
        Regulation regulation = repository.findById(RegulationId.fromString(command.regulationId()))
                .orElseThrow(() -> new IllegalArgumentException("Regulation not found with ID: " + command.regulationId()));

        Article article = regulation.addArticle(
                command.articleNumber(),
                command.title(),
                command.content()
        );

        if (command.deonticFormula() != null) {
            article.setDeonticFormula(command.deonticFormula());
        }
        if (command.odrlPolicy() != null) {
            article.setOdrlPolicy(command.odrlPolicy());
        }

        if (command.clauses() != null) {
            for (var clauseCmd : command.clauses()) {
                article.addClause(
                        clauseCmd.clauseNumber(),
                        clauseCmd.content(),
                        ClauseType.valueOf(clauseCmd.clauseType().toUpperCase())
                );
            }
        }

        repository.save(regulation);
        return DtoMapper.toResponse(article);
    }

    @Override
    public RegulationResponse activate(String id) {
        Regulation regulation = repository.findById(RegulationId.fromString(id))
                .orElseThrow(() -> new IllegalArgumentException("Regulation not found with ID: " + id));
        regulation.activate();
        Regulation saved = repository.save(regulation);
        return DtoMapper.toResponse(saved);
    }

    @Override
    public RegulationResponse deprecate(String id) {
        Regulation regulation = repository.findById(RegulationId.fromString(id))
                .orElseThrow(() -> new IllegalArgumentException("Regulation not found with ID: " + id));
        regulation.deprecate();
        Regulation saved = repository.save(regulation);
        return DtoMapper.toResponse(saved);
    }

    @Override
    public RegulationResponse updateFormalSpec(String id, String formalSpec) {
        Regulation regulation = repository.findById(RegulationId.fromString(id))
                .orElseThrow(() -> new IllegalArgumentException("Regulation not found with ID: " + id));
        regulation.updateFormalSpec(formalSpec);
        Regulation saved = repository.save(regulation);
        return DtoMapper.toResponse(saved);
    }

    @Override
    public RegulationResponse updateMetadata(String id, UpdateMetadataCommand command) {
        Regulation regulation = repository.findById(RegulationId.fromString(id))
                .orElseThrow(() -> new IllegalArgumentException("Regulation not found with ID: " + id));
        regulation.updateMetadata(command.name(), command.description(), command.version());
        Regulation saved = repository.save(regulation);
        return DtoMapper.toResponse(saved);
    }

    @Override
    public void deleteDraft(String id) {
        RegulationId regId = RegulationId.fromString(id);
        Regulation regulation = repository.findById(regId)
                .orElseThrow(() -> new IllegalArgumentException("Regulation not found with ID: " + id));
        if (regulation.getStatus() != RegulationStatus.DRAFT) {
            throw new IllegalStateException("Only regulations in DRAFT status can be deleted.");
        }
        repository.deleteById(regId);
    }
}
