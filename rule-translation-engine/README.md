# Module 2 — Legal Rule Translation Engine (LRTE)

## 1. Objective
Provide a semantic translation framework that converts legal regulatory mandates (written in Controlled Natural Language - CNL) into structured Deontic Logic representation, W3C ODRL policy JSONs, and Z3 SMT-LIB2 format constraints for mathematical verification.

## 2. Research Motivation
Bridging natural language law (highly ambiguous and interpretive) to formal SMT models requires a structured intermediate syntax. LRTE introduces a machine-assisted pipeline translating statutory text into precise deontic logic statements to prove compliance automatically.

## 3. Mathematical Model (Deontic Logic AST)
Legal statements are translated to a 5-tuple abstract syntax representation:
$$\text{Formula} = \langle \mathcal{O}, S, A, T, \mathcal{C} \rangle$$
Where:
- $\mathcal{O} \in \{ \text{Permission}, \text{Obligation}, \text{Prohibition}, \text{Exemption} \}$ (Deontic modal operators)
- $S$: Subject actor (e.g., "controller", "processor", "fiduciary")
- $A$: Action verb (e.g., "transfer", "process", "store")
- $T$: Target data asset classification (e.g., "personal data", "health records")
- $\mathcal{C}$: Mathematical constraint conditions (e.g., "safeguards signed", "consent present")

## 4. Assumptions
1. Input legal text is written or normalized to follow Controlled Natural Language (CNL) grammatical constructs.
2. Direct contradictions represent logical flaws in the underlying regulations and must be flagged rather than evaluated.

## 5. Inputs & Outputs
- **Inputs**: CNL statement strings, regulation identifier, article index.
- **Outputs**:
  - Deontic Logic tuple (`operator`, `subject`, `action`, `target`, `constraint`).
  - SMT-LIB2 code containing type declarations and constraint asset assertions.
  - W3C ODRL JSON-LD policies.

## 6. Algorithms
### Logical Contradiction Checking
```text
Algorithm CheckContradiction(Candidate, RuleLedger):
    for each rule in RuleLedger:
        if (rule.subject == Candidate.subject AND 
            rule.action == Candidate.action AND 
            rule.target == Candidate.target):
            
            if (rule.operator == PROHIBITION and Candidate.operator == PERMISSION) OR
               (rule.operator == PERMISSION and Candidate.operator == PROHIBITION):
                if Overlap(rule.constraint, Candidate.constraint):
                    return CONFLICT_FOUND
    return CONSISTENT
```

## 7. Complexity Analysis
- **Translation time**: $O(1)$ for regex parsing fallbacks, $O(N)$ for remote NLP microservice dependency classification.
- **Conflict validation scan**: $O(K)$ where $K$ is the number of active rules stored in database for target regulation.

## 8. Security Analysis
- The SQL schema uses strict indices to prevent overlapping entries.
- The Python NLP bridge enforces network boundaries (isolated container mapping).
- Local fallback parser secures system availability if the remote Python extractor goes offline.

## 9. Design Decisions
- **Decoupled Python NLP client**: Outsources AI parsing logic to Python (natural choice for spaCy) while maintaining core business validation inside Java.
- **W3C ODRL mapping**: Encodes policies using the international open standard for digital rights management to enable enterprise cloud compliance interoperability.

## 10. APIs
- **REST Endpoints**:
  - `/api/v1/translation/translate` (POST translation task)
  - `/api/v1/translation/rules/regulation/{regulation}` (GET rules)
- **gRPC Services**:
  - `TranslateCNLRule`
  - `GetRulesForRegulation`

## 11. Unit & Integration Tests
- **DeonticFormulaTest**: Verifies AST value structures.
- **RuleTranslationServiceTest**: Verifies use-case orchestrations and contradiction detection.
- **RuleTranslationControllerIT**: Integrates REST API layer and Postgres H2 checks.

## 12. Example Execution
1. Send POST request with CNL text: `"a controller is forbidden to transfer personal data"`
2. Engine processes text → Extracts `PROHIBITION` operator on action `transfer`.
3. SMT generator outputs `(assert (= action "transfer"))`.
4. Conflict checker returns validation status: `isValid = true` (or `false` if contradictory permission exists).

## 13. Limitations
- Basic parser matches standard CNL structure (Subject modalVerb Action Target). Highly complex nested legal prose may require pre-structuring by policy analysts.

## 14. Future Improvements
- Train a fine-tuned legal-BERT transformer model inside the Python service to directly parse semantic dependency trees of raw law documents.
