package com.pqvcf.regulation.application.port.in;

import com.pqvcf.regulation.application.dto.RegulationResponse;
import java.util.List;

public interface ListRegulationsUseCase {
    List<RegulationResponse> listAll();
    List<RegulationResponse> listByJurisdiction(String jurisdiction);
    List<RegulationResponse> search(String query);
}
