package com.pqvcf.translation.application.port.in;

import com.pqvcf.translation.application.port.in.TranslateRuleUseCase.TranslationResponse;
import java.util.List;
import java.util.Optional;

public interface GetTranslatedRuleUseCase {
    Optional<TranslationResponse> getById(String id);
    List<TranslationResponse> findByRegulation(String regulationShortName);
    List<TranslationResponse> findByRegulationAndArticle(String regulationShortName, String articleNumber);
    List<TranslationResponse> listAll();
    void delete(String id);
}
