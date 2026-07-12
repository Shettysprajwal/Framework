package com.pqvcf.regulation.application.service;

import com.pqvcf.regulation.application.dto.RegulationResponse;
import com.pqvcf.regulation.application.port.in.RegisterRegulationUseCase.RegisterRegulationCommand;
import com.pqvcf.regulation.domain.model.Regulation;
import com.pqvcf.regulation.domain.model.RegulationId;
import com.pqvcf.regulation.domain.model.RegulationStatus;
import com.pqvcf.regulation.domain.repository.RegulationRepository;
import com.pqvcf.shared.types.JurisdictionCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegulationServiceTest {

    @Mock
    private RegulationRepository repository;

    private RegulationService service;

    @BeforeEach
    void setUp() {
        service = new RegulationService(repository);
    }

    @Test
    @DisplayName("Should register new regulation successfully and persist in repository")
    void shouldRegisterRegulation() {
        RegisterRegulationCommand command = new RegisterRegulationCommand(
                "General Data Protection Regulation",
                "GDPR",
                "EU",
                "2016/679",
                "EU privacy protection"
        );

        when(repository.existsByShortName("GDPR")).thenReturn(false);
        when(repository.save(any(Regulation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RegulationResponse response = service.register(command);

        assertThat(response).isNotNull();
        assertThat(response.shortName()).isEqualTo("GDPR");
        assertThat(response.primaryJurisdiction()).isEqualTo("EU");
        assertThat(response.status()).isEqualTo(RegulationStatus.DRAFT.name());

        verify(repository).save(any(Regulation.class));
    }

    @Test
    @DisplayName("Should fail registration when regulation short name is duplicated")
    void shouldFailIfShortNameExists() {
        RegisterRegulationCommand command = new RegisterRegulationCommand(
                "Duplicate Regulation",
                "GDPR",
                "EU",
                "2016/679",
                "Details"
        );

        when(repository.existsByShortName("GDPR")).thenReturn(true);

        assertThatThrownBy(() -> service.register(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("Should activate draft regulation successfully")
    void shouldActivateRegulation() {
        Regulation regulation = Regulation.create(
                "General Data Protection Regulation",
                "GDPR",
                JurisdictionCode.EU,
                "2016/679",
                "EU privacy protection"
        );
        RegulationId regId = regulation.getId();

        when(repository.findById(regId)).thenReturn(Optional.of(regulation));
        when(repository.save(any(Regulation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RegulationResponse response = service.activate(regId.toString());

        assertThat(response.status()).isEqualTo(RegulationStatus.ACTIVE.name());
        assertThat(regulation.getStatus()).isEqualTo(RegulationStatus.ACTIVE);
    }
}
