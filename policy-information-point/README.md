# Module 4 — Policy Information Point (PIP)

## 1. Objective
Serve as the centralized context attributes provider for the policy decision engine (Module 5), resolving dynamic subject roles, resource classifications, environmental parameters, and transitive jurisdiction adequacy path safety checks.

## 2. Research Motivation
Modern compliance checks require more than static rule matching; they depend on real-time environmental context. PIP aggregates information across corporate databases, directories, and graph stores to compile a unified authorization payload.

## 3. Mathematical Model (Transitivity Path Resolution)
To verify if data can flow safely between two countries $A$ and $B$, PIP checks for a transitive adequacy pathway in the Graph Store:
$$Adequate(A, B) \iff A = B \lor \exists X_1, X_2, \dots, X_k \text{ s.t. } Path(A \to X_1 \to X_2 \to \dots \to B)$$
Where each edge $\to$ represents a legally recognized adequacy agreement. If such a path exists, PIP flags the environment context attribute `is_transitive_adequate = true`.

## 4. Assumptions
1. Subject attributes (e.g. mfa_enabled, clearance) are linked to unique subject identifiers.
2. Neo4j or in-memory graph stores in Module 1 hold current jurisdiction adequacy configurations.

## 5. Inputs & Outputs
- **Inputs**: Attribute resolution query specifying Subject ID, Resource ID, Action ID, Source Country, and Target Country.
- **Outputs**: Complete resolved context JSON containing typed attributes grouped by category, and path transit safety flags.

## 6. Algorithms
### Cache Resolution
```text
Algorithm ResolveAttributes(SubjectId, ResourceId):
    SubjectAttrs = SubjectCache.GetOrDefault(SubjectId, DefaultSubjectAttrs)
    ResourceAttrs = ResourceCache.GetOrDefault(ResourceId, DefaultResourceAttrs)
    return Merge(SubjectAttrs, ResourceAttrs)
```

## 7. Complexity Analysis
- **Local Attribute Cache Lookup**: $O(1)$ query time.
- **Adequacy Path Query**: $O(V + E)$ where $V$ is jurisdictions and $E$ is adequacy agreements.

## 8. Security Analysis
- The local concurrent map cache simulates enterprise caching layers.
- Admin endpoints that allow caching overrides require network isolation or appropriate API scopes to prevent unauthorized attribute tampering.

## 9. Design Decisions
- **Configurable Fallbacks**: If Module 1's gRPC channel is offline, the resolver uses a pre-configured local transitivity map to prevent blocking system evaluations.
- **Unified Attribute Category**: Context attributes map directly to standard XACML categorizations (Subject, Resource, Action, Environment).

## 10. APIs
- **REST Endpoints**:
  - `POST /api/v1/pip/resolve` (Query resolution)
  - `POST /api/v1/pip/attributes/subject` (Cache injection)
- **gRPC Services**:
  - `ResolveRequestContext`

## 11. Unit & Integration Tests
- **ContextAttributeTest**: Cast validations.
- **AttributeResolutionServiceTest**: Application service mock verification.
- **PipControllerIT**: MockMvc integration checking controller overrides.

## 12. Example Execution
1. Post resolve request: `{"subjectId": "analyst", "resourceId": "health-records", "sourceCountry": "IN", "targetCountry": "EU"}`.
2. PIP fetches analyst role and health records classification from cache.
3. Checks IN → EU transitivity (resolves true).
4. Returns combined resolved attributes payload.

## 13. Limitations
- Basic concurrent caching structure. Production environments should use distributed redis instances with automated eviction policies.

## 14. Future Improvements
- Integrate with LDAP / Active Directory to fetch user groups and metadata automatically.
