package com.pqvcf.regulation.domain.model;

import com.pqvcf.regulation.domain.event.RegulationRegisteredEvent;
import com.pqvcf.shared.types.JurisdictionCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RegulationTest {

    @Test
    @DisplayName("Should successfully create a new regulation in DRAFT status and raise registration event")
    void shouldCreateDraftRegulation() {
        Regulation regulation = Regulation.create(
                "General Data Protection Regulation",
                "GDPR",
                JurisdictionCode.EU,
                "2016/679",
                "EU privacy framework"
        );

        assertThat(regulation).isNotNull();
        assertThat(regulation.getStatus()).isEqualTo(RegulationStatus.DRAFT);
        assertThat(regulation.getShortName()).isEqualTo("GDPR");
        assertThat(regulation.getPrimaryJurisdiction()).isEqualTo(JurisdictionCode.EU);
        
        // Assert domain event raised
        var events = regulation.pullDomainEvents();
        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(RegulationRegisteredEvent.class);
        
        RegulationRegisteredEvent event = (RegulationRegisteredEvent) events.get(0);
        assertThat(event.shortName()).isEqualTo("GDPR");
        assertThat(event.jurisdiction()).isEqualTo(JurisdictionCode.EU);
    }

    @Test
    @DisplayName("Should validate regulation short name length constraints")
    void shouldValidateShortNameLength() {
        assertThatThrownBy(() -> Regulation.create(
                "Long Name",
                "THIS_IS_A_VERY_LONG_SHORT_NAME_THAT_EXCEEDS_FIFTY_CHARACTERS_LIMIT_NORMALLY",
                JurisdictionCode.US,
                "1.0",
                "desc"
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Short name exceeds 50 characters");
    }

    @Test
    @DisplayName("Should transition from DRAFT to ACTIVE and set effective date")
    void shouldTransitionToActive() {
        Regulation regulation = Regulation.create(
                "Digital Personal Data Protection Act",
                "DPDP",
                JurisdictionCode.IN,
                "2023",
                "India DPDP Act"
        );

        assertThat(regulation.getStatus()).isEqualTo(RegulationStatus.DRAFT);
        assertThat(regulation.getEffectiveDate()).isNull();

        regulation.activate();

        assertThat(regulation.getStatus()).isEqualTo(RegulationStatus.ACTIVE);
        assertThat(regulation.getEffectiveDate()).isNotNull().isEqualTo(LocalDate.now());
    }

    @Test
    @DisplayName("Should not allow activating deprecated regulations")
    void shouldNotAllowActivatingDeprecated() {
        Regulation regulation = Regulation.create(
                "Old Act",
                "OLD",
                JurisdictionCode.US,
                "1.0",
                "desc"
        );
        regulation.activate();
        regulation.deprecate();

        assertThatThrownBy(regulation::activate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot activate a deprecated regulation");
    }
}
