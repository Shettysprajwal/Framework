package com.pqvcf.pip.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ContextAttributeTest {

    @Test
    @DisplayName("Should successfully create a valid ContextAttribute and map data types")
    void shouldCreateContextAttribute() {
        ContextAttribute attr = new ContextAttribute(
                AttributeCategory.SUBJECT,
                "clearance",
                "top-secret",
                "String"
        );

        assertThat(attr).isNotNull();
        assertThat(attr.getCategory()).isEqualTo(AttributeCategory.SUBJECT);
        assertThat(attr.getKey()).isEqualTo("clearance");
        assertThat(attr.getValue()).isEqualTo("top-secret");
        assertThat(attr.getDataType()).isEqualTo("String");
    }

    @Test
    @DisplayName("Should convert types correctly for Boolean and Integer values")
    void shouldConvertDataTypes() {
        ContextAttribute boolAttr = new ContextAttribute(
                AttributeCategory.SUBJECT,
                "mfa_enabled",
                "true",
                "Boolean"
        );
        assertThat(boolAttr.asBoolean()).isTrue();

        ContextAttribute intAttr = new ContextAttribute(
                AttributeCategory.RESOURCE,
                "access_limit",
                "50",
                "Integer"
        );
        assertThat(intAttr.asInteger()).isEqualTo(50);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException if key is empty")
    void shouldThrowIfKeyEmpty() {
        assertThatThrownBy(() -> new ContextAttribute(
                AttributeCategory.ACTION,
                "",
                "transfer",
                "String"
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("key must not be empty");
    }
}
