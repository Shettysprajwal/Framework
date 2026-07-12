package com.pqvcf.pdp.domain.solver;

import com.pqvcf.pdp.domain.model.DecisionResult;

/**
 * Port interface defining formal Z3 SMT-based solvers.
 *
 * @author PQVCF Research Team
 * @version 1.0.0
 */
public interface SmtComplianceSolver {

    /**
     * Executes mathematical satisfiability checks on the compiled SMT formula.
     */
    DecisionResult solve(String smtFormula);
}
