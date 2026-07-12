package com.pqvcf.regulation.application.port.in;

import com.pqvcf.regulation.application.dto.RegulationResponse;
import java.util.Optional;

public interface GetRegulationUseCase {
    Optional<RegulationResponse> getById(String id);
    Optional<RegulationResponse> getByShortName(String shortName);
}
