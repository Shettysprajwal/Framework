package com.pqvcf.translation.application.service;

import com.pqvcf.translation.application.port.in.TranslateRuleUseCase.TranslateRuleCommand;
import com.pqvcf.translation.application.port.in.TranslateRuleUseCase.TranslationResponse;
import com.pqvcf.translation.domain.model.DeonticFormula;
import com.pqvcf.translation.domain.model.DeonticOperator;
import com.pqvcf.translation.domain.model.LegalRule;
import com.pqvcf.translation.domain.parser.DeonticParser;
import com.pqvcf.translation.domain.repository.RuleTranslationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RuleTranslationServiceTest {

    @Mock
    private RuleTranslationRepository repository;

    @Mock
    private DeonticParser cnlParser;

    private RuleTranslationService service;

    @BeforeEach
    void setUp() {
        service = new RuleTranslationService(repository, cnlParser);
    }

    @Test
    @DisplayName("Should translate a valid CNL rule successfully and call SMT and ODRL generators")
    void shouldTranslateRule() {
        TranslateRuleCommand command = new TranslateRuleCommand(
                "GDPR",
                "Article 46",
                "1",
                "a controller may transfer personal data if standard contractual clauses are signed"
        );

        DeonticFormula formula = new DeonticFormula(
                DeonticOperator.PERMISSION,
                "controller",
                "transfer",
                "personal_data",
                "standard contractual clauses are signed"
        );

        when(cnlParser.parse(command.rawSourceText())).thenReturn(formula);
        when(repository.findByRegulation("GDPR")).thenReturn(List.of());
        when(repository.save(any(LegalRule.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TranslationResponse response = service.translate(command);

        assertThat(response).isNotNull();
        assertThat(response.regulationShortName()).isEqualTo("GDPR");
        assertThat(response.deonticOperator()).isEqualTo("PERMISSION");
        assertThat(response.subject()).isEqualTo("controller");
        assertThat(response.action()).isEqualTo("transfer");
        assertThat(response.target()).isEqualTo("personal_data");
        assertThat(response.constraint()).contains("standard contractual clauses");
        assertThat(response.smtSpec()).contains("(= action \"transfer\")");
        assertThat(response.odrlPolicy()).contains("\"action\": \"transfer\"");
        assertThat(response.isValid()).isTrue();

        verify(repository).save(any(LegalRule.class));
    }

    @Test
    @DisplayName("Should flag validation conflict if candidate rule contradicts an existing persisted rule")
    void shouldDetectLogicalConflict() {
        TranslateRuleCommand command = new TranslateRuleCommand(
                "GDPR",
                "Article 46",
                "1",
                "a controller may transfer personal data"
        );

        DeonticFormula candidateFormula = new DeonticFormula(
                DeonticOperator.PERMISSION,
                "controller",
                "transfer",
                "personal_data",
                ""
        );

        // Pre-existing conflicting prohibition rule
        DeonticFormula existingFormula = new DeonticFormula(
                DeonticOperator.PROHIBITION,
                "controller",
                "transfer",
                "personal_data",
                ""
        );
        LegalRule existingRule = LegalRule.create(
                "GDPR", "Article 44", "1", "a controller must not transfer personal data",
                existingFormula, "smt", "odrl"
        );

        when(cnlParser.parse(command.rawSourceText())).thenReturn(candidateFormula);
        when(repository.findByRegulation("GDPR")).thenReturn(List.of(existingRule));
        when(repository.save(any(LegalRule.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TranslationResponse response = service.translate(command);

        assertThat(response.isValid()).isFalse();
        assertThat(response.validationMessage()).contains("Logical Conflict");
    }
}
