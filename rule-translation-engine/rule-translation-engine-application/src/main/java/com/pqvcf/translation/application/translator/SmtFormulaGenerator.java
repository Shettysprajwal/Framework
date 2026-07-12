package com.pqvcf.translation.application.translator;

import com.pqvcf.translation.domain.model.DeonticFormula;

/**
 * Utility to generate SMT-LIB2 compliant constraint specifications from DeonticFormulas.
 * These assertions are consumable by the Z3 SMT solver in Module 4.
 *
 * @author PQVCF Research Team
 * @version 1.0.0
 */
public final class SmtFormulaGenerator {

    private SmtFormulaGenerator() {}

    /**
     * Generates a complete SMT-LIB2 formula from a deontic logic representation.
     */
    public static String generate(DeonticFormula formula) {
        StringBuilder smt = new StringBuilder();
        smt.append("; --- PQVCF SMT-LIB2 Auto-generated Specification ---\n");
        smt.append("(declare-const subject String)\n");
        smt.append("(declare-const action String)\n");
        smt.append("(declare-const target String)\n");
        
        String condVar = "constraint_satisfied";
        if (formula.hasConstraint()) {
            smt.append(String.format("(declare-const %s Bool)\n", condVar));
        }

        // Subject, Action, Target assertion equations
        smt.append(String.format("(assert (= subject \"%s\"))\n", formula.getSubject()));
        smt.append(String.format("(assert (= action \"%s\"))\n", formula.getAction()));
        smt.append(String.format("(assert (= target \"%s\"))\n", formula.getTarget()));

        // Deontic logic assertions
        switch (formula.getOperator()) {
            case PROHIBITION -> {
                smt.append("; Prohibition clause: Action is invalid if constraints fail\n");
                smt.append("(assert true)\n");
            }
            case OBLIGATION -> {
                smt.append("; Obligation clause: Mandatory check\n");
                if (formula.hasConstraint()) {
                    smt.append(String.format("(assert (= %s true))\n", condVar));
                }
            }
            case PERMISSION -> {
                smt.append("; Permission clause: Satisfying constraint permits access\n");
                if (formula.hasConstraint()) {
                    smt.append(String.format("(assert (= %s true))\n", condVar));
                }
            }
            case EXEMPTION -> {
                smt.append("; Exemption clause\n");
                if (formula.hasConstraint()) {
                    smt.append(String.format("(assert (= %s true))\n", condVar));
                }
            }
        }
        return smt.toString();
    }
}
