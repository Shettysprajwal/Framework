package com.pqvcf.translation.application.port.in;

public interface TranslateRuleUseCase {

    TranslationResponse translate(TranslateRuleCommand command);

    record TranslateRuleCommand(
            String regulationShortName,
            String articleNumber,
            String clauseNumber,
            String rawSourceText
    ) {}

    record TranslationResponse(
            String id,
            String regulationShortName,
            String articleNumber,
            String clauseNumber,
            String rawSourceText,
            String deonticOperator,
            String subject,
            String action,
            String target,
            String constraint,
            String smtSpec,
            String odrlPolicy,
            boolean isValid,
            String validationMessage
    ) {}
}
