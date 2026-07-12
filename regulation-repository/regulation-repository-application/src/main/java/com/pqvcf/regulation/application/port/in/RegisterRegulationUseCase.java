package com.pqvcf.regulation.application.port.in;

import com.pqvcf.regulation.application.dto.RegulationResponse;

public interface RegisterRegulationUseCase {
    RegulationResponse register(RegisterRegulationCommand command);

    record RegisterRegulationCommand(
            String name,
            String shortName,
            String jurisdiction,
            String version,
            String description
    ) {}
}
