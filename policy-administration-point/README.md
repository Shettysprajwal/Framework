# Module 3 — Policy Administration Point (PAP)

## 1. Objective
Enable organizational policy authoring (such as enterprise data transfer policies or data retention standards) and support linking these high-level policies to regulatory rule statements parsed by Module 2 (Rule Translation Engine) to enable continuous, unified compliance audits.

## 2. Research Motivation
Organizational compliance is often disconnected from the actual legal statutes that govern the business. To achieve automated policy checks, PAP supports binding internal organizational rules directly to parsed regulatory formulas (W3C ODRL / Z3 SMT assertions), creating a traceable validation link.

## 3. Mathematical Model (Rule Binding mapping)
Let $\mathcal{P}$ represent the set of organizational policies, and $\mathcal{R}$ represent the set of regulatory rules translated in Module 2.
A Rule Link binding $L$ is represented as a mapping:
$$L = \langle P_{id}, Rule_{org}, Rule_{reg\_id} \rangle$$
Where:
- $P_{id} \in \mathcal{P}$ represents the organizational policy.
- $Rule_{org}$ is the descriptive name of the internal corporate rule.
- $Rule_{reg\_id} \in \mathcal{R}$ is the UUID of the matching regulatory deontic formula translated by Module 2.

## 4. Assumptions
1. High-level compliance policies are authored by corporate legal officers or IT administrators.
2. The rules referenced by regulatory IDs have already been parsed and validated for consistency in Module 2.

## 5. Inputs & Outputs
- **Inputs**: Policy authoring requests (Name, Owner, Description), rule link binding requests (PolicyId, OrgRuleName, RegulatoryRuleId).
- **Outputs**: PolicyResponse JSON DTOs, including nested RuleLink collections and active lifecycle status badges.

## 6. Algorithms
### Static Policy Validation
During link creation, the engine verifies that the linked regulatory rules belong to active regulations before allowing the policy to transition from `DRAFT` to `ACTIVE`.

## 7. Complexity Analysis
- **Rule Linking**: $O(1)$ database insertion time.
- **Ledger retrieval**: $O(N)$ database query time where $N$ is the number of authored organizational policies.

## 8. Security Analysis
- The SQL tables enforce unique constraints to prevent linking duplicate organizational rules under the same policy scope.
- State changes (`activate()` and `deprecate()`) trigger the emission of `PolicyPublishedEvent` objects, allowing downstream enforcement modules (PDP) to securely rebuild policy access caches.

## 9. Design Decisions
- **Loose Coupling via UUID Links**: Policies reference regulatory rules by UUID, allowing the policy engine to remain decoupled from direct schema mappings of Module 1 & 2 databases.
- **Explicit Lifecycle Guardrails**: Rules can only be linked/unlinked when a policy is in the `DRAFT` phase, preventing dynamic alterations to active compliance pipelines.

## 10. APIs
- **REST Endpoints**:
  - `POST /api/v1/policies` (Create policy)
  - `POST /api/v1/policies/{id}/links` (Bind rule link mapping)
  - `DELETE /api/v1/policies/{id}/links/{linkId}` (Remove rule link binding)
  - `POST /api/v1/policies/{id}/activate` (Publish policy)
- **gRPC Services**:
  - `CreateCompliancePolicy`
  - `GetActivePolicies`

## 11. Unit & Integration Tests
- **PolicyTest**: Domain aggregate validation logic.
- **PolicyAdministrationServiceTest**: Application service mock verification.
- **PolicyControllerIT**: Integration test for REST mappings and H2 persistence.

## 12. Example Execution
1. Client creates a "Global HIPAA Access Control Policy" (`POST /api/v1/policies`).
2. Client binds an internal rule "Patient record access control" to the translated rule of HIPAA §164.312 (Article ID).
3. Client transitions policy status to `ACTIVE`, firing a publication event.

## 13. Limitations
- Does not verify that the internal organizational rule *logically* satisfies the SMT spec of the regulatory rule. That mathematical proof is performed by the PDP (Module 5) at request time.

## 14. Future Improvements
- Integrate policy authoring with version control control systems (Git) to maintain complete historical commit logs of corporate policies.
