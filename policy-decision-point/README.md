# Module 5 — Policy Decision Point (PDP)

## 1. Objective
Receive compliance evaluation requests, fetch active policy mappings from PAP, compile context variables resolved by PIP into mathematical assertions, and execute a formal satisfiability verification check using a native Z3 solver.

## 2. Research Motivation
Traditional access control engines (RBAC, ABAC) rely on static rules databases that are difficult to formally audit. By modeling policies as first-order logic formulas and solving them using SMT engines, PDP proves mathematically whether a data transfer action violates international compliance regulations.

## 3. Mathematical Model (Compliance Satisfiability Proof)
Let $\mathbf{C}_{req}$ represent the compiled assertions of the request context attributes (subject role, data classification, etc.), and $\mathbf{R}_{active}$ represent the logical formulas of all linked regulatory rules.
The PDP evaluates compliance by checking the satisfiability of the joint formula:
$$\mathbf{Model} = \mathbf{C}_{req} \land \mathbf{R}_{active}$$
- If $\mathbf{Model}$ is **UNSATISFIABLE** ($\text{unsat}$), it indicates a logical contradiction between the request parameters and regulatory mandates. The evaluation verdict is **DENY**.
- If $\mathbf{Model}$ is **SATISFIABLE** ($\text{sat}$), it indicates the parameters satisfy all constraints. The evaluation verdict is **PERMIT**.

## 4. Assumptions
1. High-level compliance links have been mapped inside PAP (Module 3).
2. Resolved attributes compiled by PIP (Module 4) are accurate representations of the environment status.

## 5. Inputs & Outputs
- **Inputs**: Evaluation query specifying Subject ID, Resource ID, Action ID, and target transfer countries.
- **Outputs**: EvaluateResponse specifying verdict (PERMIT/DENY), compiled SMT proof trace, and solver details.

## 6. Algorithms
### Z3 Satisfiability Check
```text
Algorithm EvaluateCompliance(Request):
    Policies = PAP.FetchActivePolicies()
    Context = PIP.ResolveContext(Request)
    
    SMT_Formula = CompileSmtLIB2(Request, Context, Policies)
    
    SolverResult = Z3.Execute(SMT_Formula)
    if SolverResult == "unsat":
        return DENY
    else if SolverResult == "sat":
        return PERMIT
    return INDETERMINATE
```

## 7. Complexity Analysis
- **SMT Formula Compilation**: $O(\mathcal{A} + \mathcal{K})$ where $\mathcal{A}$ is resolved attributes and $\mathcal{K}$ is linked active rules.
- **Solving time**: Determined by Z3 DPLL(T) solver execution complexity (potentially NP-complete, but optimized heuristics solve compliance contexts in $<10\text{ms}$).

## 8. Security Analysis
- All compliance decisions are logged in a PostgreSQL database as an immutable audit trail ledger to support forensic audits.
- The execution of native CLI Z3 binaries uses sandboxed Java processes to prevent command injection hazards.

## 9. Design Decisions
- **Loose Coupling via gRPC stubs**: Inter-module communication maps across gRPC microservice endpoints (PAP, PIP), enabling independent scaling.
- **In-process Heuristic Fallback**: If a native local `z3` binary is not configured or fails, the solver falls back to in-process logic parsing to keep the API server functioning.

## 10. APIs
- **REST Endpoints**:
  - `POST /api/v1/pdp/evaluate` (Request verdict)
  - `GET /api/v1/pdp/audits` (Query past evaluations)
- **gRPC Services**:
  - `EvaluateCompliance`

## 11. Unit & Integration Tests
- **DecisionResultTest**: Outcome checks.
- **PolicyDecisionServiceTest**: Flow orchestrations check.
- **PdpControllerIT**: MockMvc endpoints integration check.

## 12. Example Execution
1. Client calls `/evaluate` with analyst role transferring records US → IN.
2. PIP returns transitivity check (false).
3. Compiler generates SMT assertions: `(assert (= transitive_adequate false))`.
4. Regulatory constraint: `(assert (=> (= action "transfer") (= transitive_adequate true)))`.
5. Z3 returns `unsat` → PDP verdict is **DENY**.

## 13. Limitations
- Relies on Z3 CLI execution. Distributed high-throughput clouds should use JNI bindings (e.g. Com.microsoft.z3.jar) to prevent process creation overhead.

## 14. Future Improvements
- Implement JWT token authorization context parsers to extract attributes directly from user credentials.
