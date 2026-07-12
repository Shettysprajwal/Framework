# Module 1 — Regulation Knowledge Repository (RKR)

## 1. Objective
Provide a structured, versioned, machine-readable store of global data movement and access control regulations (GDPR, DPDP, HIPAA) to serve as the mathematical foundation for logical policy checking, formal verification (Module 4), and cryptographic compliance proving (Module 6).

## 2. Research Motivation
Traditional cloud compliance relies on human auditing of qualitative policies. To achieve automated "compliance-by-design", we must represent statutes as machine-executable rules. RKR bridges the gap between text-based laws and SMT logical statements.

## 3. Mathematical Model (Deontic Logic)
Every rule is structured under standard Deontic Logic Operators:
- **Permission** $\mathcal{P}(\alpha, \gamma)$ — actor $\alpha$ is allowed to process data under criteria $\gamma$.
- **Prohibition** $\mathcal{F}(\alpha, \gamma)$ — actor $\alpha$ is forbidden from executing transfer under criteria $\gamma$.
- **Obligation** $\mathcal{O}(\alpha, \gamma)$ — actor $\alpha$ is mandated to enforce safeguard $\gamma$.

## 4. Assumptions
1. Seeding of regulations (via Flyway/SQL) is performed by authorized legal compliance experts.
2. In-memory and graph-based models represent the legal constraints accurately.

## 5. Inputs & Outputs
- **Inputs**: Registration commands (Name, ShortName, Jurisdiction, Version, Description), Article commands (Content, Deontic axioms, ODRL policy JSON).
- **Outputs**: Fully serialized JSON DTOs, ODRL policy objects, raw SMT-LIB2 specifications.

## 6. Algorithms
### Transitive Adequacy Resolution
```text
Algorithm ResolveAdequacyPath(Source, Target, Visited):
    if Source == Target then return True
    if Source in Visited then return False
    add Source to Visited
    for each neighbor in AdequacyAgreements(Source):
        if ResolveAdequacyPath(neighbor, Target, Visited) then
            return True
    return False
```

## 7. Complexity Analysis
- **Graph Traversal (Adequacy Check)**: Time Complexity $O(V + E)$ where $V$ is number of jurisdictions and $E$ is adequacy edges. Space Complexity $O(V)$ for recursive recursion stack.

## 8. Security Analysis
- Relational tables use strict primary key and foreign key reference integrity.
- Graph syncing triggers only after successful Postgres transactional commits (`AFTER_COMMIT` event listener).
- Input verification checks normalized formats (uppercase short_name) to prevent injection risks.

## 9. Design Decisions
- **Relational + Graph dual-storage**: Relational for ACID transactions, Graph for path reasoning (adequacy agreements transitivities).
- **Graceful Neo4j Fallback**: Uses in-memory structures if Neo4j is disabled, ensuring offline testability.

## 10. APIs
- **REST Endpoints**: `/api/v1/regulations`, `/api/v1/regulations/short/{shortName}`, `/api/v1/graph/adequacy`.
- **gRPC Services**: `GetRegulationByShortName`, `CheckJurisdictionAdequacy`.

## 11. Unit & Integration Tests
- **RegulationTest**: Domain aggregate validation logic.
- **RegulationServiceTest**: Use case mock assertions.
- **RegulationControllerIT**: API controller endpoint test.

## 12. Example Execution
1. Client registers draft `GDPR` policy via `POST /api/v1/regulations`.
2. Client appends `Article 46` with SMT formula.
3. Client issues `POST /api/v1/regulations/{id}/activate`.

## 13. Limitations
- Does not automatically parse arbitrary natural language text; requires structured template inputs or NLP preprocessing.

## 14. Future Improvements
- Integrate LLMs in Module 2 to automatically parse PDF statutes directly into SMT-LIB2 formulations.
