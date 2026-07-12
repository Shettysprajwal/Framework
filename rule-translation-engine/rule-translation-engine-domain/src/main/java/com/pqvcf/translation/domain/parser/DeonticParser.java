package com.pqvcf.translation.domain.parser;

import com.pqvcf.translation.domain.model.DeonticFormula;

/**
 * Port interface for parsing Controlled Natural Language (CNL) strings to deontic formulas.
 *
 * @author PQVCF Research Team
 * @version 1.0.0
 */
public interface DeonticParser {

    /**
     * Parses the raw CNL statement and returns its DeonticFormula AST structure.
     *
     * @param rawText CNL string representation
     * @return the extracted DeonticFormula
     * @throws IllegalArgumentException if parsing fails or text is structurally incorrect
     */
    DeonticFormula parse(String rawText);
}
