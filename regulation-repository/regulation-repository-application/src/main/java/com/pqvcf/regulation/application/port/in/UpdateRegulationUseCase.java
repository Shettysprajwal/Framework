package com.pqvcf.regulation.application.port.in;

import com.pqvcf.regulation.application.dto.RegulationResponse;

public interface UpdateRegulationUseCase {
    RegulationResponse activate(String id);
    RegulationResponse deprecate(String id);
    RegulationResponse updateFormalSpec(String id, String formalSpec);
    RegulationResponse updateMetadata(String id, UpdateMetadataCommand command);
    void deleteDraft(String id);

    record UpdateMetadataCommand(
            String name,
            String description,
            String version
    ) {}
}
