# PROJECT_ARCHITECTURE.md
## Post-Quantum Verifiable Compliance Framework (PQVCF)
### Complete System Architecture — Single Source of Truth

---

> **Who is this for?**
> This document is written so that a developer joining the project for the first time, a researcher reviewing the prototype, or a system administrator deploying it can all understand the *entire system* from start to finish — without needing to read every source file first.

---

## Table of Contents

1. [What is PQVCF?](#1-what-is-pqvcf)
2. [The Core Research Question](#2-the-core-research-question)
3. [High-Level Architecture Overview](#3-high-level-architecture-overview)
4. [Complete Folder Structure](#4-complete-folder-structure)
5. [Module Catalogue](#5-module-catalogue)
6. [The Clean Architecture Pattern (Used in Every Module)](#6-the-clean-architecture-pattern-used-in-every-module)
7. [Data Flow — How a Compliance Request Travels Through the System](#7-data-flow--how-a-compliance-request-travels-through-the-system)
8. [Request Lifecycle — Step by Step](#8-request-lifecycle--step-by-step)
9. [Database Design](#9-database-design)
10. [API Reference — All Endpoints](#10-api-reference--all-endpoints)
11. [Security Architecture](#11-security-architecture)
12. [Cryptographic Components — Post-Quantum Cryptography](#12-cryptographic-components--post-quantum-cryptography)
13. [Zero-Knowledge Proof Workflow](#13-zero-knowledge-proof-workflow)
14. [Formal Verification Process](#14-formal-verification-process)
15. [The NLP Rule Extraction Pipeline](#15-the-nlp-rule-extraction-pipeline)
16. [Frontend Architecture](#16-frontend-architecture)
17. [Frontend–Backend Interaction](#17-frontendbackend-interaction)
18. [Inter-Service Communication (gRPC)](#18-inter-service-communication-grpc)
19. [Deployment Architecture](#19-deployment-architecture)
20. [Technology Stack Reference](#20-technology-stack-reference)
21. [End-to-End Worked Example](#21-end-to-end-worked-example)
22. [Testing Strategy](#22-testing-strategy)
23. [Performance Characteristics](#23-performance-characteristics)
24. [How Every Component Works Together](#24-how-every-component-works-together)
25. [Extending the System](#25-extending-the-system)
26. [Glossary](#26-glossary)

---

## 1. What is PQVCF?

**PQVCF** stands for **Post-Quantum Verifiable Compliance Framework for Multi-Jurisdiction Cloud Data Systems**.

In simple terms, it is a research-grade software framework that answers this practical question:

> *"Can a cloud company mathematically **prove** it is following every data privacy law (GDPR, HIPAA, DPDP…) when moving data across borders — without revealing its confidential server setup — and remain secure even if quantum computers exist in the future?"*

PQVCF achieves this through three groundbreaking techniques working together:

| Technique | What it Does | Why it Matters |
|---|---|---|
| **Post-Quantum Cryptography (PQC)** | Signs compliance proofs using algorithms quantum computers cannot break | Compliance evidence stays valid decades into the future |
| **Zero-Knowledge Proofs (ZKP)** | Proves a statement is true without revealing the underlying secret data | Cloud providers can prove compliance without exposing infrastructure secrets |
| **Formal Verification (SMT/Z3)** | Uses mathematical logic solvers to *prove* a system obeys a rule — not just test it | Goes beyond testing: mathematically guarantees correctness |

The framework is designed to be a **research prototype** — every algorithm is formally documented, every result is reproducible, and the entire system is publishable at top-tier conferences.

---

## 2. The Core Research Question

> *Can cloud providers mathematically prove that cross-border data movement continuously complies with multiple international regulations while preserving confidentiality and remaining secure against quantum adversaries?*

**PQVCF demonstrates that the answer is: Yes.** It provides the first integrated prototype that combines regulatory machine-encoding, deontic logic, SMT solving, zero-knowledge proofs, and post-quantum signatures into one unified compliance framework.

---

## 3. High-Level Architecture Overview

The system is structured as a **microservices architecture** with 10 backend services (Java/Spring Boot), one Python NLP microservice, and one React frontend dashboard. All services communicate over REST APIs (external) and gRPC (internal).

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                        PRESENTATION LAYER                                     │
│  Research Dashboard Frontend (React/TypeScript on port 80)                    │
│  Nginx reverse proxy  ·  Swagger UI on every service                          │
└───────────────────────────────┬──────────────────────────────────────────────┘
                                │  REST HTTP/JSON
┌───────────────────────────────▼──────────────────────────────────────────────┐
│                        APPLICATION LAYER (10 Microservices)                   │
│                                                                                │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │  Regulation  │  │ Rule Trans-  │  │   Policy     │  │   Policy     │     │
│  │  Repository  │  │ lation Eng.  │  │  Admin (PAP) │  │  Info (PIP)  │     │
│  │   :8081      │  │   :8082      │  │   :8083      │  │   :8084      │     │
│  └──────────────┘  └──────────────┘  └──────────────┘  └──────────────┘     │
│                                                                                │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │  Policy Dec. │  │  PQC Crypto  │  │  ZK Proof    │  │ Data Govern. │     │
│  │  Pt. (PDP)   │  │   Layer      │  │   Engine     │  │   Engine     │     │
│  │   :8085      │  │   :8086      │  │   :8087      │  │   :8088      │     │
│  └──────────────┘  └──────────────┘  └──────────────┘  └──────────────┘     │
│                                                                                │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────────────────────┐   │
│  │  Compliance  │  │  Auditing    │  │  NLP Rule Extractor (Python)      │   │
│  │  Monitor     │  │  Ledger      │  │  Flask on :5001                   │   │
│  │   :8089      │  │   :8090      │  └──────────────────────────────────┘   │
│  └──────────────┘  └──────────────┘                                          │
└───────────────────────────────┬──────────────────────────────────────────────┘
                                │  JDBC / Bolt protocol
┌───────────────────────────────▼──────────────────────────────────────────────┐
│                        PERSISTENCE LAYER                                       │
│  PostgreSQL :5432 (relational — regulations, policies, keys, proofs, audits)  │
│  Neo4j      :7474/:7687 (graph — jurisdiction adequacy relationships)          │
└──────────────────────────────────────────────────────────────────────────────┘
```

### Port Map at a Glance

| Port | Service | Technology |
|------|---------|------------|
| `80` | Research Dashboard Frontend | React + Nginx |
| `5001` | NLP Rule Extractor | Python / Flask |
| `8081` | Regulation Repository API | Java / Spring Boot |
| `8082` | Rule Translation Engine API | Java / Spring Boot |
| `8083` | Policy Administration Point API | Java / Spring Boot |
| `8084` | Policy Information Point API | Java / Spring Boot |
| `8085` | Policy Decision Point API | Java / Spring Boot |
| `8086` | PQC Crypto Layer API | Java / Spring Boot |
| `8087` | ZK Proof Engine API | Java / Spring Boot |
| `8088` | Data Governance Engine API | Java / Spring Boot |
| `8089` | Compliance Monitor API | Java / Spring Boot |
| `8090` | Auditing Ledger API | Java / Spring Boot |
| `5432` | PostgreSQL | Database |
| `7474` | Neo4j Browser | Graph Database |
| `7687` | Neo4j Bolt | Graph Database |

---

## 4. Complete Folder Structure

```
Framework/                                   ← Root of the entire project
│
├── pom.xml                                  ← Maven parent POM (manages all Java modules)
├── README.md                                ← Master implementation prompt
├── PROJECT_ARCHITECTURE.md                  ← THIS FILE (single source of truth)
├── verify_framework.ps1                     ← PowerShell smoke-test script
│
├── pqvcf-shared-kernel/                     ← Shared domain types used across modules
│   └── src/main/java/com/pqvcf/shared/      ← Common value objects, events, exceptions
│
├── regulation-repository/                   ← Module 1: Regulation Knowledge Repository
│   ├── regulation-repository-domain/        ← Domain model (entities, value objects)
│   ├── regulation-repository-application/   ← Use cases, ports (interfaces)
│   ├── regulation-repository-infrastructure/← JPA adapters, repositories, Flyway migrations
│   └── regulation-repository-api/           ← Spring Boot app, REST controllers, security config
│
├── rule-translation-engine/                 ← Module 2: Legal Rule Translation Engine
│   ├── rule-translation-engine-domain/
│   ├── rule-translation-engine-application/
│   ├── rule-translation-engine-infrastructure/
│   └── rule-translation-engine-api/
│
├── policy-administration-point/             ← Module 3: Policy Administration Point (PAP)
│   ├── policy-administration-point-domain/
│   ├── policy-administration-point-application/
│   ├── policy-administration-point-infrastructure/
│   └── policy-administration-point-api/
│
├── policy-information-point/                ← Module 4: Policy Information Point (PIP)
│   ├── policy-information-point-domain/
│   ├── policy-information-point-application/
│   ├── policy-information-point-infrastructure/
│   └── policy-information-point-api/
│
├── policy-decision-point/                   ← Module 5: Policy Decision Point (PDP / Z3 solver)
│   ├── policy-decision-point-domain/
│   ├── policy-decision-point-application/
│   ├── policy-decision-point-infrastructure/
│   └── policy-decision-point-api/
│
├── pqc-crypto-layer/                        ← Module 6: Post-Quantum Crypto Layer
│   ├── pqc-crypto-layer-domain/
│   ├── pqc-crypto-layer-application/
│   ├── pqc-crypto-layer-infrastructure/
│   └── pqc-crypto-layer-api/
│
├── zk-proof-engine/                         ← Module 7: Zero-Knowledge Proof Engine
│   ├── zk-proof-engine-domain/
│   ├── zk-proof-engine-application/
│   ├── zk-proof-engine-infrastructure/
│   └── zk-proof-engine-api/
│
├── data-governance-engine/                  ← Module 8: Cross-Border Data Governance
│   ├── data-governance-engine-domain/
│   ├── data-governance-engine-application/
│   ├── data-governance-engine-infrastructure/
│   └── data-governance-engine-api/
│
├── compliance-monitor/                      ← Module 9: Continuous Compliance Monitor
│   ├── compliance-monitor-domain/
│   ├── compliance-monitor-application/
│   ├── compliance-monitor-infrastructure/
│   └── compliance-monitor-api/
│
├── auditing-ledger/                         ← Module 10: Compliance Auditing Ledger
│   ├── auditing-ledger-domain/
│   ├── auditing-ledger-application/
│   ├── auditing-ledger-infrastructure/
│   └── auditing-ledger-api/
│
├── nlp-rule-extractor/                      ← Python NLP microservice
│   ├── app.py                               ← Flask REST API, regex/deontic keyword parser
│   └── requirements.txt                     ← Flask dependency
│
├── research-dashboard-frontend/             ← React + TypeScript research dashboard
│   ├── src/
│   │   ├── App.tsx                          ← Router definition, all page routes
│   │   ├── main.tsx                         ← React entry point
│   │   ├── index.css                        ← Global CSS styles
│   │   ├── api/                             ← One file per backend service (axios calls)
│   │   │   ├── regulationsApi.ts
│   │   │   ├── translationApi.ts
│   │   │   ├── policyApi.ts
│   │   │   ├── pipApi.ts
│   │   │   ├── pdpApi.ts
│   │   │   ├── pqcApi.ts
│   │   │   ├── zkpApi.ts
│   │   │   ├── governanceApi.ts
│   │   │   ├── monitorApi.ts
│   │   │   └── ledgerApi.ts
│   │   ├── components/                      ← Reusable UI components (Layout, etc.)
│   │   ├── pages/                           ← One page per module (14 pages total)
│   │   └── types/                           ← TypeScript type definitions
│   ├── package.json
│   ├── vite.config.ts
│   └── tsconfig.json
│
└── docker/                                  ← All Docker build files & Docker Compose
    ├── docker-compose.yml                   ← Full system orchestration
    ├── nginx.conf                           ← Frontend Nginx reverse proxy config
    ├── Dockerfile.regulation-api
    ├── Dockerfile.translation-api
    ├── Dockerfile.policy-api
    ├── Dockerfile.pip-api
    ├── Dockerfile.pdp-api
    ├── Dockerfile.pqc-api
    ├── Dockerfile.zk-api
    ├── Dockerfile.governance-api
    ├── Dockerfile.monitor-api
    ├── Dockerfile.ledger-api
    ├── Dockerfile.nlp-extractor
    └── Dockerfile.regulation-frontend
```

---

## 5. Module Catalogue

Every Java backend module is organized identically using **Clean Architecture**. Below is what each module does and where it fits.

| # | Module Folder | Module Name | Short Role | Port |
|---|---|---|---|---|
| 1 | `regulation-repository` | Regulation Knowledge Repository | Stores laws (GDPR, HIPAA, DPDP) as machine-readable SMT formulas | `8081` |
| 2 | `rule-translation-engine` | Legal Rule Translation Engine | Converts legal text → deontic logic + SMT-LIB2 + ODRL | `8082` |
| 3 | `policy-administration-point` | Policy Administration Point (PAP) | Authors organizational compliance policies; links them to regulatory rules | `8083` |
| 4 | `policy-information-point` | Policy Information Point (PIP) | Resolves dynamic context attributes (subject role, resource classification, jurisdiction paths) | `8084` |
| 5 | `policy-decision-point` | Policy Decision Point (PDP) | Evaluates compliance using the Z3 SMT solver → PERMIT or DENY | `8085` |
| 6 | `pqc-crypto-layer` | Post-Quantum Crypto Layer | Generates ML-KEM / ML-DSA / SLH-DSA keys; signs and verifies compliance proofs | `8086` |
| 7 | `zk-proof-engine` | Zero-Knowledge Proof Engine | Creates and verifies Pedersen commitment / Schnorr-Sigma ZK proofs | `8087` |
| 8 | `data-governance-engine` | Cross-Border Data Governance | Decides if a cross-border data transfer is APPROVED / CONDITIONAL / BLOCKED | `8088` |
| 9 | `compliance-monitor` | Continuous Compliance Monitor | Ingests real-time cloud traffic events; raises alerts; tracks rolling SLA | `8089` |
| 10 | `auditing-ledger` | Compliance Auditing Ledger | Hash-chained tamper-evident ledger of all compliance decisions | `8090` |
| — | `nlp-rule-extractor` | NLP Rule Extractor | Python / Flask service; parses CNL regulatory text with regex + deontic keywords | `5001` |
| — | `research-dashboard-frontend` | Research Dashboard | React SPA; visualizes all 10 modules in real-time | `80` |

---

## 6. The Clean Architecture Pattern (Used in Every Module)

Every Java module applies the same four-layer **Clean Architecture** pattern. This keeps business logic isolated from databases, frameworks, and external services.

```
┌────────────────────────────────────────────────┐
│  API Layer  (-api submodule)                   │
│  • Spring Boot application entry point         │
│  • REST Controllers (annotated with @RestController) │
│  • Security configuration (CORS, stateless)    │
│  • OpenAPI/Swagger documentation               │
│  • gRPC server if module exposes gRPC          │
└──────────────────────┬─────────────────────────┘
                       │ calls Use Cases (interfaces)
┌──────────────────────▼─────────────────────────┐
│  Application Layer  (-application submodule)   │
│  • Use Case interfaces (ports "in")            │
│  • Use Case implementations (services)         │
│  • DTOs (Request/Response transfer objects)    │
│  • Domain event handlers                       │
│  • Mappers (MapStruct) domain↔DTO              │
└──────────────────────┬─────────────────────────┘
                       │ calls domain model, invokes ports "out"
┌──────────────────────▼─────────────────────────┐
│  Domain Layer  (-domain submodule)             │
│  • Pure Java entities (no Spring annotations)  │
│  • Value objects                               │
│  • Business rules and invariants               │
│  • Repository port interfaces (out)            │
│  • Domain events                               │
└──────────────────────┬─────────────────────────┘
                       │ implements repository ports
┌──────────────────────▼─────────────────────────┐
│  Infrastructure Layer  (-infrastructure submodule) │
│  • JPA entities and Spring Data repositories   │
│  • Flyway SQL migration scripts                │
│  • gRPC client stubs (calling other services)  │
│  • External service adapters (NLP, Z3, Neo4j)  │
│  • Converters (domain entity ↔ JPA entity)     │
└────────────────────────────────────────────────┘
```

### Concrete Example: Adding an Article to a Regulation

```
HTTP POST /api/v1/regulations/articles
    ↓
ArticleController (API layer)
    ↓ calls interface
AddArticleUseCase (Application port — interface)
    ↓ implemented by
AddArticleService (Application service)
    ↓ loads via
RegulationRepository (Domain port — interface)
    ↓ implemented by
JpaRegulationRepositoryAdapter (Infrastructure)
    ↓ queries
SpringDataRegulationRepository (Spring Data JPA)
    ↓ hits
PostgreSQL database
```

**Why this matters:** You can swap PostgreSQL for any other database by only changing the Infrastructure layer. The Domain layer never knows what database is being used.

---

## 7. Data Flow — How a Compliance Request Travels Through the System

The primary scenario is: *"Is this data transfer from Country A to Country B legally permitted?"*

```
         User/System
              │
              │  POST /api/v1/pdp/evaluate
              │  { subjectId, resourceId, action, sourceCountry, targetCountry }
              ▼
    ┌─────────────────┐
    │  PDP  (Module 5)│ ← Receives evaluation request
    └────────┬────────┘
             │
    ┌────────▼────────┐     gRPC: GetActivePolicies
    │  PAP  (Module 3)│ ─────────────────────────── Returns active policies
    └─────────────────┘           + SMT rule formulas
             │
    ┌────────▼────────┐     gRPC: ResolveRequestContext
    │  PIP  (Module 4)│ ─────────────────────────── Returns resolved attributes:
    └─────────────────┘           subject role, data class,
             │                    transitive adequacy path
             │
    ┌────────▼────────────────────────────────┐
    │  PDP: Compile SMT-LIB2 assertion block  │
    │  (assert (= action "transfer"))         │
    │  (assert (= transitive_adequate false)) │
    │  ← policy rule:                         │
    │  (assert (=> (= action "transfer")      │
    │              (= transitive_adequate true))) │
    └────────┬────────────────────────────────┘
             │
    ┌────────▼────────┐
    │  Z3 SMT Solver  │ ← Returns "sat" or "unsat"
    └────────┬────────┘
             │ DENY if unsat, PERMIT if sat
             ▼
    ┌─────────────────────────────────────────┐
    │  PDP: Return EvaluateResponse           │
    │  { verdict: "DENY", smtTrace: "...",    │
    │    solver: "Z3 4.x" }                   │
    └────────┬────────────────────────────────┘
             │
    ┌────────▼─────────────────────┐
    │  Auditing Ledger (Module 10) │ ← Decision sealed into hash chain
    └──────────────────────────────┘
             │
    ┌────────▼──────────────────────┐
    │  PQC Crypto Layer (Module 6)  │ ← Decision proof signed with ML-DSA
    └──────────────────────────────-┘
             │
    ┌────────▼──────────────────────┐
    │  ZK Proof Engine (Module 7)   │ ← Optional: ZK proof generated if
    └───────────────────────────────┘     confidential infrastructure involved
             │
    ┌────────▼──────────────────────┐
    │  Response returned to client  │
    └───────────────────────────────┘
```

---

## 8. Request Lifecycle — Step by Step

This section traces **every** action the system takes for a complete compliance verification, from the moment the frontend button is clicked to the moment the signed proof is stored.

### Step 1 — Legal Text Ingestion (one-time setup)

1. A legal expert opens the dashboard → **Regulations** page.
2. They click **Register Regulation** and fill in details (name: "GDPR", jurisdiction: "EU", version: "2018").
3. The frontend sends `POST /api/v1/regulations` to **Module 1 (Regulation Repository)**.
4. The API layer validates the request and calls the `RegisterRegulationUseCase`.
5. The domain creates a `Regulation` aggregate with DRAFT status and fires a `RegulationRegisteredEvent`.
6. Flyway-managed PostgreSQL tables (`regulations`, `articles`, `clauses`) store the data.
7. A post-commit event listener syncs the jurisdiction node to **Neo4j**.

### Step 2 — Article Encoding (deontic formula)

1. The expert adds GDPR Article 46 with content and a deontic formula:
   ```
   O(controller, ensure_safeguards) ∧ ¬GDPR_Art45(dest) → P(controller, transfer, dest)
   ```
2. `POST /api/v1/regulations/articles` is called. The Article entity stores:
   - Raw legal text
   - Deontic formula (SMT-LIB2 format)
   - ODRL policy (W3C JSON-LD)
   - Clauses with types (OBLIGATION / PROHIBITION / PERMISSION / EXEMPTION)

### Step 3 — NLP-Assisted Rule Translation (Module 2)

1. On the **Rule Translation** page, user enters a plain-English sentence:
   ```
   "A controller is forbidden to transfer personal data unless safeguards are in place."
   ```
2. Frontend calls `POST /api/v1/translation/translate` on **Module 2 (Rule Translation Engine)**.
3. Module 2 internally calls the **Python NLP service** at `http://nlp-extractor:5001/extract`.
4. The Python Flask app applies regex patterns and deontic keyword matching:
   - Detects "forbidden" → operator = `PROHIBITION`
   - Extracts subject = "controller", action = "transfer", target = "personal data"
   - Extracts constraint = "safeguards are in place"
5. Module 2 receives the structured result and generates:
   - **Deontic AST tuple**: `<PROHIBITION, controller, transfer, personal_data, safeguards_present>`
   - **SMT-LIB2**: `(assert (=> (and (= action "transfer") (= safeguards_present false)) false))`
   - **ODRL JSON-LD**: W3C compliant policy expression
6. A contradiction check scans all existing rules in the same regulation to detect logical conflicts.

### Step 4 — Policy Authoring (Module 3 — PAP)

1. The compliance officer authors an enterprise policy on the **Policy Administration** page:
   - Name: "Global HIPAA Access Control Policy"
   - Owner: "Legal Team"
2. `POST /api/v1/policies` creates the policy in DRAFT status.
3. They bind an internal rule ("Patient record access control") to the formal regulatory rule UUID from Module 2:
   `POST /api/v1/policies/{id}/links`
4. The policy is published: `POST /api/v1/policies/{id}/activate`.
   - This fires a `PolicyPublishedEvent`, allowing the PDP to refresh its policy cache.

### Step 5 — Context Resolution (Module 4 — PIP)

1. When an evaluation request arrives at the PDP, it calls PIP via gRPC.
2. PIP resolves the full context from its in-memory cache:
   - **Subject attributes**: `{ role: "analyst", mfa_enabled: true, clearance: "level-2" }`
   - **Resource attributes**: `{ classification: "HEALTH_RECORDS", sensitivity: "HIGH" }`
   - **Environment attributes**: `{ is_transitive_adequate: false }` ← checked via Neo4j graph traversal
3. Transitive adequacy: PIP queries whether there is an adequacy-agreement path from `sourceCountry → targetCountry` in the jurisdiction graph:
   ```
   US → EU  (via GDPR Article 45 adequacy)
   IN → EU  (direct adequacy agreement)
   RU → DE  (NO PATH — transfer blocked)
   ```

### Step 6 — Formal Verification / Decision (Module 5 — PDP)

1. PDP fetches active policies from PAP (gRPC).
2. PDP receives the resolved context from PIP (gRPC).
3. PDP compiles the full SMT-LIB2 assertion block combining request parameters + policy constraints:
   ```smtlib
   (declare-const action String)
   (declare-const transitive_adequate Bool)
   (assert (= action "transfer"))
   (assert (= transitive_adequate false))
   ; Policy constraint from Module 2:
   (assert (=> (= action "transfer") (= transitive_adequate true)))
   (check-sat)
   ```
4. Z3 solver evaluates and returns `unsat` (contradiction found → the rule is violated).
5. PDP records the **DENY** verdict + SMT trace in PostgreSQL.
6. The audit record is handed off to the Auditing Ledger.

### Step 7 — Cryptographic Signing (Module 6 — PQC)

1. The compliance decision object (verdict + timestamp + regulation IDs) is serialized to hex.
2. Module 6 looks up the appropriate signing key in its in-memory vault.
3. The **ML-DSA-65 (Dilithium3)** algorithm (via Bouncy Castle 1.80) signs the payload → produces a 3,293-byte signature.
4. The signed evidence bundle is returned and can be independently verified later.

### Step 8 — Zero-Knowledge Proof (Module 7 — ZKP)

1. If the cloud provider wants to prove compliance for a specific constraint (e.g., "server location is within EU") **without revealing the actual server IP**:
   - The server ID (e.g., `101`) is treated as the private witness `x`.
   - A **Pedersen commitment** is computed: `C = x·G + r·H` where `r` is a random blinding factor.
2. The Schnorr-Sigma protocol produces a proof tuple `(c, s1, s2, T)`.
3. Any verifier can check: `s1·G + s2·H - c·C = T` and `Hash(G||H||C||T) = c` — without ever knowing `x`.
4. Proof is stored in PostgreSQL. Proof ID is returned to the client.

### Step 9 — Auditing Ledger (Module 10)

1. Every compliance decision, event, and action is sealed into the ledger:
   ```
   Block N:
     previousHash = SHA256(Block N-1)
     timestamp = 2026-07-11T12:14:00Z
     data = { action: "transfer", actor: "analyst", target: "health-records", decision: "DENY" }
     currentHash = SHA256(previousHash + timestamp + data)
   ```
2. At any time, `GET /api/v1/ledger/verify` walks the entire chain and detects if any block was tampered with.

### Step 10 — Continuous Monitoring (Module 9)

1. A traffic simulator or real SDN controller sends data-movement events to `POST /api/v1/monitor/ingest`.
2. Each event is forwarded to Module 8 (Data Governance) via gRPC for legality checking.
3. If the governance engine returns BLOCKED, a CRITICAL violation alert is raised immediately.
4. The rolling SLA metric is updated: `SLA = (total_events - critical_violations) / total_events × 100%`.
5. The dashboard shows live SLA dials, event log, and violation list.

---

## 9. Database Design

### PostgreSQL — Relational Store

All Java services share one PostgreSQL database (`pqvcf_regulations`). Schema migrations are managed by **Flyway** (SQL files in `infrastructure/src/main/resources/db/migration/`).

#### Key Tables

```sql
-- Module 1: Regulations
regulations (
  id UUID PRIMARY KEY,
  name VARCHAR NOT NULL,
  short_name VARCHAR UNIQUE NOT NULL,  -- e.g. "GDPR", "HIPAA"
  primary_jurisdiction VARCHAR,
  version VARCHAR,
  description TEXT,
  status VARCHAR,                      -- DRAFT | ACTIVE | DEPRECATED
  created_at TIMESTAMP,
  updated_at TIMESTAMP
)

articles (
  id UUID PRIMARY KEY,
  regulation_id UUID REFERENCES regulations(id),
  article_number VARCHAR,              -- e.g. "Art. 46", "§164.312"
  title VARCHAR,
  content TEXT,
  deontic_formula TEXT,               -- SMT-LIB2 formula for Z3
  odrl_policy TEXT,                   -- W3C ODRL JSON-LD
  created_at TIMESTAMP,
  updated_at TIMESTAMP
)

clauses (
  id UUID PRIMARY KEY,
  article_id UUID REFERENCES articles(id),
  clause_number VARCHAR,
  content TEXT,
  clause_type VARCHAR                 -- OBLIGATION | PROHIBITION | PERMISSION | EXEMPTION
)

-- Module 2: Translated Rules
translated_rules (
  id UUID PRIMARY KEY,
  regulation_id UUID,
  article_index VARCHAR,
  operator VARCHAR,                   -- PROHIBITION | OBLIGATION | PERMISSION | EXEMPTION
  subject VARCHAR,
  action VARCHAR,
  target VARCHAR,
  constraint_text TEXT,
  smt_assertion TEXT,
  odrl_json TEXT,
  is_valid BOOLEAN,
  conflict_reason TEXT
)

-- Module 3: Policies (PAP)
compliance_policies (
  id UUID PRIMARY KEY,
  name VARCHAR,
  owner VARCHAR,
  description TEXT,
  status VARCHAR,                     -- DRAFT | ACTIVE | DEPRECATED
  created_at TIMESTAMP
)

policy_rule_links (
  id UUID PRIMARY KEY,
  policy_id UUID REFERENCES compliance_policies(id),
  org_rule_name VARCHAR,
  regulatory_rule_id UUID            -- References a translated_rules.id
)

-- Module 5: PDP Audit Log
pdp_decisions (
  id UUID PRIMARY KEY,
  subject_id VARCHAR,
  resource_id VARCHAR,
  action_id VARCHAR,
  source_country VARCHAR,
  target_country VARCHAR,
  verdict VARCHAR,                   -- PERMIT | DENY | INDETERMINATE
  smt_formula TEXT,
  solver_result VARCHAR,
  created_at TIMESTAMP
)

-- Module 6: PQC Key Vault
pqc_key_pairs (
  id UUID PRIMARY KEY,
  alias VARCHAR,
  algorithm VARCHAR,                 -- ML_DSA_65 | ML_KEM_768 | SLH_DSA_SHA2_128S
  public_key_hex TEXT,
  expires_at TIMESTAMP,
  created_at TIMESTAMP
  -- NOTE: Private keys are NEVER persisted; stored only in in-memory vault
)

-- Module 7: ZK Proofs
zk_proofs (
  id UUID PRIMARY KEY,
  circuit_type VARCHAR,             -- DATA_RESIDENCY | CONSENT | PURPOSE_LIMITATION
  commitment_point TEXT,            -- Pedersen C = x·G + r·H
  challenge TEXT,
  response_s1 TEXT,
  response_s2 TEXT,
  commitment_t TEXT,
  is_valid BOOLEAN,
  created_at TIMESTAMP
)

-- Module 8: Governance Decisions
governance_decisions (
  id UUID PRIMARY KEY,
  source_country VARCHAR,
  target_country VARCHAR,
  data_category VARCHAR,
  purpose VARCHAR,
  decision VARCHAR,                 -- APPROVED | CONDITIONAL | BLOCKED
  citations TEXT,
  reasoning TEXT,
  created_at TIMESTAMP
)

-- Module 9: Monitor Events & Violations
monitor_events (
  id UUID PRIMARY KEY,
  source_host VARCHAR,
  source_geo VARCHAR,
  target_host VARCHAR,
  target_geo VARCHAR,
  data_category VARCHAR,
  size_bytes BIGINT,
  ingested_at TIMESTAMP
)

violation_alerts (
  id UUID PRIMARY KEY,
  event_id UUID REFERENCES monitor_events(id),
  severity VARCHAR,                 -- INFO | WARNING | CRITICAL
  message TEXT,
  raised_at TIMESTAMP
)

-- Module 10: Auditing Ledger
ledger_blocks (
  id UUID PRIMARY KEY,
  sequence_number BIGINT,
  previous_hash VARCHAR(64),
  current_hash VARCHAR(64),
  actor VARCHAR,
  action VARCHAR,
  target VARCHAR,
  decision VARCHAR,
  timestamp TIMESTAMP,
  payload_json TEXT
)
```

### Neo4j — Graph Database

Neo4j stores the **jurisdiction adequacy relationship graph** used by Module 1 and queried by Module 4 (PIP).

```cypher
-- Nodes
(:Jurisdiction { code: "EU", name: "European Union" })
(:Jurisdiction { code: "US", name: "United States" })
(:Jurisdiction { code: "IN", name: "India" })
(:Jurisdiction { code: "RU", name: "Russia" })

-- Edges (adequacy agreements)
(EU)-[:ADEQUATE_WITH { basis: "GDPR Art. 45" }]->(US)
(EU)-[:ADEQUATE_WITH { basis: "EU-India adequacy" }]->(IN)
-- No edge between EU and RU → transfer blocked

-- Path query used by PIP
MATCH path = shortestPath(
  (:Jurisdiction {code: $source})-[:ADEQUATE_WITH*]->(:Jurisdiction {code: $target})
)
RETURN path IS NOT NULL AS is_adequate
```

---

## 10. API Reference — All Endpoints

### Module 1 — Regulation Repository (:8081)

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/v1/regulations` | List all regulations |
| `POST` | `/api/v1/regulations` | Register a new regulation |
| `GET` | `/api/v1/regulations/{id}` | Get regulation by ID |
| `GET` | `/api/v1/regulations/short/{shortName}` | Get regulation by short name (e.g., "GDPR") |
| `POST` | `/api/v1/regulations/{id}/activate` | Activate a draft regulation |
| `POST` | `/api/v1/regulations/articles` | Add article (with deontic formula + ODRL) to a regulation |
| `GET` | `/api/v1/graph/adequacy` | Get the jurisdiction adequacy graph (Neo4j) |
| `GET` | `/swagger-ui.html` | Interactive API docs |

### Module 2 — Rule Translation Engine (:8082)

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/v1/translation/translate` | Translate a CNL legal statement → deontic + SMT + ODRL |
| `GET` | `/api/v1/translation/rules/regulation/{regulation}` | Fetch all translated rules for a regulation |

### Module 3 — Policy Administration Point (:8083)

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/v1/policies` | Create a new compliance policy |
| `GET` | `/api/v1/policies` | List all policies |
| `POST` | `/api/v1/policies/{id}/links` | Bind an org rule to a regulatory rule |
| `DELETE` | `/api/v1/policies/{id}/links/{linkId}` | Remove a rule link |
| `POST` | `/api/v1/policies/{id}/activate` | Publish / activate the policy |

### Module 4 — Policy Information Point (:8084)

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/v1/pip/resolve` | Resolve full context attributes for a request |
| `POST` | `/api/v1/pip/attributes/subject` | Inject subject attributes into cache |

### Module 5 — Policy Decision Point (:8085)

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/v1/pdp/evaluate` | Submit compliance evaluation → PERMIT or DENY |
| `GET` | `/api/v1/pdp/audits` | List all past evaluation decisions |

### Module 6 — PQC Crypto Layer (:8086)

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/v1/pqc/keys` | Generate a new PQ key pair (ML-DSA / ML-KEM / SLH-DSA) |
| `GET` | `/api/v1/pqc/keys` | List all generated keys |
| `POST` | `/api/v1/pqc/sign` | Sign a payload (hex) with a stored key |
| `POST` | `/api/v1/pqc/verify` | Verify a signature against a key and payload |

### Module 7 — ZK Proof Engine (:8087)

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/v1/zkp/prove` | Generate a ZK proof for a compliance claim |
| `POST` | `/api/v1/zkp/verify` | Verify a ZK proof |
| `GET` | `/api/v1/zkp/proofs` | List all proofs in the ledger |

### Module 8 — Data Governance Engine (:8088)

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/v1/governance/evaluate` | Evaluate if a cross-border transfer is legal |
| `GET` | `/api/v1/governance/decisions` | List all governance decisions |

### Module 9 — Compliance Monitor (:8089)

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/v1/monitor/ingest` | Ingest a real-time data movement event |
| `GET` | `/api/v1/monitor/metrics` | Get SLA rates, event counts, violation counts |

### Module 10 — Auditing Ledger (:8090)

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/v1/ledger/seal` | Seal a new block into the hash chain |
| `GET` | `/api/v1/ledger/verify` | Verify integrity of the entire ledger chain |

### NLP Rule Extractor — Python Flask (:5001)

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/extract` | Parse a CNL text → `{ operator, subject, action, target, constraint }` |

---

## 11. Security Architecture

### Spring Security Configuration

Every Java service uses a **stateless, session-free** Spring Security setup:

```java
http
  .cors(cors -> cors.configurationSource(corsConfigurationSource()))
  .csrf(AbstractHttpConfigurer::disable)        // REST API — CSRF not needed
  .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
  .authorizeHttpRequests(auth -> auth
    .requestMatchers("/api/v1/**").permitAll()   // Open for prototype phase
    .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
    .anyRequest().authenticated()
  );
```

### CORS Configuration

| Allowed Origin | Reason |
|---|---|
| `http://localhost:5173` | Vite dev server |
| `http://127.0.0.1:5173` | Vite dev server (loopback) |
| `http://localhost:3000` | Alternative dev port |

Allowed methods: `GET, POST, PUT, DELETE, OPTIONS, PATCH`
Allowed headers: `Authorization, Content-Type, X-Requested-With`

### Security Layers Summary

```
Layer 1 — Network:
  • Docker network isolation (services cannot be reached except via exposed ports)
  • Nginx reverse proxy terminates all external connections

Layer 2 — Transport:
  • HTTPS (configurable for production)
  • gRPC uses TLS channels internally

Layer 3 — Application:
  • Spring Security on every service
  • Input validation on all request DTOs
  • SQL injection protected by JPA parameterized queries
  • Z3 CLI executed in sandboxed Java subprocess (prevents command injection)

Layer 4 — Cryptographic:
  • All compliance proofs signed with ML-DSA-65 (post-quantum safe)
  • Auditing ledger protected by SHA-256 hash chain (tamper-evident)
  • ZK proofs protect operational secrets (Pedersen commitments)

Layer 5 — Data:
  • Private keys NEVER persisted to disk (in-memory vault only)
  • Sensitive fields stored as hex-encoded byte arrays
```

---

## 12. Cryptographic Components — Post-Quantum Cryptography

### Why Post-Quantum?

Classical algorithms (RSA, ECDSA, ECDH) rely on the hardness of integer factoring and discrete logarithms. **Shor's algorithm** running on a sufficiently large quantum computer can break these in polynomial time. This means compliance evidence signed today with RSA will become forgeable in the future.

PQVCF uses **NIST-standardized post-quantum algorithms** (FIPS 203, 204, 205) to ensure compliance evidence remains cryptographically valid even after quantum computers exist.

### Algorithms Implemented

| Algorithm | NIST Standard | Type | Use in PQVCF | Key Sizes |
|---|---|---|---|---|
| **ML-KEM-768** (Kyber-768) | FIPS 203 | Key Encapsulation | Secure key exchange for compliance channels | Public: 1184 bytes |
| **ML-DSA-65** (Dilithium3) | FIPS 204 | Digital Signature | Signs compliance proof bundles and audit records | Signature: 3,293 bytes |
| **SLH-DSA-SHA2-128S** (SPHINCS+) | FIPS 205 | Digital Signature | Hash-based signature for long-term archival evidence | Signature: ~7,856 bytes |
| **SHA-3** | FIPS 202 | Hash Function | Hashing in proof generation | N/A |

### Mathematical Foundations

**ML-KEM Security (Module Learning With Errors):**
- Based on M-LWE: given matrix **A** and vector **b = A·s + e** (where **s** is secret and **e** is small error), find **s**.
- Quantum speedup via Grover's algorithm reduces security by only a constant factor → remains secure.

**ML-DSA Security (Module Short Integer Solution + M-LWE):**
- Signature `σ = (z, c)` where:
  - `c` is a hash challenge: `c = Hash(w1 || payload)`
  - `z` is a short vector response: `||z|| ≤ B` (norm bound)
- Verification: `A·z - c·t ≈ w` — checks the algebraic relationship holds.

### API Flow for Signing a Compliance Proof

```
1. Generate key:
   POST /api/v1/pqc/keys
   { "algorithmType": "ML_DSA_65", "alias": "audit-signer-2026" }
   → { "keyId": "uuid", "publicKeyHex": "...", "expiresAt": "..." }

2. Sign proof:
   POST /api/v1/pqc/sign
   { "keyId": "uuid", "payloadHex": "68656c6c6f" }
   → { "signatureHex": "...(3293 bytes)...", "signatureLength": 3293 }

3. Verify later:
   POST /api/v1/pqc/verify
   { "keyId": "uuid", "payloadHex": "68656c6c6f", "signatureHex": "..." }
   → { "valid": true }
```

### Implementation Note: Direct Bouncy Castle Engine API

The system uses **Bouncy Castle 1.80** with direct engine API calls (not JCA provider registration) to avoid JVM security policy blocks in containerized cloud environments:

```java
// ML-DSA signing (simplified)
MLDSASigner signer = new MLDSASigner();
signer.init(true, privateKey);
signer.update(payload, 0, payload.length);
byte[] signature = signer.generateSignature();
```

---

## 13. Zero-Knowledge Proof Workflow

### The Problem ZKP Solves

Imagine a cloud provider must prove: *"Our server that handled this health record is located within the EU."* But revealing the server's IP address or hostname would expose confidential infrastructure to regulators. ZKP allows proving the statement is true without revealing any operational secret.

### Mathematical Protocol: Schnorr-Pedersen Sigma Protocol

The system uses **secp256r1** elliptic curve with two generator points `G` (standard) and `H` (constructed with unknown discrete log).

**Step 1 — Commit:** The secret server ID `x` (e.g., `101`) is hidden inside a Pedersen commitment:
```
C = x·G + r·H
```
where `r` is a random blinding factor. `C` can be shared publicly — it reveals nothing about `x`.

**Step 2 — Prover creates proof:**
```
k₁, k₂  ← random scalars
T = k₁·G + k₂·H                          (random commitment point)
c = Hash(G || H || C || T)  mod q         (Fiat-Shamir challenge — non-interactive)
s₁ = k₁ + c·x  mod q
s₂ = k₂ + c·r  mod q
Proof = (c, s₁, s₂, T)
```

**Step 3 — Verifier checks:**
```
T' = s₁·G + s₂·H - c·C
c_check = Hash(G || H || C || T')
Accept if c_check == c
```

The verifier learns nothing about `x` (the server ID), yet is mathematically convinced the prover knows a value that satisfies the compliance predicate.

### ZKP API Flow

```
POST /api/v1/zkp/prove
{
  "circuitType": "DATA_RESIDENCY",
  "privateWitness": 101,
  "publicInputs": { "region": "EU", "dataCategory": "HEALTH" }
}
→ {
  "proofId": "uuid",
  "commitment": "(x:..., y:...)",
  "challenge": "3a9f...",
  "responseS1": "7bc2...",
  "responseS2": "1e4a..."
}

POST /api/v1/zkp/verify
{
  "proofId": "uuid",
  "challenge": "3a9f...",
  "responseS1": "7bc2...",
  "responseS2": "1e4a...",
  "commitment": "(x:..., y:...)"
}
→ { "valid": true }
```

### Circuit Types Supported

| Circuit Type | What it Proves |
|---|---|
| `DATA_RESIDENCY` | Data was processed within a specific geographic region |
| `CONSENT` | Data subject's consent was recorded (without revealing the subject) |
| `PURPOSE_LIMITATION` | Data was used only for its declared purpose |

---

## 14. Formal Verification Process

### What is Formal Verification?

Traditional software testing checks a program with specific inputs and verifies the outputs are correct. Formal verification mathematically **proves** that a property holds for **all possible inputs** — it cannot miss edge cases.

PQVCF uses the **Z3 SMT Solver** (Microsoft Research) to prove that regulatory compliance holds for a given request context.

### How SMT Works in PQVCF

**SMT (Satisfiability Modulo Theories)** is a decision procedure that checks whether a logical formula has a solution (is "satisfiable") or has no solution (is "unsatisfiable").

**Compliance Check Logic:**
- Regulatory rules say: *"If action is 'transfer' AND consent is absent, the transfer is PROHIBITED."*
- This becomes a constraint: `(assert (=> (and (= action "transfer") (= consent_present false)) false))`
- The request says: `(assert (= action "transfer"))` and `(assert (= consent_present false))`
- These together are **UNSATISFIABLE** — a logical contradiction → verdict: **DENY**

### Z3 Execution in PDP

```
Algorithm EvaluateCompliance(Request):
    1. Fetch active policy formulas from PAP (gRPC)
    2. Resolve request context attributes from PIP (gRPC)
    3. Build SMT-LIB2 string:
       (declare-const action String)
       (declare-const consent_present Bool)
       (assert (= action "transfer"))          ← from request
       (assert (= consent_present false))       ← from PIP context
       (assert (=> (and (= action "transfer")  ← from policy rule
                        (= consent_present false))
                   false))
       (check-sat)
    4. Execute Z3 binary as sandboxed subprocess
    5. Parse output:
       "unsat" → verdict = DENY
       "sat"   → verdict = PERMIT
       else    → verdict = INDETERMINATE
    6. Persist decision + SMT trace to PostgreSQL
```

### Fallback Behaviour

If the Z3 binary is not installed in the container, PDP falls back to an in-process heuristic logic parser that evaluates common compliance patterns without external solver overhead. This keeps the API functional for demo purposes.

### Deontic Logic Encoding

Legal statements are encoded using four deontic modal operators:

| Operator | Symbol | Meaning | Example |
|---|---|---|---|
| Permission | `P(α, γ)` | Actor α is allowed to do γ | "Controller may transfer data to adequate countries" |
| Obligation | `O(α, γ)` | Actor α must do γ | "Controller must ensure safeguards are in place" |
| Prohibition | `F(α, γ)` | Actor α is forbidden from doing γ | "Processor must not transfer data without consent" |
| Exemption | `E(α, γ)` | Actor α is exempt from constraint γ | "Unless emergency medical treatment is required" |

---

## 15. The NLP Rule Extraction Pipeline

The **NLP Rule Extractor** is a Python Flask microservice that assists legal analysts in converting natural language regulatory text into structured machine-readable form.

### Input → Output Example

**Input (plain English):**
```
"A controller must not transfer personal data to third countries unless adequate safeguards are in place."
```

**Processing pipeline:**
```
1. Normalize to lowercase
2. Scan for deontic keywords:
   "must not" → PROHIBITION operator
3. Apply CNL regex: Subject + modalVerb + Action + Target
   Subject = "a controller"
   Action  = "transfer"
   Target  = "personal data"
4. Extract constraint (text after "unless"):
   constraint = "adequate safeguards are in place"
```

**Output (JSON):**
```json
{
  "operator": "PROHIBITION",
  "subject": "a controller",
  "action": "transfer",
  "target": "personal data",
  "constraint": "adequate safeguards are in place",
  "raw_text": "A controller must not transfer personal data..."
}
```

### Deontic Keyword Mappings

| Deontic Type | Keywords |
|---|---|
| PROHIBITION | shall not, must not, prohibited, forbidden, cannot, restricts, restrict |
| OBLIGATION | shall, must, obliged, obligated, required, mandated |
| EXEMPTION | exempt, exemption, except, unless, exemption clause |
| PERMISSION | (default — any statement not matching above categories) |

### Integration with Module 2

Module 2 (Rule Translation Engine) calls this Python service via HTTP:
```
POST http://nlp-extractor:5001/extract
{ "text": "the raw CNL statement" }
```
If the Python service is unavailable, Module 2 falls back to its own internal regex parser to maintain availability.

---

## 16. Frontend Architecture

The research dashboard is a **React 18 + TypeScript** single-page application built with **Vite** for fast hot-reloading during development.

### Technology Choices

| Technology | Version | Purpose |
|---|---|---|
| React | 18 | UI component framework |
| TypeScript | 5.x | Type-safe JavaScript |
| React Router v6 | 6.x | Client-side page routing |
| Axios | 1.x | HTTP client for API calls |
| Vite | 5.x | Dev server + production bundler |
| Nginx | 1.25 | Production static file server + API reverse proxy |

### Page → Module Mapping

| Route | Page Component | Talks to Backend |
|---|---|---|
| `/` | `HomePage` | Summary of all modules |
| `/regulations` | `RegulationsListPage` | Module 1 (:8081) |
| `/regulations/:id` | `RegulationDetailPage` | Module 1 (:8081) |
| `/register` | `RegisterRegulationPage` | Module 1 (:8081) |
| `/graph` | `GraphExplorerPage` | Module 1 (:8081) — Neo4j graph |
| `/translate` | `RuleTranslationPage` | Module 2 (:8082) + NLP (:5001) |
| `/policies` | `PolicyAdministrationPage` | Module 3 (:8083) |
| `/pip` | `PipPage` | Module 4 (:8084) |
| `/pdp` | `PdpPage` | Module 5 (:8085) |
| `/pqc` | `PqcCryptoPage` | Module 6 (:8086) |
| `/zkp` | `ZkProofPage` | Module 7 (:8087) |
| `/governance` | `DataGovernancePage` | Module 8 (:8088) |
| `/monitor` | `ComplianceMonitorPage` | Module 9 (:8089) |
| `/ledger` | `AuditingLedgerPage` | Module 10 (:8090) |

### Frontend File Organisation

```
src/
├── App.tsx                    ← Router config; imports all pages
├── main.tsx                   ← ReactDOM.createRoot entry point
├── index.css                  ← Global CSS (dark theme, colour palette, typography)
│
├── api/                       ← One file per backend service (thin Axios wrappers)
│   ├── regulationsApi.ts      ← GET/POST /api/v1/regulations
│   ├── translationApi.ts      ← POST /api/v1/translation/translate
│   ├── policyApi.ts           ← POST /api/v1/policies
│   ├── pipApi.ts              ← POST /api/v1/pip/resolve
│   ├── pdpApi.ts              ← POST /api/v1/pdp/evaluate
│   ├── pqcApi.ts              ← POST /api/v1/pqc/keys, sign, verify
│   ├── zkpApi.ts              ← POST /api/v1/zkp/prove, verify
│   ├── governanceApi.ts       ← POST /api/v1/governance/evaluate
│   ├── monitorApi.ts          ← POST /api/v1/monitor/ingest
│   └── ledgerApi.ts           ← POST /api/v1/ledger/seal, GET verify
│
├── components/
│   └── layout/
│       └── Layout.tsx         ← Navigation sidebar + page wrapper
│
├── pages/                     ← 14 page components (one per route)
│   └── *.tsx
│
└── types/                     ← Shared TypeScript interfaces matching API DTOs
```

---

## 17. Frontend–Backend Interaction

### Vite Dev Server Proxy

During development (`npm run dev`), Vite proxies all `/api` requests to avoid CORS issues:

```typescript
// vite.config.ts
server: {
  proxy: {
    '/api/v1/regulations':  { target: 'http://localhost:8081' },
    '/api/v1/translation':  { target: 'http://localhost:8082' },
    '/api/v1/policies':     { target: 'http://localhost:8083' },
    '/api/v1/pip':          { target: 'http://localhost:8084' },
    '/api/v1/pdp':          { target: 'http://localhost:8085' },
    '/api/v1/pqc':          { target: 'http://localhost:8086' },
    '/api/v1/zkp':          { target: 'http://localhost:8087' },
    '/api/v1/governance':   { target: 'http://localhost:8088' },
    '/api/v1/monitor':      { target: 'http://localhost:8089' },
    '/api/v1/ledger':       { target: 'http://localhost:8090' },
  }
}
```

### Production Nginx Proxy

In Docker production, Nginx serves the static React build and proxies API calls to the correct backend container:

```nginx
# docker/nginx.conf (simplified)
location /api/v1/regulations  { proxy_pass http://regulation-api:8081; }
location /api/v1/translation  { proxy_pass http://rule-translation-api:8082; }
location /api/v1/policies     { proxy_pass http://policy-administration-api:8083; }
location /api/v1/pip          { proxy_pass http://policy-information-api:8084; }
location /api/v1/pdp          { proxy_pass http://policy-decision-api:8085; }
location /api/v1/pqc          { proxy_pass http://pqc-crypto-api:8086; }
location /api/v1/zkp          { proxy_pass http://zk-proof-api:8087; }
location /api/v1/governance   { proxy_pass http://data-governance-api:8088; }
location /api/v1/monitor      { proxy_pass http://compliance-monitor-api:8089; }
location /api/v1/ledger       { proxy_pass http://auditing-ledger-api:8090; }
```

### Typical API Call (TypeScript example)

```typescript
// src/api/pdpApi.ts
import axios from 'axios';

export interface EvaluateRequest {
  subjectId: string;
  resourceId: string;
  actionId: string;
  sourceCountry: string;
  targetCountry: string;
}

export interface EvaluateResponse {
  verdict: 'PERMIT' | 'DENY' | 'INDETERMINATE';
  smtFormula: string;
  solverResult: string;
  evaluatedAt: string;
}

export const evaluateCompliance = (req: EvaluateRequest): Promise<EvaluateResponse> =>
  axios.post('/api/v1/pdp/evaluate', req).then(r => r.data);
```

---

## 18. Inter-Service Communication (gRPC)

Some services need to call each other synchronously with low latency. These use **gRPC + Protocol Buffers** over the internal Docker network.

### gRPC Service Endpoints

| Caller | Callee | gRPC Method | What it Does |
|---|---|---|---|
| PDP (8085) | PAP (8083) | `GetActivePolicies` | Fetch active policy + rule links | 
| PDP (8085) | PIP (8084) | `ResolveRequestContext` | Resolve subject/resource/env attributes |
| PIP (8084) | Regulation Repo (8081) | `CheckJurisdictionAdequacy` | Query Neo4j for adequacy path |
| Compliance Monitor (8089) | Data Governance (8088) | `EvaluateTransfer` | Check if event violates data residency rules |

### gRPC Ports (Internal Docker Network Only)

| Service | gRPC Port | REST Port |
|---|---|---|
| Regulation Repository | `9091` | `8081` |
| Rule Translation | `9092` | `8082` |
| PAP | `9093` | `8083` |
| PIP | `9094` | `8084` |
| PDP | `9095` | `8085` |
| PQC Crypto | `9096` | `8086` |
| ZK Proof | `9097` | `8087` |
| Data Governance | `9098` | `8088` |
| Compliance Monitor | `9099` | `8089` |
| Auditing Ledger | `9100` | `8090` |

### Fallback Strategy

If a gRPC target is offline (e.g., PAP container not yet started), the calling service falls back to in-memory cached data or a local heuristic to prevent the entire evaluation pipeline from failing. This is a **graceful degradation** design.

---

## 19. Deployment Architecture

### Docker Compose (Local / Research Environment)

The entire system (14 containers) starts with a single command from the `docker/` directory:

```bash
cd docker
docker-compose up --build
```

#### Container Startup Order

```
1. postgres       (waits for: nothing)
2. neo4j          (waits for: nothing)
3. nlp-extractor  (waits for: nothing)
4. regulation-api          (depends on: postgres, neo4j)
5. rule-translation-api    (depends on: postgres, nlp-extractor)
6. policy-administration-api (depends on: postgres)
7. policy-information-api  (depends on: postgres, regulation-api)
8. policy-decision-api     (depends on: postgres, PAP, PIP)
9. pqc-crypto-api          (depends on: postgres)
10. zk-proof-api           (depends on: postgres)
11. data-governance-api    (depends on: postgres)
12. compliance-monitor-api (depends on: postgres, data-governance-api)
13. auditing-ledger-api    (depends on: postgres)
14. regulation-frontend    (depends on: all services above)
```

### Docker Volume Strategy

| Volume Name | Contents |
|---|---|
| `postgres_data` | PostgreSQL database files (persists across container restarts) |
| `neo4j_data` | Neo4j graph database files |

### Environment Variables per Service

| Variable | Used By | Example |
|---|---|---|
| `SPRING_DATASOURCE_URL` | All Java services | `jdbc:postgresql://postgres:5432/pqvcf_regulations` |
| `SPRING_NEO4J_URI` | Regulation Repository | `bolt://neo4j:7687` |
| `PQVCF_NEO4J_ENABLED` | Regulation Repository | `true` |
| `PQVCF_NLP_URL` | Rule Translation | `http://nlp-extractor:5001/extract` |
| `PQVCF_PAP_GRPC_HOST` | PDP | `policy-administration-api` |
| `PQVCF_PAP_GRPC_PORT` | PDP | `9093` |
| `PQVCF_PIP_GRPC_HOST` | PDP | `policy-information-api` |
| `PQVCF_PIP_GRPC_PORT` | PDP | `9094` |
| `PQVCF_GOVERNANCE_GRPC_HOST` | Compliance Monitor | `data-governance-api` |
| `PQVCF_GOVERNANCE_GRPC_PORT` | Compliance Monitor | `9098` |

### Kubernetes (Production Scale-Out)

The system is Kubernetes-ready. Each container maps directly to a **Deployment + Service** resource:

```yaml
# Example: policy-decision-api
apiVersion: apps/v1
kind: Deployment
metadata:
  name: policy-decision-api
spec:
  replicas: 3  # Horizontal scaling
  selector:
    matchLabels:
      app: policy-decision-api
  template:
    spec:
      containers:
      - name: policy-decision-api
        image: pqvcf/policy-decision-api:1.0.0
        ports:
        - containerPort: 8085  # REST
        - containerPort: 9095  # gRPC
        env:
        - name: PQVCF_PAP_GRPC_HOST
          value: policy-administration-api  # Kubernetes service DNS
```

---

## 20. Technology Stack Reference

### Backend (Java)

| Technology | Version | Role |
|---|---|---|
| Java | 21 | Primary language (uses preview features) |
| Spring Boot | 3.3.1 | Application framework |
| Spring Security | 6.x | Authentication + CORS |
| Spring Data JPA | 3.x | ORM for PostgreSQL |
| Spring Data Neo4j | 7.x | Graph DB integration |
| Flyway | 10.15.0 | Database schema versioning |
| gRPC | 1.65.0 | Inter-service communication |
| Protocol Buffers | 4.27.2 | gRPC serialization format |
| Bouncy Castle | 1.80 | Post-quantum cryptography |
| MapStruct | 1.5.5 | DTO ↔ domain object mapping |
| Lombok | 1.18.32 | Boilerplate reduction (@Data, @Builder, etc.) |
| SpringDoc OpenAPI | 2.5.0 | Swagger UI generation |
| JUnit 5 | 5.x | Unit testing |
| Mockito | 5.x | Mocking |
| ArchUnit | 1.3.0 | Architecture fitness functions |
| Testcontainers | 1.19.8 | Integration tests with real Docker containers |
| JaCoCo | 0.8.12 | Test coverage reports |
| SpotBugs | 4.8.5 | Static analysis |
| OpenTelemetry | 1.38.0 | Distributed tracing + metrics |
| Maven | 3.9+ | Build system |

### Frontend (TypeScript)

| Technology | Version | Role |
|---|---|---|
| React | 18 | UI framework |
| TypeScript | 5.x | Type safety |
| React Router | 6.x | Client-side routing |
| Axios | 1.x | HTTP client |
| Vite | 5.x | Build tool + dev server |
| Nginx | 1.25 | Production static file server |

### Python (NLP Service)

| Technology | Version | Role |
|---|---|---|
| Python | 3.11+ | Runtime |
| Flask | 3.x | Web framework |

### Databases

| Database | Version | Role |
|---|---|---|
| PostgreSQL | 16 | Primary relational store |
| Neo4j | 5.20 Community | Jurisdiction graph store |

### Infrastructure

| Technology | Version | Role |
|---|---|---|
| Docker | 24+ | Containerization |
| Docker Compose | 3.8 | Local orchestration |
| Kubernetes | 1.29+ | Production orchestration (planned) |

---

## 21. End-to-End Worked Example

### Scenario: "Can an analyst transfer Indian health records to Germany?"

**Parameters:**
- Subject: `analyst-001`
- Resource: `indian-health-records`
- Action: `transfer`
- Source Country: `IN` (India)
- Target Country: `DE` (Germany)

---

**Step 1:** Frontend PDP page → user fills form and clicks "Evaluate".

```
POST /api/v1/pdp/evaluate
{
  "subjectId": "analyst-001",
  "resourceId": "indian-health-records",
  "actionId": "transfer",
  "sourceCountry": "IN",
  "targetCountry": "DE"
}
```

**Step 2:** PDP calls PAP (gRPC: `GetActivePolicies`).
→ Returns HIPAA policy with rule: *"Health records transfer requires adequacy OR explicit consent."*
→ SMT fragment: `(assert (=> (= resource_type "HEALTH_RECORDS") (or (= adequacy_path_exists true) (= consent_present true))))`

**Step 3:** PDP calls PIP (gRPC: `ResolveRequestContext`).
→ PIP queries Neo4j: Path from `IN` → `DE`? ✅ Path exists via EU adequacy agreement.
→ Returns: `{ is_transitive_adequate: true, data_classification: "HEALTH_RECORDS", subject_role: "ANALYST" }`

**Step 4:** PDP builds SMT formula:
```smtlib
(declare-const resource_type String)
(declare-const adequacy_path_exists Bool)
(declare-const consent_present Bool)
(assert (= resource_type "HEALTH_RECORDS"))
(assert (= adequacy_path_exists true))        ← PIP confirmed path exists
(assert (=> (= resource_type "HEALTH_RECORDS")
            (or (= adequacy_path_exists true)
                (= consent_present true))))
(check-sat)
```

**Step 5:** Z3 evaluates → `sat` (no contradiction). Verdict: **PERMIT**.

**Step 6:** Decision sealed into auditing ledger:
```
Block #47:
  previousHash: abc123...
  data: { actor: "analyst-001", action: "transfer", target: "indian-health-records", decision: "PERMIT" }
  currentHash: def456...
```

**Step 7:** Compliance proof signed with ML-DSA-65:
```
Payload: { verdict: "PERMIT", timestamp: "2026-07-11T12:14:00Z", ... }
Signature: 3293-byte Dilithium signature
```

**Step 8:** Response returned to frontend:
```json
{
  "verdict": "PERMIT",
  "smtFormula": "(check-sat) → sat",
  "solverResult": "sat",
  "evaluatedAt": "2026-07-11T12:14:01Z"
}
```

---

**Now compare: Transfer from Russia to Germany**

Same process, but at Step 3, PIP queries Neo4j: Path from `RU` → `DE`?
- No adequacy agreement edge in graph → `is_transitive_adequate = false`

At Step 4, the formula becomes:
```smtlib
(assert (= adequacy_path_exists false))
; DPDP localization rule (if Russia is source):
(assert (= is_localized true))
(assert (=> (= is_localized true) false))     ← localization mandates block transfer
(check-sat)
```
Z3 → `unsat`. Verdict: **DENY**.

The Compliance Monitor would have raised a **CRITICAL alert** and reduced the rolling SLA score.

---

## 22. Testing Strategy

### Three Layers of Testing

Each module has three types of tests following the same naming conventions:

| Test Type | File Pattern | Description | Tools |
|---|---|---|---|
| **Unit Tests** | `*Test.java` | Tests domain logic in complete isolation | JUnit 5, Mockito |
| **Integration Tests** | `*IT.java` | Tests full REST endpoint + database round-trip | Spring Boot Test, Testcontainers, MockMvc |
| **Architecture Tests** | `*ArchTest.java` | Enforces Clean Architecture layer rules | ArchUnit |

### Example Unit Tests Per Module

| Module | Test Class | What it Validates |
|---|---|---|
| Regulation Repository | `RegulationTest` | Domain aggregate invariants |
| Rule Translation | `DeonticFormulaTest` | AST 5-tuple value structures |
| PAP | `PolicyTest` | Policy state machine transitions |
| PIP | `ContextAttributeTest` | Attribute category casting |
| PDP | `DecisionResultTest` | Verdict enumeration logic |
| PQC | `PqcKeyPairTest` | Key model domain checks |
| ZKP | `PedersenCommitmentTest` | Commitment point computation |
| Data Governance | `GovernanceDecisionTest` | APPROVED/BLOCKED/CONDITIONAL logic |
| Compliance Monitor | `SlaMetricsTest` | Rolling SLA calculation formula |
| Auditing Ledger | `HashChainTest` | SHA-256 chain linking |

### Running Tests

```bash
# Run all unit tests
mvn test

# Run all integration tests
mvn verify

# Run a single module's tests
mvn test -pl regulation-repository

# Generate coverage report
mvn jacoco:report
```

---

## 23. Performance Characteristics

| Operation | Expected Latency | Complexity |
|---|---|---|
| Regulation lookup (PostgreSQL) | < 5 ms | O(1) index |
| Governance decision (hash map) | < 1 ms | O(1) |
| PIP context resolution (cache) | < 1 ms | O(1) |
| Neo4j adequacy path query | < 10 ms | O(V + E) |
| Z3 SMT solving (compliance) | < 10 ms | DPLL(T) heuristic |
| ML-DSA key generation | < 1 ms | O(d² log q) |
| ML-DSA signing | < 3 ms | O(d²) |
| ML-DSA verification | < 3 ms | O(d²) |
| ZK proof generation | < 10 ms | 4 EC multiplications |
| ZK proof verification | < 5 ms | 2 EC multiplications |
| Ledger block sealing | < 0.1 ms | O(1) SHA-256 |
| Full ledger verification (10k blocks) | < 50 ms | O(N) |

---

## 24. How Every Component Works Together

This is the complete picture of how all 12 components collaborate in the running system:

```
                    ╔═══════════════════════════════════════════════════════╗
                    ║           RESEARCH DASHBOARD FRONTEND (React)         ║
                    ║  14 pages, each talking to exactly 1–2 backend APIs   ║
                    ╚══════════════════════════╤════════════════════════════╝
                                               │ REST HTTP
          ┌────────────────────────────────────┼────────────────────────────────────┐
          │                                    │                                    │
          ▼                                    ▼                                    ▼
  ╔═══════════════╗                 ╔══════════════════╗               ╔══════════════════╗
  ║  Module 1     ║                 ║  Module 2        ║               ║  Module 3 (PAP)  ║
  ║  Regulation   ║ ──── sync ────► ║  Rule            ║               ║  Policy Admin    ║
  ║  Repository   ║   jurisdiction  ║  Translation     ║               ║  Point           ║
  ║  (PostgreSQL  ║   nodes to      ║  Engine          ║               ╚════════╤═════════╝
  ║   + Neo4j)    ║   Neo4j         ║  (+ Python NLP)  ║                        │ gRPC
  ╚═══════════════╝                 ╚══════════════════╝                        │ GetActivePolicies
          ▲                                  ▲                                   │
          │ gRPC                             │ (Translation rules are            │
          │ CheckJurisdictionAdequacy        │  stored back in Postgres)         ▼
  ╔═══════════════╗                                                   ╔══════════════════╗
  ║  Module 4     ║ ────────────────────────────────────────────────► ║  Module 5 (PDP)  ║
  ║  Policy Info  ║ gRPC: ResolveRequestContext                       ║  Policy Decision ║
  ║  Point (PIP)  ║ (subject attrs + adequacy path)                   ║  Point (Z3)      ║
  ╚═══════════════╝                                                   ╚════════╤═════════╝
                                                                               │ verdict
                                                                               ▼
                                        ┌──────────────────────────────────────┤
                                        │                                      │
                              ╔═════════▼═══════╗               ╔══════════════▼══════════╗
                              ║  Module 6       ║               ║  Module 10              ║
                              ║  PQC Crypto     ║               ║  Auditing Ledger        ║
                              ║  Layer          ║               ║  (SHA-256 hash chain)   ║
                              ║  (ML-DSA sign)  ║               ╚═════════════════════════╝
                              ╚═════════════════╝
                                        │
                              ╔═════════▼═══════╗
                              ║  Module 7       ║
                              ║  ZK Proof       ║
                              ║  Engine         ║
                              ║  (Pedersen/     ║
                              ║   Sigma protocol║
                              ╚═════════════════╝

  ╔═══════════════════════════════════════════════════════════════════════════════╗
  ║                      CONTINUOUS MONITORING PIPELINE                           ║
  ║                                                                               ║
  ║  Module 9 (Monitor) ──── gRPC EvaluateTransfer ────► Module 8 (Governance)   ║
  ║  Ingest events from                                   Cross-border legality   ║
  ║  network/SDN                                          checker                 ║
  ║       │                                                       │               ║
  ║       ▼ if BLOCKED                                            │               ║
  ║  Raise CRITICAL alert, reduce SLA%                           ▼               ║
  ║  Log all events to PostgreSQL                         APPROVED/               ║
  ║                                                       CONDITIONAL/            ║
  ║                                                       BLOCKED                 ║
  ╚═══════════════════════════════════════════════════════════════════════════════╝
```

### The "Chain of Proof"

Every compliance action generates a chain of verifiable evidence:

```
Legal Text
    ↓ Module 2 (NLP + Translation)
Formal Deontic Rule (SMT-LIB2)
    ↓ Module 3 (PAP)
Organizational Policy (linked to rule)
    ↓ Module 5 (PDP + Z3)
Formal Proof of Compliance (sat/unsat result)
    ↓ Module 6 (PQC Crypto)
Post-Quantum Signed Compliance Certificate
    ↓ Module 7 (ZKP)
Zero-Knowledge Proof (no operational secrets revealed)
    ↓ Module 10 (Auditing Ledger)
Tamper-Evident Immutable Record
    ↓
Verifiable Evidence Bundle (for regulators / auditors)
```

---

## 25. Extending the System

### Adding a New Regulation (e.g., Australia Privacy Act)

1. Register via `POST /api/v1/regulations`:
   ```json
   { "name": "Australia Privacy Act 1988", "shortName": "APA", "primaryJurisdiction": "AU" }
   ```
2. Add articles with deontic formulas.
3. Add the jurisdiction node to Neo4j via the graph API.
4. Add adequacy edges for countries Australia has data-sharing agreements with.
5. Translated rules from Module 2 automatically feed into PAP policies.

### Adding a New Cryptographic Algorithm

1. Add the Bouncy Castle algorithm class reference to `pqc-crypto-layer-domain` (new enum value in `AlgorithmType`).
2. Implement the signer/encapsulator in `pqc-crypto-layer-infrastructure`.
3. The API layer automatically picks up new enum values — no controller changes needed.

### Adding a New ZKP Circuit Type

1. Add enum value to `CircuitType` in `zk-proof-engine-domain`.
2. Implement the specific witness predicate in the application service.
3. No frontend changes needed — the `ZkProofPage` dynamically displays all circuit types.

### Adding a New Backend Module

Follow the four-layer Clean Architecture template:
1. Create folder: `new-module-name/`
2. Create four sub-modules: `-domain`, `-application`, `-infrastructure`, `-api`
3. Add `pom.xml` pointing to parent `pqvcf-parent`.
4. Register in root `pom.xml` `<modules>` section.
5. Add a Dockerfile to `docker/`.
6. Add service to `docker-compose.yml`.
7. Add API file to frontend `src/api/` and a new page to `src/pages/`.

---

## 26. Glossary

| Term | Definition |
|---|---|
| **PQVCF** | Post-Quantum Verifiable Compliance Framework — the name of this project |
| **PQC** | Post-Quantum Cryptography — algorithms resistant to quantum computer attacks |
| **ZKP** | Zero-Knowledge Proof — proves a statement true without revealing underlying secrets |
| **SMT** | Satisfiability Modulo Theories — decision procedure for logical formula verification |
| **Z3** | Microsoft Research SMT solver used for formal compliance verification |
| **Deontic Logic** | Branch of logic dealing with obligations, permissions, and prohibitions |
| **ML-KEM** | Module-Lattice Key Encapsulation Mechanism (FIPS 203, formerly Kyber) |
| **ML-DSA** | Module-Lattice Digital Signature Algorithm (FIPS 204, formerly Dilithium) |
| **SLH-DSA** | Stateless Hash-based Digital Signature Algorithm (FIPS 205, formerly SPHINCS+) |
| **M-LWE** | Module Learning With Errors — hard problem underlying Kyber/Dilithium security |
| **CNL** | Controlled Natural Language — simplified English structure used for regulatory encoding |
| **ODRL** | Open Digital Rights Language — W3C standard for expressing policies as JSON-LD |
| **SMT-LIB2** | Standard input language for SMT solvers including Z3 |
| **PAP** | Policy Administration Point — authors and manages compliance policies |
| **PIP** | Policy Information Point — resolves dynamic context attributes at evaluation time |
| **PDP** | Policy Decision Point — makes the final PERMIT/DENY verdict |
| **GDPR** | General Data Protection Regulation (EU) |
| **HIPAA** | Health Insurance Portability and Accountability Act (US) |
| **DPDP** | Digital Personal Data Protection Act (India) |
| **Adequacy** | Legal recognition that a country provides equivalent data protection |
| **DDD** | Domain-Driven Design — software pattern where the business domain drives architecture |
| **Clean Architecture** | Layered architecture isolating domain logic from frameworks and databases |
| **gRPC** | Google Remote Procedure Call — high-performance binary protocol for inter-service communication |
| **Protobuf** | Protocol Buffers — binary serialization format used by gRPC |
| **Flyway** | Database migration tool — manages SQL schema changes in version-controlled order |
| **Pedersen Commitment** | Cryptographic commitment scheme: `C = x·G + r·H` hiding the secret `x` |
| **Fiat-Shamir** | Transform converting interactive proofs to non-interactive (using hash as challenge) |
| **secp256r1** | NIST P-256 elliptic curve used in ZKP computations |
| **Hash Chain** | Sequence of records where each block's hash includes the previous block's hash |
| **Bouncy Castle** | Java cryptography library implementing NIST post-quantum algorithms |
| **ArchUnit** | Library for encoding and testing Java architecture rules in unit tests |
| **Testcontainers** | Spins up real Docker containers (PostgreSQL, Neo4j) for integration tests |
| **SLA** | Service Level Agreement — in PQVCF context: rolling compliance rate metric |

---

---

# Part II — Deep-Dive Reference

> This part provides exhaustive, source-code-accurate input/output contracts, exact JSON payloads, database-level details, and step-by-step internals for every module.

---

## 27. Where GDPR, HIPAA, and DPDP Come From — The Regulation Seeding Pipeline

This is one of the most important questions to answer: **how does legal text become machine data?**

### Source of Truth: Two Flyway SQL Files

The regulations are seeded into PostgreSQL at **application startup** by Flyway. There are exactly two migration scripts:

```
regulation-repository/
└── regulation-repository-infrastructure/
    └── src/main/resources/db/migration/
        ├── V1__init_schema.sql      ← Creates empty tables (regulations, articles, clauses)
        └── V2__seed_regulations.sql ← Inserts GDPR, DPDP, HIPAA with real article text
```

Flyway runs these scripts in order when the Spring Boot application starts. They run **once and only once** (Flyway tracks applied migrations in a `flyway_schema_history` table).

### V1 — Schema Creation (exact DDL)

```sql
-- Creates regulations table
CREATE TABLE regulations (
    id UUID PRIMARY KEY,
    name VARCHAR(500) NOT NULL,
    short_name VARCHAR(50) NOT NULL UNIQUE,     -- e.g. 'GDPR', 'HIPAA'
    primary_jurisdiction VARCHAR(20) NOT NULL,  -- e.g. 'EU', 'US', 'IN'
    version VARCHAR(50) NOT NULL,
    effective_date DATE,
    description TEXT,
    status VARCHAR(20) NOT NULL,                -- 'DRAFT' | 'ACTIVE' | 'DEPRECATED'
    formal_spec TEXT,                           -- SMT-LIB2 axiom set
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

-- Articles belong to a regulation (CASCADE delete)
CREATE TABLE articles (
    id UUID PRIMARY KEY,
    regulation_id UUID NOT NULL REFERENCES regulations(id) ON DELETE CASCADE,
    article_number VARCHAR(50) NOT NULL,        -- e.g. 'Article 44', '§164.312'
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,                      -- Full original legal text
    deontic_formula TEXT,                       -- SMT-LIB2 assert fragment
    odrl_policy TEXT,                           -- W3C ODRL JSON-LD string
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT unique_regulation_article UNIQUE(regulation_id, article_number)
);

-- Clauses are sub-provisions within articles (CASCADE delete)
CREATE TABLE clauses (
    id UUID PRIMARY KEY,
    article_id UUID NOT NULL REFERENCES articles(id) ON DELETE CASCADE,
    clause_number VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    clause_type VARCHAR(50) NOT NULL,           -- 'PROHIBITION'|'OBLIGATION'|'PERMISSION'|'EXEMPTION'
    CONSTRAINT unique_article_clause UNIQUE(article_id, clause_number)
);

-- Performance indexes
CREATE INDEX idx_regulations_jurisdiction ON regulations(primary_jurisdiction);
CREATE INDEX idx_regulations_status       ON regulations(status);
CREATE INDEX idx_articles_regulation_id   ON articles(regulation_id);
CREATE INDEX idx_clauses_article_id       ON clauses(article_id);
```

### V2 — Exact Seeded Regulations Data

Three regulations are seeded with real legal text and machine-readable encodings:

#### GDPR — General Data Protection Regulation (EU)

```sql
INSERT INTO regulations VALUES (
  'a0f7e42d-2090-4822-bcfa-14f76228308d',
  'General Data Protection Regulation',
  'GDPR',                         -- shortName (unique key for lookups)
  'EU',                           -- primaryJurisdiction
  '2016/679',                     -- version (the EU regulation number)
  '2018-05-25',                   -- effective_date (entered into force)
  'Regulation (EU) 2016/679 of the European Parliament and of the Council...',
  'ACTIVE',
  -- Full SMT-LIB2 formal specification:
  '(declare-const src_jurisdiction String)
   (declare-const dest_jurisdiction String)
   (declare-const data_category String)
   (declare-const adequacy_decision Bool)
   (declare-const appropriate_safeguards Bool)
   (assert (= src_jurisdiction "EU"))
   (assert (not (= dest_jurisdiction "EU")))
   (assert (or adequacy_decision appropriate_safeguards))',
  NOW(), NOW()
);
```

GDPR has 3 seeded articles:

| Article | Title | Deontic Operator | SMT Assert Fragment |
|---|---|---|---|
| Article 44 | General principle for transfers | PROHIBITION | `(assert (and (not (= dest "EU")) (not (or adequacy safeguards))))` |
| Article 45 | Transfers on the basis of adequacy | PERMISSION | `(assert (= adequacy_decision true))` |
| Article 46 | Transfers subject to appropriate safeguards | PERMISSION | `(assert (= appropriate_safeguards true))` |

Article 44 ODRL JSON-LD (exact seeded value):
```json
{
  "@context": "http://www.w3.org/ns/odrl.jsonld",
  "@type": "Policy",
  "prohibition": [
    { "target": "personal_data", "action": "transfer" }
  ]
}
```

Article 45 ODRL JSON-LD:
```json
{
  "@context": "http://www.w3.org/ns/odrl.jsonld",
  "@type": "Policy",
  "permission": [{
    "target": "personal_data",
    "action": "transfer",
    "constraint": [{
      "leftOperand": "dest_jurisdiction",
      "operator": "eq",
      "rightOperand": "adequate_countries"
    }]
  }]
}
```

#### DPDP — Digital Personal Data Protection Act (India)

```sql
INSERT INTO regulations VALUES (
  'e0a8f702-0a94-793f-b80f-3bf81bf623f8',
  'Digital Personal Data Protection Act',
  'DPDP',
  'IN',                           -- India jurisdiction
  '2023',
  '2023-08-11',                   -- When signed into law
  'An Act to provide for the processing of digital personal data...',
  'ACTIVE',
  '(declare-const dest_jurisdiction String)
   (declare-const restricted_country Bool)
   (assert (not restricted_country))',  -- Transfers to restricted countries blocked
  NOW(), NOW()
);
```

DPDP has 1 seeded article:

| Article | Title | Deontic Operator | Description |
|---|---|---|---|
| Section 16 | Transfer of personal data outside India | PROHIBITION | Government may restrict transfers to notified countries |

DPDP Section 16 Clause SMT:
```smtlib
(assert (= restricted_country true))
```
This asserts that if the destination country is on India's restricted list, the transfer MUST be blocked.

#### HIPAA — Health Insurance Portability and Accountability Act (US)

```sql
INSERT INTO regulations VALUES (
  '02b3c4d5-e6f7-0123-4567-89abcdef0123',
  'Health Insurance Portability and Accountability Act',
  'HIPAA',
  'US',
  '1996',
  '1996-08-21',
  'United States legislation that provides data privacy for safeguarding medical information (PHI).',
  'ACTIVE',
  '(declare-const data_category String)
   (declare-const access_control_enabled Bool)
   (declare-const transmission_security_enabled Bool)
   (assert (= data_category "HEALTH"))
   (assert (and access_control_enabled transmission_security_enabled))',
  NOW(), NOW()
);
```

HIPAA has 1 seeded article with 2 clauses:

| Clause | Number | Type | Requirement |
|---|---|---|---|
| Access Control | §164.312(a)(1) | OBLIGATION | Technical policies must restrict access to authorized persons/programs only |
| Transmission Security | §164.312(e)(1) | OBLIGATION | Measures must guard against unauthorized access to PHI during transmission |

### The Complete Regulation Data Model Hierarchy

```
Regulation (Aggregate Root)
│
├── id:                  UUID  (auto-generated, e.g. a0f7e42d-2090-4822-bcfa-14f76228308d)
├── name:                String (max 500 chars) — "General Data Protection Regulation"
├── shortName:           String (max 50 chars, UNIQUE, UPPERCASE) — "GDPR"
├── primaryJurisdiction: JurisdictionCode — "EU"
├── version:             String — "2016/679"
├── effectiveDate:       LocalDate — 2018-05-25
├── description:         String (TEXT) — full regulation preamble
├── status:              RegulationStatus enum — DRAFT | ACTIVE | DEPRECATED
├── formalSpec:          String (TEXT) — complete SMT-LIB2 axiom block
├── createdAt:           Instant (UTC timestamp)
├── updatedAt:           Instant (UTC timestamp)
│
└── articles: List<Article>
    │
    ├── Article
    │   ├── id:             UUID
    │   ├── regulationId:   UUID (FK → regulations.id)
    │   ├── articleNumber:  String — "Article 46"
    │   ├── title:          String — "Transfers subject to appropriate safeguards"
    │   ├── content:        String (TEXT) — full original legal text
    │   ├── deonticFormula: String — SMT-LIB2 assert fragment for this article
    │   ├── odrlPolicy:     String — W3C ODRL JSON-LD policy
    │   ├── createdAt:      Instant
    │   ├── updatedAt:      Instant
    │   │
    │   └── clauses: List<Clause>
    │       │
    │       └── Clause
    │           ├── id:           UUID
    │           ├── articleId:    UUID (FK → articles.id)
    │           ├── clauseNumber: String — "46(1)"
    │           ├── content:      String — condensed clause text
    │           └── clauseType:   ClauseType enum:
    │                             PERMISSION (P)  — green badge
    │                             OBLIGATION (O)  — amber badge
    │                             PROHIBITION (F) — red badge
    │                             EXEMPTION (E)   — purple badge
    │                             DEFINITION (D)  — gray badge
    │                             PROVISION (-)   — slate badge
```

### Regulation Status State Machine

```
                       register()
                          │
                    ┌─────▼──────┐
                    │   DRAFT    │  ← New regulation starts here
                    └─────┬──────┘
                          │ activate()
                          │ (sets effectiveDate = today if not set)
                    ┌─────▼──────┐
                    │   ACTIVE   │  ← Used in PDP evaluation
                    └─────┬──────┘
                          │ deprecate()
                          │ (superseded by newer version)
                    ┌─────▼──────┐
                    │ DEPRECATED │  ← Cannot be re-activated
                    └────────────┘

Rules:
- DEPRECATED → ACTIVE: BLOCKED (throws IllegalStateException)
- DRAFT → DEPRECATED: BLOCKED (must activate first)
- ACTIVE → DRAFT: NOT possible
```

---

## 28. Module 1 — Regulation Repository: Complete Input/Output Reference

### API 1: Register a New Regulation

**Endpoint:** `POST /api/v1/regulations`

**Request Body (exact Java record):**
```java
record RegisterRegulationCommand(
    String name,         // Full legal name
    String shortName,    // Short identifier (auto-uppercased)
    String jurisdiction, // ISO country/region code
    String version,      // Version string
    String description   // Human-readable summary
) {}
```

**Example Request:**
```json
{
  "name": "General Data Protection Regulation",
  "shortName": "gdpr",
  "jurisdiction": "EU",
  "version": "2016/679",
  "description": "EU data protection regulation"
}
```

**What Happens Internally:**
```
1. RegulationController.register(command) ← HTTP request arrives
2. RegisterRegulationUseCase.register(command) ← controller delegates
3. Regulation.create(name, shortName, jurisdiction, version, description)
   ├── Validates: name not blank, ≤500 chars
   ├── Validates: shortName not blank, ≤50 chars (auto-UPPERCASED → "GDPR")
   ├── Validates: jurisdiction not null
   ├── Validates: version not blank
   ├── Generates: RegulationId = UUID.randomUUID()
   ├── Sets status: DRAFT
   └── Raises: RegulationRegisteredEvent(id, "GDPR", "EU", "2016/679")
4. RegulationRepository.save(regulation)
   ├── Pulls domain events from aggregate
   ├── Maps Regulation → RegulationJpaEntity
   ├── SpringDataRegulationRepository.save(jpaEntity) → SQL INSERT
   └── Publishes RegulationRegisteredEvent to Spring context
5. Neo4j event listener receives RegulationRegisteredEvent
   └── Creates (:Jurisdiction {code: "EU"}) node if not exists
6. DtoMapper maps Regulation → RegulationResponse
7. HTTP 201 Created returned with body
```

**Response Body (exact Java record):**
```java
record RegulationResponse(
    String id,                    // UUID string
    String name,
    String shortName,
    String primaryJurisdiction,
    String version,
    LocalDate effectiveDate,      // null for DRAFT
    String description,
    String status,                // "DRAFT"
    String formalSpec,            // null for new regulation
    List<ArticleResponse> articles, // [] empty for new regulation
    Instant createdAt,
    Instant updatedAt
) {}
```

**Example Response:**
```json
{
  "id": "a0f7e42d-2090-4822-bcfa-14f76228308d",
  "name": "General Data Protection Regulation",
  "shortName": "GDPR",
  "primaryJurisdiction": "EU",
  "version": "2016/679",
  "effectiveDate": null,
  "description": "EU data protection regulation",
  "status": "DRAFT",
  "formalSpec": null,
  "articles": [],
  "createdAt": "2026-07-11T06:45:00Z",
  "updatedAt": "2026-07-11T06:45:00Z"
}
```

---

### API 2: Add an Article with Deontic Formula

**Endpoint:** `POST /api/v1/regulations/articles`

**Request Body:**
```java
record AddArticleCommand(
    String regulationId,         // UUID of the regulation
    String articleNumber,        // e.g. "Article 46"
    String title,
    String content,              // Full legal text
    String deonticFormula,       // SMT-LIB2 assert block
    String odrlPolicy,           // W3C ODRL JSON-LD
    List<AddClauseCommand> clauses
) {}

record AddClauseCommand(
    String clauseNumber,   // e.g. "46(1)"
    String content,
    String clauseType      // "PROHIBITION" | "OBLIGATION" | "PERMISSION" | "EXEMPTION"
) {}
```

**Example Request:**
```json
{
  "regulationId": "a0f7e42d-2090-4822-bcfa-14f76228308d",
  "articleNumber": "Article 46",
  "title": "Transfers subject to appropriate safeguards",
  "content": "In the absence of a decision pursuant to Article 45(3), a controller or processor may transfer personal data to a third country only if the controller or processor has provided appropriate safeguards...",
  "deonticFormula": "(assert (= appropriate_safeguards true))",
  "odrlPolicy": "{\"@context\": \"http://www.w3.org/ns/odrl.jsonld\", \"@type\": \"Policy\", \"permission\": [{\"target\": \"personal_data\", \"action\": \"transfer\", \"constraint\": [{\"leftOperand\": \"safeguard\", \"operator\": \"eq\", \"rightOperand\": \"standard_contractual_clauses\"}]}]}",
  "clauses": [
    {
      "clauseNumber": "46(1)",
      "content": "A controller or processor may transfer personal data only if they have provided appropriate safeguards.",
      "clauseType": "PERMISSION"
    }
  ]
}
```

**Internal Processing:**
```
1. AddArticleService.addArticle(command)
2. Load regulation: RegulationRepository.findById(regulationId)
   ├── SpringDataRegulationRepository.findById(UUID) → SQL SELECT
   └── Map JpaEntity → domain Regulation (with all articles + clauses)
3. regulation.addArticle(articleNumber, title, content)
   ├── Check uniqueness: no duplicate articleNumber in this regulation
   ├── new Article(regulationId, articleNumber, title, content)
   └── Raises: ArticleAddedEvent(regulationId, articleId, "Article 46")
4. article.setDeonticFormula(formula)  ← stores SMT-LIB2
5. article.setOdrlPolicy(odrlPolicy)   ← stores W3C ODRL
6. For each AddClauseCommand:
   article.addClause(clauseNumber, content, ClauseType.PERMISSION)
   ├── Checks: clauseNumber not duplicate
   └── new Clause(articleId, clauseNumber, content, ClauseType.PERMISSION)
7. RegulationRepository.save(regulation) → SQL UPDATE + INSERT (articles, clauses)
8. Map Article → ArticleResponse and return HTTP 201
```

**Example Response:**
```json
{
  "id": "d947e6f1-9f83-682f-a7fe-2af70af512e7",
  "regulationId": "a0f7e42d-2090-4822-bcfa-14f76228308d",
  "articleNumber": "Article 46",
  "title": "Transfers subject to appropriate safeguards",
  "content": "In the absence of a decision...",
  "deonticFormula": "(assert (= appropriate_safeguards true))",
  "odrlPolicy": "{\"@context\":\"http://www.w3.org/ns/odrl.jsonld\",...}",
  "clauses": [
    {
      "id": "00000000-0000-0000-0000-000000000003",
      "clauseNumber": "46(1)",
      "content": "A controller or processor may transfer personal data...",
      "clauseType": "PERMISSION"
    }
  ],
  "formalized": true,
  "restrictingClauseCount": 0,
  "createdAt": "2026-07-11T06:45:01Z",
  "updatedAt": "2026-07-11T06:45:01Z"
}
```

---

### API 3: Activate a Regulation

**Endpoint:** `POST /api/v1/regulations/{id}/activate`

**What Happens:**
```
1. Load regulation by UUID
2. regulation.activate()
   ├── Guard: status != DEPRECATED (throws IllegalStateException if deprecated)
   ├── status := ACTIVE
   ├── effectiveDate := LocalDate.now() (if null)
   └── Raises: RegulationStatusChangedEvent(id, DRAFT, ACTIVE)
3. Save to PostgreSQL
4. Event listener: PDP policy cache is invalidated → next evaluation fetches fresh rules
5. Return HTTP 200 with updated RegulationResponse (status: "ACTIVE")
```

---

### API 4: Get All Regulations (with filtering)

**Endpoint:** `GET /api/v1/regulations?jurisdiction=EU`

**Database Query:**
```sql
SELECT r.*, a.*, c.*
FROM regulations r
LEFT JOIN articles a ON a.regulation_id = r.id
LEFT JOIN clauses c ON c.article_id = a.id
WHERE r.primary_jurisdiction = 'EU'
ORDER BY r.short_name ASC;
```

**Response:** Array of `RegulationResponse` objects including nested articles and clauses.

---

### API 5: Search Regulations

**Endpoint:** `GET /api/v1/regulations?search=hipaa`

**Database Query (ILIKE search across 3 fields):**
```sql
SELECT r FROM RegulationJpaEntity r WHERE
  LOWER(r.name) LIKE LOWER(CONCAT('%', :query, '%')) OR
  LOWER(r.shortName) LIKE LOWER(CONCAT('%', :query, '%')) OR
  LOWER(r.description) LIKE LOWER(CONCAT('%', :query, '%'))
```

---

## 29. Module 2 — Rule Translation Engine: Complete Input/Output Reference

### How CNL Text Becomes SMT-LIB2

This is the full pipeline from a plain English sentence to machine-executable compliance assertions.

**Step 1: Frontend sends translation request**
```
POST /api/v1/translation/translate
Content-Type: application/json

{
  "cnlText": "A controller must not transfer personal data to third countries unless adequate safeguards are in place.",
  "regulationId": "a0f7e42d-2090-4822-bcfa-14f76228308d",
  "articleIndex": "Art-46-custom"
}
```

**Step 2: Java Rule Translation Service calls Python NLP**
```java
// RuleTranslationService.translate(command)
String nlpResponse = httpClient.post("http://nlp-extractor:5001/extract",
    Map.of("text", command.cnlText()));
```

**Step 3: Python NLP Service (port 5001) processes the text**

Input to `/extract`:
```json
{ "text": "A controller must not transfer personal data to third countries unless adequate safeguards are in place." }
```

Python processing:
```python
# Step 1: Normalize text
normalized = text.lower()
# → "a controller must not transfer personal data to third countries unless adequate safeguards are in place."

# Step 2: Scan deontic keyword list
DEONTIC_PROHIBITION = ["shall not", "must not", "prohibited", "forbidden", "cannot", "restricts", "restrict"]
# → "must not" found → operator = "PROHIBITION"

# Step 3: Extract constraint (text after "unless")
constraint_match = re.search(r'(?i)(?:if|unless|except when|subject to)\s+(.+)$', text)
# → matches "unless adequate safeguards are in place."
# → constraint = "adequate safeguards are in place"
# → base_sentence = "A controller must not transfer personal data to third countries"

# Step 4: Extract Subject / Action / Target via regex
# Pattern: "Subject modalVerb Action Target"
sentence_match = re.match(
    r'(?i)^(.+?)\s+(?:shall not|must not|...)\s+(.+?)\s+(.+)$',
    base_sentence
)
# → subject = "A controller"
# → action  = "transfer"
# → target  = "personal data to third countries"
```

NLP Service Response:
```json
{
  "operator":   "PROHIBITION",
  "subject":    "A controller",
  "action":     "transfer",
  "target":     "personal data to third countries",
  "constraint": "adequate safeguards are in place",
  "raw_text":   "A controller must not transfer personal data to third countries unless adequate safeguards are in place."
}
```

**Step 4: Java generates the 3 output formats**

**Output A — Deontic Logic AST (5-tuple):**
```
Formula = <PROHIBITION, "a_controller", "transfer", "personal_data", "adequate_safeguards_present">
```
Formal notation: `F(controller, transfer(personal_data)) | adequate_safeguards_present`

**Output B — SMT-LIB2 assertion:**
```smtlib
; Translated from: "A controller must not transfer personal data..."
(declare-const action String)
(declare-const data_category String)
(declare-const adequate_safeguards Bool)

; Core prohibition assertion
(assert (=> (and
              (= action "transfer")
              (= data_category "personal_data")
              (= adequate_safeguards false))
            false))  ; False = unsatisfiable = PROHIBITED
```

**Output C — W3C ODRL JSON-LD:**
```json
{
  "@context": "http://www.w3.org/ns/odrl.jsonld",
  "@type": "Set",
  "uid": "http://pqvcf.example/policy/art46-custom",
  "prohibition": [{
    "target":  "personal_data",
    "action":  "transfer",
    "assignee": "controller",
    "constraint": [{
      "leftOperand":  "adequate_safeguards",
      "operator":     "eq",
      "rightOperand": "false"
    }]
  }]
}
```

**Step 5: Contradiction check**

Before saving, the engine scans all existing translated rules for the same regulation:
```
Algorithm CheckContradiction(Candidate, ExistingRules):
  for each rule in ExistingRules:
    if rule.subject == candidate.subject
    AND rule.action  == candidate.action
    AND rule.target  == candidate.target:
      if (rule.operator == PROHIBITION AND candidate.operator == PERMISSION) OR
         (rule.operator == PERMISSION  AND candidate.operator == PROHIBITION):
        if constraints_overlap(rule.constraint, candidate.constraint):
          return CONFLICT_FOUND
  return CONSISTENT
```

**Final Translation Response:**
```json
{
  "ruleId": "uuid-auto-generated",
  "regulationId": "a0f7e42d-...",
  "articleIndex": "Art-46-custom",
  "operator": "PROHIBITION",
  "subject": "A controller",
  "action": "transfer",
  "target": "personal data to third countries",
  "constraintText": "adequate safeguards are in place",
  "smtAssertion": "(assert (=> (and (= action \"transfer\") ...) false))",
  "odrlJson": "{\"@context\":\"http://www.w3.org/ns/odrl.jsonld\",...}",
  "isValid": true,
  "conflictReason": null,
  "translatedAt": "2026-07-11T06:50:00Z"
}
```

**If a contradiction is detected:**
```json
{
  "isValid": false,
  "conflictReason": "CONFLICT: Existing rule PERMISSION(controller, transfer, personal_data) contradicts new PROHIBITION with overlapping constraint scope"
}
```

---

## 30. Module 3 — Policy Administration Point (PAP): Complete Input/Output

### Policy Lifecycle: DRAFT → ACTIVE

**Step 1: Create Policy**

```
POST /api/v1/policies
{
  "name": "Global HIPAA Patient Data Access Policy",
  "owner": "Chief Compliance Officer",
  "description": "Controls access and transfer of PHI across all cloud regions"
}
```

Internal state:
```java
Policy policy = new Policy(
    UUID.randomUUID(),          // id
    "Global HIPAA Patient...",  // name
    "Chief Compliance Officer", // owner
    "Controls access...",       // description
    PolicyStatus.DRAFT,         // status — always starts DRAFT
    Instant.now()               // createdAt
);
```

Response:
```json
{
  "id": "policy-uuid-here",
  "name": "Global HIPAA Patient Data Access Policy",
  "owner": "Chief Compliance Officer",
  "status": "DRAFT",
  "ruleLinks": [],
  "createdAt": "2026-07-11T07:00:00Z"
}
```

**Step 2: Bind Regulatory Rule**

```
POST /api/v1/policies/{policyId}/links
{
  "orgRuleName": "PHI access must have access control enabled",
  "regulatoryRuleId": "uuid-of-translated-rule-from-module2"
}
```

The `regulatoryRuleId` references a translated rule from Module 2. The binding creates:

```
Rule Link = <policyId, "PHI access must have access control enabled", regulatoryRuleId>
```

Mathematical representation:
```
L = <P_id, Rule_org, Rule_reg_id>
where P_id ∈ P (set of policies)
      Rule_reg_id ∈ R (set of translated regulatory rules from Module 2)
```

**Step 3: Activate Policy**

```
POST /api/v1/policies/{policyId}/activate
```

- Guard: policy must be in DRAFT state with at least one rule link
- Status transitions: DRAFT → ACTIVE
- Fires `PolicyPublishedEvent` → PDP receives notification → rebuilds policy cache

**Activated Policy Response:**
```json
{
  "id": "policy-uuid-here",
  "name": "Global HIPAA Patient Data Access Policy",
  "owner": "Chief Compliance Officer",
  "status": "ACTIVE",
  "ruleLinks": [
    {
      "id": "link-uuid",
      "orgRuleName": "PHI access must have access control enabled",
      "regulatoryRuleId": "translated-rule-uuid"
    }
  ],
  "createdAt": "2026-07-11T07:00:00Z"
}
```

---

## 31. Module 4 — Policy Information Point (PIP): Complete Input/Output

### Attribute Resolution — Exact Flow

**Request:**
```
POST /api/v1/pip/resolve
{
  "subjectId":     "analyst-001",
  "resourceId":    "health-records-db",
  "actionId":      "transfer",
  "sourceCountry": "IN",
  "targetCountry": "DE"
}
```

**Internal Resolution Steps:**

```
Step 1: Subject Attribute Resolution
   Cache key: "analyst-001"
   Returns: {
     "subjectId":  "analyst-001",
     "role":       "ANALYST",
     "clearance":  "LEVEL_2",
     "mfaEnabled": true,
     "department": "RESEARCH"
   }
   (If not in cache → returns DefaultSubjectAttrs: role="UNKNOWN", clearance="NONE")

Step 2: Resource Attribute Resolution
   Cache key: "health-records-db"
   Returns: {
     "resourceId":      "health-records-db",
     "classification":  "HEALTH_RECORDS",
     "sensitivity":     "HIGH",
     "dataCategory":    "HEALTH",
     "retentionPolicy": "7_YEARS"
   }

Step 3: Action Attribute Resolution
   actionId = "transfer"
   Returns: {
     "actionId":      "transfer",
     "riskLevel":     "HIGH",
     "requiresAudit": true
   }

Step 4: Adequacy Path Check (Neo4j / in-memory graph)
   Query: Is there a path IN → DE ?
   Graph traversal (BFS):
     IN → EU (via India-EU adequacy agreement) → DE (DE is in EU)
     Path found! → is_transitive_adequate = true

Step 5: Environment Assembly
   Returns: {
     "sourceCountry":           "IN",
     "targetCountry":           "DE",
     "isTransitiveAdequate":    true,
     "isLocalized":             false,  // India localizes PERSONAL but not HEALTH
     "currentTimezone":         "Asia/Kolkata",
     "evaluationTimestamp":     "2026-07-11T06:45:00Z"
   }
```

**Full PIP Response:**
```json
{
  "subjectAttributes": {
    "subjectId":  "analyst-001",
    "role":       "ANALYST",
    "clearance":  "LEVEL_2",
    "mfaEnabled": true,
    "department": "RESEARCH"
  },
  "resourceAttributes": {
    "resourceId":     "health-records-db",
    "classification": "HEALTH_RECORDS",
    "sensitivity":    "HIGH",
    "dataCategory":   "HEALTH"
  },
  "actionAttributes": {
    "actionId":      "transfer",
    "riskLevel":     "HIGH",
    "requiresAudit": true
  },
  "environmentAttributes": {
    "sourceCountry":        "IN",
    "targetCountry":        "DE",
    "isTransitiveAdequate": true,
    "isLocalized":          false
  },
  "resolvedAt": "2026-07-11T06:45:01Z"
}
```

### Inject Subject Attributes into Cache

```
POST /api/v1/pip/attributes/subject
{
  "subjectId":  "analyst-001",
  "role":       "ANALYST",
  "clearance":  "LEVEL_2",
  "mfaEnabled": true
}
```

This is used in demos/testing to pre-populate the in-memory attribute cache. In production, this would be replaced by an LDAP/Active Directory integration.

---

## 32. Module 5 — Policy Decision Point (PDP): Complete Input/Output

This is the mathematical heart of the system. Every detail of the Z3 solving step is documented here.

### Exact Input → Exact Computation → Exact Output

**Input:**
```json
{
  "subjectId":     "analyst-001",
  "resourceId":    "health-records-db",
  "actionId":      "transfer",
  "sourceCountry": "IN",
  "targetCountry": "DE",
  "policyName":    "Global HIPAA Patient Data Access Policy"
}
```

Java domain model created:
```java
new DecisionRequest(
    "analyst-001",
    "health-records-db",
    "transfer",
    "IN",       // sourceCountry
    "DE",       // targetCountry
    "Global HIPAA Patient Data Access Policy"
)
```

**Step 1 — Fetch Active Policies (gRPC call to PAP)**

gRPC method: `GetActivePolicies()`
Returns: List of `PolicyDto` objects, each containing `List<RuleLinkDto>` with SMT formulas:

```
Policy: "Global HIPAA Patient Data Access Policy" (ACTIVE)
  Rule Link: "PHI access must have access control enabled"
    → SMT: (assert (and access_control_enabled transmission_security_enabled))
  Rule Link: "HIPAA transfer adequacy check"
    → SMT: (assert (=> (= action "transfer") (= transitive_adequate true)))
```

**Step 2 — Resolve Context (gRPC call to PIP)**

gRPC method: `ResolveRequestContext(subjectId, resourceId, actionId, sourceCountry, targetCountry)`
Returns context attributes (as shown in Module 4 section above):
- `is_transitive_adequate = true` (IN→DE path exists)
- `data_category = "HEALTH"`
- `access_control_enabled = true` (HIPAA §164.312)
- `transmission_security_enabled = true`

**Step 3 — Compile SMT-LIB2 Formula**

The PDP assembles the full SMT assertion set:

```smtlib
; === PQVCF PDP SMT-LIB2 Evaluation Formula ===
; Subject: analyst-001 | Resource: health-records-db
; Action: transfer | IN → DE | Policy: Global HIPAA Patient Data Access Policy
; Generated: 2026-07-11T06:45:02Z

; -- Type Declarations --
(declare-const subject_id String)
(declare-const resource_id String)
(declare-const action String)
(declare-const source_country String)
(declare-const target_country String)
(declare-const data_category String)
(declare-const transitive_adequate Bool)
(declare-const access_control_enabled Bool)
(declare-const transmission_security_enabled Bool)

; -- Request Context Assertions (from PIP) --
(assert (= subject_id "analyst-001"))
(assert (= resource_id "health-records-db"))
(assert (= action "transfer"))
(assert (= source_country "IN"))
(assert (= target_country "DE"))
(assert (= data_category "HEALTH"))
(assert (= transitive_adequate true))
(assert (= access_control_enabled true))
(assert (= transmission_security_enabled true))

; -- Policy Rule Assertions (from PAP via Module 2 translation) --
; Rule 1: PHI access must have access control AND transmission security
(assert (and access_control_enabled transmission_security_enabled))

; Rule 2: Transfer of HEALTH data requires transitive adequacy
(assert (=> (and (= action "transfer") (= data_category "HEALTH"))
            (= transitive_adequate true)))

; -- Satisfiability Check --
(check-sat)
```

**Step 4 — Z3 Solver Execution**

The PDP executes Z3 as a sandboxed subprocess:
```java
// In PDP infrastructure layer:
ProcessBuilder pb = new ProcessBuilder("z3", "-in");
pb.redirectErrorStream(true);
Process process = pb.start();
process.getOutputStream().write(smtFormula.getBytes());
String result = new String(process.getInputStream().readAllBytes()); // "sat" or "unsat"
```

Z3 output for this example:
```
sat
```

**Step 5 — Verdict Mapping**

```
"sat"   → DecisionEffect.PERMIT  (formula is satisfiable = no contradiction)
"unsat" → DecisionEffect.DENY    (formula is unsatisfiable = rule violated)
other   → DecisionEffect.INDETERMINATE
```

**Step 6 — Build DecisionResult**

```java
new DecisionResult(
    DecisionEffect.PERMIT,
    smtFormula,           // proofTrace — the full SMT-LIB2 string
    "Z3 result: sat\nSolver: Z3 4.x\nTime: <10ms"  // validationLog
)
```

**Final PDP Response:**
```json
{
  "id": "decision-uuid",
  "subjectId": "analyst-001",
  "resourceId": "health-records-db",
  "actionId": "transfer",
  "sourceCountry": "IN",
  "targetCountry": "DE",
  "verdict": "PERMIT",
  "smtFormula": "(declare-const subject_id String)...(check-sat)",
  "solverResult": "sat",
  "validationLog": "Z3 result: sat\nSolver: Z3 4.x\nTime: 8ms",
  "evaluatedAt": "2026-07-11T06:45:03Z"
}
```

### DENY Example — Russia → Germany

For the same request with `sourceCountry = "RU"`, `targetCountry = "DE"`:

PIP returns: `is_transitive_adequate = false` (no path RU → DE in graph)
And checks localization: Russia has localization mandate for PERSONAL data.

SMT formula changes:
```smtlib
(assert (= source_country "RU"))
(assert (= target_country "DE"))
(assert (= transitive_adequate false))
; Russia localization mandate:
(assert (= is_localized true))
; Data governance rule:
(assert (=> (= is_localized true) false))  ; Localized data CANNOT be transferred
(check-sat)
```

Z3 output: `unsat`

PDP Response:
```json
{
  "verdict": "DENY",
  "solverResult": "unsat",
  "validationLog": "Z3 result: unsat\nContradiction: localization mandate blocks transfer"
}
```

---

## 33. Module 6 — PQC Crypto Layer: Complete Input/Output

### Key Generation — Exact Flow

**Request:**
```
POST /api/v1/pqc/keys
{
  "algorithmType": "ML_DSA_65",
  "alias": "audit-signer-2026"
}
```

**Internal Java Execution (PqcCryptographyService):**

```java
// Step 1: Parse algorithm type
PqcKeyType type = PqcKeyType.ML_DSA_65;  // From "ML_DSA_65"

// Step 2: Generate key pair via Bouncy Castle
PqcKeyPair pair = cryptoProvider.generatePqcKeyPair(type, "audit-signer-2026");
// BouncyCastlePqcProvider internally does:
//   MLDSAKeyPairGenerator gen = new MLDSAKeyPairGenerator();
//   gen.init(new MLDSAKeyGenerationParameters(new SecureRandom(), MLDSAParameters.ml_dsa_65));
//   AsymmetricCipherKeyPair bcPair = gen.generateKeyPair();
//   publicKeyBytes  = ((MLDSAPublicKeyParameters) bcPair.getPublic()).getEncoded();   // 1952 bytes
//   privateKeyBytes = ((MLDSAPrivateKeyParameters) bcPair.getPrivate()).getEncoded(); // 4000 bytes

// Step 3: Wrap in domain value object
PqcKeyPair keyPair = new PqcKeyPair(
    UUID.randomUUID().toString(),   // keyId
    PqcKeyType.ML_DSA_65,
    publicKeyBytes,                  // 1952 bytes for ML-DSA-65
    privateKeyBytes,                 // 4000 bytes — NEVER returned to client
    Instant.now(),
    Instant.now().plus(365, DAYS)    // expires in 1 year
);

// Step 4: Store in in-memory KMS vault
kmsRepository.save(keyPair);
// ConcurrentHashMap<String, PqcKeyPair> vault.put(keyId, keyPair)
// NOTE: Private key bytes stored ONLY in this map, NEVER written to database or disk

// Step 5: Persist public key to PostgreSQL (public key only)
// INSERT INTO pqc_key_pairs(id, alias, algorithm, public_key_hex, expires_at, created_at)
// Note: private_key_hex column does NOT exist in the DB schema — by design
```

**Response:**
```json
{
  "keyId":        "f7e3a2b1-...",
  "alias":        "audit-signer-2026",
  "algorithmType": "ML_DSA_65",
  "publicKeyHex": "308203...1a2b3c...(3904 hex chars = 1952 bytes)",
  "createdAt":    "2026-07-11T06:45:00Z",
  "expiresAt":    "2027-07-11T06:45:00Z"
}
```

> **Security Property:** Private key bytes are **never** included in any API response. They exist only in the JVM heap for the lifetime of the process.

---

### Signing a Payload — Exact Flow

**Request:**
```
POST /api/v1/pqc/sign
{
  "keyId":      "f7e3a2b1-...",
  "payloadHex": "7b2276657264696374223a225045524d4954227d"
}
```

The `payloadHex` is hex-encoded bytes. In this example it decodes to: `{"verdict":"PERMIT"}`

**Internal Execution:**

```java
// Step 1: Load key pair from in-memory vault
PqcKeyPair pair = kmsRepository.findByKeyId("f7e3a2b1-...")
    .orElseThrow(() -> new IllegalArgumentException("Key not found"));

// Step 2: Decode hex payload
byte[] payload = HexFormat.of().parseHex("7b2276657264696374...");
// → byte[] {"verdict":"PERMIT"} bytes

// Step 3: Sign using Bouncy Castle ML-DSA engine
// cryptoProvider.sign(pair, payload) internally does:
MLDSASigner signer = new MLDSASigner();
signer.init(true, privateKey);   // true = signing mode
signer.update(payload, 0, payload.length);
byte[] signature = signer.generateSignature();
// signature.length = 3293 bytes (fixed for ML-DSA-65)

// Step 4: Hex encode result
String signatureHex = HexFormat.of().formatHex(signature);
// 6586 hex characters
```

**Response:**
```json
{
  "signatureHex":    "3a7f2e...(6586 hex chars = 3293 bytes)...",
  "algorithmType":   "ML_DSA_65",
  "keyId":           "f7e3a2b1-...",
  "signatureLength":  3293
}
```

---

### Verification — Exact Flow

**Request:**
```
POST /api/v1/pqc/verify
{
  "keyId":        "f7e3a2b1-...",
  "payloadHex":   "7b2276657264696374223a225045524d4954227d",
  "signatureHex": "3a7f2e...(3293 bytes)..."
}
```

**Internal Execution:**

```java
// Load key pair from vault
PqcKeyPair pair = kmsRepository.findByKeyId(keyId);

// Decode inputs
byte[] payload   = HexFormat.of().parseHex(payloadHex);
byte[] signature = HexFormat.of().parseHex(signatureHex);

// Verify using Bouncy Castle:
MLDSASigner verifier = new MLDSASigner();
verifier.init(false, publicKey);  // false = verification mode
verifier.update(payload, 0, payload.length);
boolean valid = verifier.verifySignature(signature);
// Internally: validates A·z - c·t ≈ w check
```

**Response:**
```json
{
  "valid": true,
  "keyId": "f7e3a2b1-...",
  "algorithmType": "ML_DSA_65",
  "verifiedAt": "2026-07-11T06:45:10Z"
}
```

### Algorithm Comparison Table

| Property | ML-KEM-768 | ML-DSA-65 | SLH-DSA-SHA2-256f |
|---|---|---|---|
| NIST Standard | FIPS 203 | FIPS 204 | FIPS 205 |
| Hard Problem | M-LWE | M-SIS + M-LWE | Hash collision |
| Purpose | Key exchange | Digital signature | Digital signature |
| Public Key Size | 1,184 bytes | 1,952 bytes | 64 bytes |
| Private Key Size | 2,400 bytes | 4,000 bytes | 96 bytes |
| Output Size | Ciphertext: 1,088 bytes / SharedSecret: 32 bytes | 3,293 bytes | 49,856 bytes |
| Generation Speed | < 1 ms | < 1 ms | < 2 ms |
| Sign/Encap Speed | < 3 ms | < 3 ms | 50–200 ms |
| Verify/Decap Speed | < 3 ms | < 3 ms | < 5 ms |
| Quantum Security | 178-bit | 165-bit | 256-bit |
| Best For | Key establishment | Frequent signing | Long-term archival |

---

## 34. Module 7 — ZK Proof Engine: Complete Input/Output

### Proof Generation — Step by Step with Actual Numbers

**Request:**
```
POST /api/v1/zkp/prove
{
  "circuitType":    "DATA_RESIDENCY",
  "privateWitness": 101,
  "publicInputs":   { "region": "EU", "dataCategory": "HEALTH" }
}
```

The `privateWitness` value `101` represents a server ID. The prover wants to prove that server 101 is located in the EU, **without revealing the number 101** to the verifier.

**Internal Mathematical Execution:**

```java
// === Pedersen Commitment Setup ===
// secp256r1 curve parameters
ECNamedCurveParameterSpec spec = ECNamedCurveTable.getParameterSpec("P-256");
ECPoint G = spec.getG();          // Standard generator point
BigInteger q = spec.getN();       // Group order

// H is a secondary generator with unknown discrete log:
// H = Hash-to-curve("PQVCF-ZKP-H-GENERATOR") using SHA-256 output as x-coordinate
ECPoint H = deriveSecondaryGenerator();  // Fixed public parameter

// Secret value: x = 101 (the server ID, kept private)
BigInteger x = BigInteger.valueOf(101);

// Random blinding factor: r ← Zq
SecureRandom rng = new SecureRandom();
BigInteger r = new BigInteger(256, rng);   // e.g. r = 0x3f7a2c...

// Compute Pedersen commitment:
// C = x·G + r·H
ECPoint xG = G.multiply(x);       // 101 × Generator
ECPoint rH = H.multiply(r);       // r × H
ECPoint C  = xG.add(rH);
// C is a point on the elliptic curve — reveals nothing about x=101

// === Schnorr-Sigma Protocol ===
// Prover picks random k1, k2 ∈ Zq
BigInteger k1 = new BigInteger(256, rng);  // e.g. k1 = 0x8a1f3e...
BigInteger k2 = new BigInteger(256, rng);  // e.g. k2 = 0x2c9b4d...

// Compute commitment T:
// T = k1·G + k2·H
ECPoint T = G.multiply(k1).add(H.multiply(k2));

// Fiat-Shamir challenge (non-interactive):
// c = SHA256(G || H || C || T) mod q
MessageDigest sha = MessageDigest.getInstance("SHA-256");
sha.update(G.getEncoded(false));
sha.update(H.getEncoded(false));
sha.update(C.getEncoded(false));
sha.update(T.getEncoded(false));
BigInteger c = new BigInteger(1, sha.digest()).mod(q);

// Compute responses:
// s1 = k1 + c·x mod q
// s2 = k2 + c·r mod q
BigInteger s1 = k1.add(c.multiply(x)).mod(q);
BigInteger s2 = k2.add(c.multiply(r)).mod(q);

// Proof tuple = (c, s1, s2, T)
// C is the public commitment
```

**Proof Stored in Database:**
```
ZkProof {
  proofId:         "zkp-uuid-here"
  proofType:       DATA_RESIDENCY
  commitment: PedersenCommitment {
    commitmentBytes: [bytes of EC point C]
    blindingFactor:  [bytes of r — stored for future disclosure if needed]
    secretValue:     101 — stored in domain object ONLY, not in DB
  }
  challengeBytes:  [bytes of c]
  responseBytes:   [bytes encoding s1 and s2]
  publicInputs:    "{\"region\":\"EU\",\"dataCategory\":\"HEALTH\"}"
  verified:        false (not yet independently verified)
}
```

**API Response:**
```json
{
  "proofId":    "zkp-uuid-here",
  "proofType":  "DATA_RESIDENCY",
  "commitment": {
    "x": "04a1b2c3...32 bytes...",
    "y": "d4e5f6a7...32 bytes..."
  },
  "challenge":  "3f7a2c9b...32 bytes hex...",
  "responseS1": "8a1f3e2d...32 bytes hex...",
  "responseS2": "2c9b4d5e...32 bytes hex...",
  "publicInputs": "{\"region\":\"EU\",\"dataCategory\":\"HEALTH\"}",
  "generatedAt": "2026-07-11T06:45:05Z"
}
```

**Proof Size:** 97 bytes total for the secp256r1 points (very compact compared to SNARKs).

---

### Proof Verification — Exact Steps

**Request:**
```
POST /api/v1/zkp/verify
{
  "proofId":    "zkp-uuid-here",
  "challenge":  "3f7a2c9b...",
  "responseS1": "8a1f3e2d...",
  "responseS2": "2c9b4d5e...",
  "commitment": { "x": "04a1b2c3...", "y": "d4e5f6a7..." }
}
```

**Verification Algorithm (exact code):**
```java
// Decode commitment point C from (x, y) coordinates
ECPoint C = curve.decodePoint(commitmentBytes);

// Decode challenge and responses
BigInteger c  = new BigInteger(1, challengeBytes);
BigInteger s1 = new BigInteger(1, response_s1_bytes);
BigInteger s2 = new BigInteger(1, response_s2_bytes);

// Reconstruct T':
// T' = s1·G + s2·H - c·C
ECPoint s1G   = G.multiply(s1);
ECPoint s2H   = H.multiply(s2);
ECPoint cC    = C.multiply(c);
ECPoint T_prime = s1G.add(s2H).subtract(cC);

// Recompute Fiat-Shamir challenge:
// c_check = SHA256(G || H || C || T') mod q
MessageDigest sha = MessageDigest.getInstance("SHA-256");
sha.update(G.getEncoded(false));
sha.update(H.getEncoded(false));
sha.update(C.getEncoded(false));
sha.update(T_prime.getEncoded(false));
BigInteger c_check = new BigInteger(1, sha.digest()).mod(q);

// Accept if c_check == c
boolean valid = c_check.equals(c);
```

**Response:**
```json
{
  "proofId": "zkp-uuid-here",
  "valid":   true,
  "verifiedAt": "2026-07-11T06:45:06Z"
}
```

**Security guarantee:** The verifier confirmed the prover knows `x` (server ID in EU region) **without learning x = 101**.

---

## 35. Module 8 — Data Governance Engine: Complete Input/Output

### Governance Evaluation Algorithm — Exact Logic

**Request:**
```
POST /api/v1/governance/evaluate
{
  "sourceCountry": "RU",
  "targetCountry": "DE",
  "dataCategory":  "PERSONAL",
  "purpose":       "analytics"
}
```

**Decision Algorithm (exact Java logic):**

```java
public GovernanceDecision evaluate(String source, String target, String category, String purpose) {

    // === STEP 1: Localization Check (highest priority — overrides everything) ===
    // Hard-coded localization mandates (from jurisdiction research):
    Set<String> LOCALIZATION_COUNTRIES = Set.of("RU", "CN", "TR", "VN");
    Set<String> LOCALIZED_CATEGORIES   = Set.of("PERSONAL", "FINANCIAL", "GOVERNMENT");

    if (LOCALIZATION_COUNTRIES.contains(source) && LOCALIZED_CATEGORIES.contains(category)) {
        return new GovernanceDecision(
            "BLOCKED",
            List.of("Russia FFDL No. 242-FZ", "Russian Federal Law on Personal Data"),
            "Mandatory local data storage laws apply. Data of type " + category +
            " originating in " + source + " cannot be transferred outside the jurisdiction.",
            null  // no evidence link
        );
    }

    // === STEP 2: Adequacy Check ===
    // Hard-coded adequacy whitelist (EU Commission decisions):
    Map<String, Set<String>> ADEQUACY_MAP = Map.of(
        "EU", Set.of("US", "JP", "KR", "IN", "CA", "AU", "NZ", "CH", "UK", "IL", "UY"),
        "US", Set.of("EU", "CA"),
        "IN", Set.of("EU", "AU")
        // etc.
    );

    Set<String> adequateTargets = ADEQUACY_MAP.getOrDefault(source, Set.of());
    if (adequateTargets.contains(target)) {
        return new GovernanceDecision(
            "APPROVED",
            List.of("GDPR Article 45"),
            "Sovereign adequacy agreement recognised between " + source + " and " + target,
            "https://ec.europa.eu/info/law/law-topic/data-protection/international-dimension-data-protection"
        );
    }

    // === STEP 3: Conditional (no adequacy, no localization) ===
    return new GovernanceDecision(
        "CONDITIONAL",
        List.of("GDPR Article 46"),
        "No adequacy decision exists. Transfer requires contractual safeguards (SCCs/BCRs) " +
        "and formal compliance proof bundle.",
        null
    );
}
```

**RU → DE (PERSONAL) Response:**
```json
{
  "decisionId":   "gov-uuid",
  "sourceCountry": "RU",
  "targetCountry": "DE",
  "dataCategory":  "PERSONAL",
  "purpose":       "analytics",
  "decision":      "BLOCKED",
  "citations": [
    "Russia FFDL No. 242-FZ",
    "Russian Federal Law on Personal Data"
  ],
  "reasoning": "Mandatory local data storage laws apply. Data of type PERSONAL originating in RU cannot be transferred outside the jurisdiction.",
  "evidenceLink": null,
  "evaluatedAt": "2026-07-11T06:45:07Z"
}
```

**IN → DE (HEALTH) Response:**
```json
{
  "decision": "APPROVED",
  "citations": ["GDPR Article 45"],
  "reasoning": "Sovereign adequacy agreement recognised between IN and DE",
  "evidenceLink": "https://ec.europa.eu/..."
}
```

**US → BR (PERSONAL) Response:**
```json
{
  "decision": "CONDITIONAL",
  "citations": ["GDPR Article 46"],
  "reasoning": "No adequacy decision exists. Transfer requires contractual safeguards (SCCs/BCRs) and formal compliance proof bundle."
}
```

---

## 36. Module 9 — Compliance Monitor: Complete Input/Output

### Event Ingestion and SLA Calculation

**Request:**
```
POST /api/v1/monitor/ingest
{
  "sourceHost":    "app-server-moscow.rusinfocloud.ru",
  "sourceGeo":     "RU",
  "targetHost":    "analytics-cluster-frankfurt.aws.de",
  "targetGeo":     "DE",
  "dataCategory":  "PERSONAL",
  "sizeBytes":     1048576
}
```

**Internal Algorithm:**

```java
// Step 1: Save event to PostgreSQL
MonitorEvent event = new MonitorEvent(
    UUID.randomUUID(),
    "app-server-moscow.rusinfocloud.ru",
    "RU",
    "analytics-cluster-frankfurt.aws.de",
    "DE",
    "PERSONAL",
    1048576L,
    Instant.now()
);
eventRepository.save(event);  // INSERT INTO monitor_events(...)

// Step 2: Call Data Governance Engine (gRPC) to check legality
GovernanceResult result = governanceClient.evaluateTransfer(
    EvaluateTransferRequest.newBuilder()
        .setSourceCountry("RU")
        .setTargetCountry("DE")
        .setDataCategory("PERSONAL")
        .build()
);

// Step 3: If BLOCKED → raise critical alert
if (result.getDecision().equals("BLOCKED")) {
    ViolationAlert alert = new ViolationAlert(
        UUID.randomUUID(),
        event.getId(),
        "CRITICAL",
        "Data residency restriction violated: PERSONAL data transfer from RU to DE " +
        "is BLOCKED by Russia FFDL No. 242-FZ",
        Instant.now()
    );
    alertRepository.save(alert);  // INSERT INTO violation_alerts(...)
}
```

**SLA Calculation (rolling metric):**
```
GET /api/v1/monitor/metrics

Algorithm:
  totalEvents = COUNT(monitor_events)
  criticalViolations = COUNT(violation_alerts WHERE severity = 'CRITICAL')

  if totalEvents == 0:
    slaRate = 100.0
  else:
    slaRate = max(0.0, min(100.0, (totalEvents - criticalViolations) / totalEvents × 100.0))
```

**Metrics Response:**
```json
{
  "totalEvents":         42,
  "criticalViolations":   3,
  "warningAlerts":        8,
  "infoAlerts":           2,
  "slaRatePercent":      92.86,
  "lastEventAt":        "2026-07-11T06:45:07Z",
  "monitoringStartedAt": "2026-07-11T05:00:00Z"
}
```

**Event Ingestion Response (async):**
```json
{
  "eventId":    "monitor-event-uuid",
  "status":     "INGESTED",
  "governance": "BLOCKED",
  "alertRaised": true,
  "alertId":    "alert-uuid"
}
```

---

## 37. Module 10 — Auditing Ledger: Complete Input/Output

### Block Sealing — Exact Hash Chain Computation

**Request:**
```
POST /api/v1/ledger/seal
{
  "action":   "transfer",
  "actor":    "analyst-001",
  "target":   "health-records-db",
  "decision": "PERMIT"
}
```

**Internal Algorithm:**

```java
// Step 1: Get the previous block's hash from the ledger
String previousHash = ledgerRepository.findLastBlock()
    .map(AuditRecord::getCurrentHash)
    .orElse("GENESIS_HASH");   // If this is the very first block

// Step 2: Create new AuditRecord
AuditRecord record = new AuditRecord(
    UUID.randomUUID().toString(),   // id
    Instant.now(),                  // timestamp
    "transfer",                     // action
    "analyst-001",                  // actor
    "health-records-db",            // target
    "PERMIT",                       // decision
    previousHash,                   // previousHash = SHA256 of last block
    ""                              // currentHash — to be computed
);

// Step 3: Compute currentHash
// SHA256(previousHash + timestamp + action + actor + target + decision)
String hashInput = previousHash
    + record.getTimestamp().toString()
    + record.getAction()
    + record.getActor()
    + record.getTarget()
    + record.getDecision();

MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
byte[] hashBytes = sha256.digest(hashInput.getBytes(StandardCharsets.UTF_8));
String currentHash = HexFormat.of().formatHex(hashBytes);
// currentHash = "a3f9e2b1..." (64 hex chars = 32 bytes = 256 bits)

record.setCurrentHash(currentHash);

// Step 4: Persist to PostgreSQL
ledgerRepository.save(record);
// INSERT INTO ledger_blocks(id, timestamp, action, actor, target, decision,
//                           previous_hash, current_hash, sequence_number)
```

**Response:**
```json
{
  "id":           "block-uuid",
  "sequenceNumber": 47,
  "timestamp":    "2026-07-11T06:45:10Z",
  "action":       "transfer",
  "actor":        "analyst-001",
  "target":       "health-records-db",
  "decision":     "PERMIT",
  "previousHash": "7c3a9f2b...(hash of block 46)...",
  "currentHash":  "a3f9e2b1...(SHA256 of this block's data)...",
  "sealedAt":     "2026-07-11T06:45:10Z"
}
```

### Chain Integrity Verification

**Request:** `GET /api/v1/ledger/verify`

**Algorithm (O(N) linear scan):**
```java
List<AuditRecord> chain = ledgerRepository.findAllOrderBySequence();
String expectedHash = "GENESIS_HASH";

for (int i = 0; i < chain.size(); i++) {
    AuditRecord block = chain.get(i);

    // Check 1: Previous hash link
    if (!block.getPreviousHash().equals(expectedHash)) {
        return VerifyResult(false, i, "Broken chain connection at block " + i);
    }

    // Check 2: Recompute this block's hash and compare
    String recomputed = sha256(
        block.getPreviousHash() + block.getTimestamp() +
        block.getAction() + block.getActor() +
        block.getTarget() + block.getDecision()
    );
    if (!block.getCurrentHash().equals(recomputed)) {
        return VerifyResult(false, i, "Tampered data detected at block " + i);
    }

    expectedHash = block.getCurrentHash();  // Move to next link
}

return VerifyResult(true, -1, "Chain integrity validated. All " + chain.size() + " blocks verified.");
```

**Valid Chain Response:**
```json
{
  "valid":          true,
  "tamperedAtIndex": -1,
  "message":        "Chain integrity validated. All 47 blocks verified.",
  "blockCount":     47,
  "verifiedAt":     "2026-07-11T06:45:12Z"
}
```

**Tampered Chain Response (e.g., block #0 was edited):**
```json
{
  "valid":          false,
  "tamperedAtIndex": 0,
  "message":        "Tampered data detected at block 0: recomputed hash does not match stored currentHash",
  "blockCount":     47,
  "verifiedAt":     "2026-07-11T06:45:12Z"
}
```

### Complete Ledger Block Structure in PostgreSQL

```sql
SELECT * FROM ledger_blocks ORDER BY sequence_number;

sequence_number | id          | timestamp             | action    | actor      | target              | decision | previous_hash   | current_hash
----------------|-------------|----------------------|-----------|------------|---------------------|----------|-----------------|-------------
0               | genesis-uuid| 2026-07-11T05:00:00Z | SYSTEM    | SYSTEM     | LEDGER_INIT         | GENESIS  | GENESIS_HASH    | a3f9e2b1...
1               | uuid-1      | 2026-07-11T05:01:00Z | evaluate  | pdp-engine | health-records-db   | PERMIT   | a3f9e2b1...     | 7c3a9f2b...
2               | uuid-2      | 2026-07-11T05:02:00Z | transfer  | analyst-001| personal-records-ru | DENY     | 7c3a9f2b...     | 1e4b8d3c...
...
47              | uuid-47     | 2026-07-11T06:45:10Z | transfer  | analyst-001| health-records-db   | PERMIT   | prev-hash...    | a3f9e2b1...
```

---

## 38. The Complete Data Flow Architecture Diagram

```
═══════════════════════════════════════════════════════════════════════════════════════════════
                    LEGAL WORLD                         MACHINE WORLD
═══════════════════════════════════════════════════════════════════════════════════════════════

GDPR 2016/679           ──┐
DPDP Act 2023           ──┤  V2__seed_regulations.sql  ──►  PostgreSQL (regulations table)
HIPAA 1996              ──┘                                  + Neo4j (jurisdiction nodes)
                                                                        │
Legal Analysts add                                                       │ Module 1 API
new articles + deontic ─────────────────────────────────────────────────┤ (port 8081)
formulas via dashboard                                                   │
                                                                         ▼
Plain English text     ──► Python NLP (port 5001) ──► Module 2  ──►  translated_rules table
(CNL sentences)            /extract endpoint           (port 8082)     (SMT-LIB2 + ODRL)
                                                                         │
                                                                         │ Rule IDs referenced by
                                                                         ▼
Compliance Officers     ──► Module 3 PAP (port 8083) ──►  compliance_policies table
create policies &           /api/v1/policies              + policy_rule_links table
link them to rules                                                        │
                                                                          │ PolicyPublishedEvent
                                                                          │ (Spring ApplicationEvent)
                                                                          ▼
                                                       ┌─────────────────────────────────────┐
                                                       │  PDP Policy Cache (in-memory)       │
                                                       │  ConcurrentHashMap<policyId, rules> │
                                                       └──────────────────┬──────────────────┘
                                                                          │
A user/system triggers                                                    │
compliance evaluation   ──► Module 5 PDP (port 8085) ◄──────────────────┘
                            POST /api/v1/pdp/evaluate
                                        │
                           ┌────────────┼────────────┐
                           │            │             │
                    gRPC call to   gRPC call to  Compile SMT
                    PAP: fetch     PIP: resolve  formula from
                    active         context       context +
                    policies       attributes    policies
                           │            │             │
                           └────────────┼────────────┘
                                        │
                                        ▼
                                Z3 SMT Solver
                                (sandboxed process)
                                   │        │
                                "sat"      "unsat"
                                  │          │
                               PERMIT      DENY
                                   │
                          ┌────────┼────────┐
                          │                 │
                   Seal to ledger    Sign with PQC
                   Module 10         Module 6
                   (port 8090)       (port 8086)
                          │                 │
                   SHA-256 block      ML-DSA-65 signature
                   chained record     (3293-byte signature)
                          │                 │
                          └────────┬────────┘
                                   │
                          Optional ZK Proof
                          Module 7 (port 8087)
                          Pedersen commitment
                          Schnorr-Sigma protocol
                                   │
                                   ▼
                          Verifiable Compliance
                          Evidence Bundle
                          (returned to client)

═══════════════════════════════════════════════════════════════════════════════════════════════
                    CONTINUOUS MONITORING PIPELINE
═══════════════════════════════════════════════════════════════════════════════════════════════

Cloud traffic events  ──► Module 9 Monitor  ──► gRPC ──► Module 8 Governance
(real-time packets)       (port 8089)                    (port 8088)
                                │                                │
                                ▼                                ▼
                         Save event to             BLOCKED / APPROVED / CONDITIONAL
                         monitor_events table                    │
                                │                   if BLOCKED:  │
                                │◄───────────────────────────────┘
                                │
                         Raise ViolationAlert (CRITICAL)
                         Save to violation_alerts table
                         Recalculate SLA% metric
                                │
                                ▼
                         Real-time dashboard update
                         (frontend polls GET /api/v1/monitor/metrics)
```

---

## 39. Neo4j Jurisdiction Graph — Detailed Structure

### Node Types and Properties

```cypher
-- Jurisdiction Node
(:Jurisdiction {
    code: "EU",       -- ISO country/region code
    name: "European Union",
    type: "REGION"    -- COUNTRY | REGION | BLOC
})

-- Adequacy Edge
(source)-[:ADEQUATE_WITH {
    basis: "GDPR Article 45",
    established: "2023-07-10",
    scope: "ALL_DATA"
}]->(target)
```

### Full Seeded Jurisdiction Graph

```
Jurisdictions (Nodes):
  EU (European Union)
  US (United States)
  UK (United Kingdom)
  IN (India)
  AU (Australia)
  JP (Japan)
  CA (Canada)
  KR (South Korea)
  CH (Switzerland)
  IL (Israel)
  NZ (New Zealand)
  UY (Uruguay)
  RU (Russia)
  CN (China)
  BR (Brazil)

Adequacy Edges (ADEQUATE_WITH):
  EU ↔ US  (basis: "EU-US Data Privacy Framework, 2023")
  EU ↔ UK  (basis: "UK Adequacy Decision, 2021")
  EU ↔ IN  (basis: "EU-India Adequacy, research prototype")
  EU ↔ AU  (basis: "EU-Australia Adequacy")
  EU ↔ JP  (basis: "GDPR Art.45 Decision 2019")
  EU ↔ CA  (basis: "PIPEDA Adequacy")
  EU ↔ KR  (basis: "GDPR Art.45 Decision 2021")
  EU ↔ CH  (basis: "Swiss Federal Data Protection Act")
  EU ↔ IL  (basis: "GDPR Art.45 Decision 2011")
  EU ↔ NZ  (basis: "GDPR Art.45 Decision 2012")
  EU ↔ UY  (basis: "GDPR Art.45 Decision 2012")
  US ↔ CA  (basis: "USMCA Data Chapter")
  IN ↔ AU  (basis: "India-Australia CECA")

NO edges:
  RU (Russia) — localization mandate, no adequacy agreements
  CN (China)  — localization mandate, no adequacy agreements
  BR (Brazil) — LGPD enacted but no EU adequacy decision
```

### Transitivity Path Resolution Example

```cypher
-- Query: Can data flow from IN to NZ?
MATCH path = shortestPath(
  (:Jurisdiction {code: "IN"})-[:ADEQUATE_WITH*]->(:Jurisdiction {code: "NZ"})
)
RETURN path IS NOT NULL AS is_adequate, length(path) AS hop_count

-- Result:
-- Path: IN → EU → NZ
-- is_adequate = true, hop_count = 2
-- PIP returns: is_transitive_adequate = true
```

---

## 40. Domain Events — Complete List

Domain events are raised inside domain aggregates and consumed by event listeners in the infrastructure layer.

| Event Class | Raised By | Consumed By | Effect |
|---|---|---|---|
| `RegulationRegisteredEvent` | `Regulation.create()` | Neo4j Listener | Creates Jurisdiction node in Neo4j |
| `RegulationStatusChangedEvent` | `Regulation.activate()` / `deprecate()` | PDP Cache Invalidator | Signals PDP to refresh policy cache |
| `ArticleAddedEvent` | `Regulation.addArticle()` | Audit Logger | Logs article addition event |
| `PolicyPublishedEvent` | `Policy.activate()` | PDP gRPC Notifier | Triggers PDP policy cache rebuild |

**RegulationRegisteredEvent fields:**
```java
record RegulationRegisteredEvent(
    RegulationId regulationId,
    String shortName,           // e.g. "GDPR"
    JurisdictionCode jurisdiction, // e.g. "EU"
    String version
) {}
```

**Neo4j Listener on RegulationRegisteredEvent:**
```java
@EventListener
@Async
public void onRegulationRegistered(RegulationRegisteredEvent event) {
    neo4jTemplate.save(new JurisdictionNode(event.jurisdiction().getCode()));
}
```

---

## 41. Object Mapping Pipeline — Domain ↔ JPA ↔ DTO

This shows exactly how data transforms at every boundary crossing.

### Regulation: HTTP → Domain → JPA → DB → Domain → HTTP

```
HTTP Request JSON
    ↓
RegisterRegulationCommand (Java record)
    ↓ Application Layer (AddArticleService)
Regulation (Domain Aggregate)
    - id = RegulationId(UUID)
    - status = DRAFT
    - articles = []
    ↓ Infrastructure Layer (PostgresRegulationRepositoryAdapter.mapToJpa())
RegulationJpaEntity (@Entity)
    - id = UUID
    - status = "DRAFT" (String, not enum)
    ↓ Spring Data JPA
SQL INSERT INTO regulations(...)
    ↓ (on next read)
SQL SELECT → RegulationJpaEntity
    ↓ Infrastructure (mapToDomain())
Regulation (reconstituted domain object)
    - id = RegulationId.of(UUID)
    - status = RegulationStatus.DRAFT (enum)
    ↓ DtoMapper (MapStruct)
RegulationResponse (Java record)
    ↓ Spring MVC (Jackson)
HTTP Response JSON
```

---

## 42. Complete gRPC Service Definitions

### PDP calls PAP: GetActivePolicies

```protobuf
// proto/pap.proto
syntax = "proto3";
package com.pqvcf.pap;

service PolicyAdministrationService {
  rpc GetActivePolicies (GetActivePoliciesRequest) returns (GetActivePoliciesResponse);
  rpc CreateCompliancePolicy (CreatePolicyRequest) returns (PolicyResponse);
}

message GetActivePoliciesRequest {}  // No filter — returns all ACTIVE policies

message GetActivePoliciesResponse {
  repeated PolicyDto policies = 1;
}

message PolicyDto {
  string policyId     = 1;
  string name         = 2;
  string status       = 3;  // "ACTIVE"
  repeated RuleLinkDto ruleLinks = 4;
}

message RuleLinkDto {
  string linkId           = 1;
  string orgRuleName      = 2;
  string regulatoryRuleId = 3;
  string smtAssertion     = 4;  // The actual SMT-LIB2 formula fragment
}
```

### PDP calls PIP: ResolveRequestContext

```protobuf
// proto/pip.proto
service PolicyInformationService {
  rpc ResolveRequestContext (ResolveContextRequest) returns (ResolvedContext);
}

message ResolveContextRequest {
  string subjectId     = 1;
  string resourceId    = 2;
  string actionId      = 3;
  string sourceCountry = 4;
  string targetCountry = 5;
}

message ResolvedContext {
  SubjectAttributes  subject     = 1;
  ResourceAttributes resource    = 2;
  ActionAttributes   action      = 3;
  EnvironmentAttributes env      = 4;
}

message SubjectAttributes {
  string subjectId  = 1;
  string role       = 2;
  string clearance  = 3;
  bool   mfaEnabled = 4;
}

message ResourceAttributes {
  string resourceId      = 1;
  string classification  = 2;
  string sensitivity     = 3;
  string dataCategory    = 4;
}

message EnvironmentAttributes {
  string sourceCountry        = 1;
  string targetCountry        = 2;
  bool   isTransitiveAdequate = 3;
  bool   isLocalized          = 4;
}
```

### Compliance Monitor calls Data Governance: EvaluateTransfer

```protobuf
// proto/governance.proto
service DataGovernanceService {
  rpc EvaluateTransfer (TransferRequest) returns (TransferDecision);
}

message TransferRequest {
  string sourceCountry = 1;
  string targetCountry = 2;
  string dataCategory  = 3;
  string purpose       = 4;
}

message TransferDecision {
  string decision     = 1;  // "APPROVED" | "CONDITIONAL" | "BLOCKED"
  repeated string citations = 2;
  string reasoning    = 3;
}
```

---

## 43. Spring Boot Auto-Configuration Details

Each Spring Boot service starts with:

```java
@SpringBootApplication(
    scanBasePackages = {
        "com.pqvcf.<module>.api",           // Controllers, security
        "com.pqvcf.<module>.application",   // Services, mappers
        "com.pqvcf.<module>.infrastructure",// JPA repos, adapters
        "com.pqvcf.shared"                  // Shared kernel
    }
)
public class <ModuleName>Application {
    public static void main(String[] args) {
        SpringApplication.run(<ModuleName>Application.class, args);
    }
}
```

### Spring Beans Wired Per Module (example for Regulation Repository)

```
@Component PostgresRegulationRepositoryAdapter implements RegulationRepository
    └── @Autowired SpringDataRegulationRepository (JPA)
    └── @Autowired ApplicationEventPublisher (Spring)

@Service RegisterRegulationService implements RegisterRegulationUseCase
    └── @Autowired RegulationRepository

@RestController RegulationController
    └── @Autowired RegisterRegulationUseCase
    └── @Autowired GetRegulationUseCase
    └── @Autowired ListRegulationsUseCase
    └── @Autowired UpdateRegulationUseCase

@Configuration SecurityConfig
    └── produces SecurityFilterChain bean

@EventListener Neo4jJurisdictionSyncListener
    └── @Async void onRegulationRegistered(RegulationRegisteredEvent)
```

---

## 44. Frontend API Layer — Exact TypeScript Contracts

### regulationsApi.ts — Complete Contract

```typescript
// Base URL determined by Vite proxy or nginx
const BASE = '/api/v1/regulations';

// All TypeScript interfaces mirror the Java RegulationResponse record exactly

export interface ClauseResponse {
  id:           string;
  clauseNumber: string;
  content:      string;
  clauseType:   'PERMISSION' | 'OBLIGATION' | 'PROHIBITION' | 'EXEMPTION' | 'DEFINITION' | 'PROVISION';
}

export interface ArticleResponse {
  id:                   string;
  regulationId:         string;
  articleNumber:        string;
  title:                string;
  content:              string;
  deonticFormula:       string | null;
  odrlPolicy:           string | null;
  clauses:              ClauseResponse[];
  formalized:           boolean;
  restrictingClauseCount: number;
  createdAt:            string;  // ISO 8601
  updatedAt:            string;
}

export interface RegulationResponse {
  id:                  string;
  name:                string;
  shortName:           string;
  primaryJurisdiction: string;
  version:             string;
  effectiveDate:       string | null;
  description:         string;
  status:              'DRAFT' | 'ACTIVE' | 'DEPRECATED';
  formalSpec:          string | null;
  articles:            ArticleResponse[];
  createdAt:           string;
  updatedAt:           string;
}

// API functions
export const listAllRegulations = ():
  Promise<RegulationResponse[]> =>
  axios.get(BASE).then(r => r.data);

export const getRegulationById = (id: string):
  Promise<RegulationResponse> =>
  axios.get(`${BASE}/${id}`).then(r => r.data);

export const registerRegulation = (cmd: {
  name: string; shortName: string; jurisdiction: string;
  version: string; description: string;
}): Promise<RegulationResponse> =>
  axios.post(BASE, cmd).then(r => r.data);

export const activateRegulation = (id: string):
  Promise<RegulationResponse> =>
  axios.post(`${BASE}/${id}/activate`).then(r => r.data);

export const addArticle = (cmd: {
  regulationId: string; articleNumber: string; title: string;
  content: string; deonticFormula?: string; odrlPolicy?: string;
  clauses: { clauseNumber: string; content: string; clauseType: string; }[];
}): Promise<ArticleResponse> =>
  axios.post(`${BASE}/articles`, cmd).then(r => r.data);
```

---

## 45. Error Handling — HTTP Status Codes

All services return consistent error responses:

| HTTP Status | When Returned | Example |
|---|---|---|
| `201 Created` | Successful creation (POST) | Regulation registered, article added |
| `200 OK` | Successful read or update | GET regulation, activate |
| `204 No Content` | Successful delete | DELETE draft regulation |
| `400 Bad Request` | Validation error | Blank shortName, duplicate articleNumber |
| `404 Not Found` | Entity not found | Unknown regulation UUID |
| `409 Conflict` | Business rule violation | Duplicate shortName across jurisdictions |
| `422 Unprocessable Entity` | Domain invariant broken | Activate deprecated regulation |
| `500 Internal Server Error` | Unexpected error | Database unavailable |

**Error Response Body:**
```json
{
  "timestamp": "2026-07-11T06:45:00Z",
  "status":    400,
  "error":     "Bad Request",
  "message":   "Article 'Article 46' already exists in regulation 'GDPR'",
  "path":      "/api/v1/regulations/articles"
}
```

---

## 46. Shared Kernel — Cross-Module Types

The `pqvcf-shared-kernel` module contains types used across all modules:

```java
// com.pqvcf.shared.domain
public abstract class AggregateRoot<ID> {
    private final List<DomainEvent> domainEvents = new ArrayList<>();
    protected void raiseEvent(DomainEvent event) { domainEvents.add(event); }
    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> copy = new ArrayList<>(domainEvents);
        domainEvents.clear();
        return copy;
    }
}

public abstract class ValueObject { /* equals/hashCode contract */ }

public interface DomainEvent {
    Instant occurredOn();
}

// com.pqvcf.shared.types
public final class JurisdictionCode {
    private final String code;  // e.g. "EU", "IN", "US"
    public static JurisdictionCode of(String code) {
        // Validates against ISO 3166-1 alpha-2/3 codes
    }
}
```

---

*Document Version: 2.0 — Updated: 2026-07-11*
*Maintainers: PQVCF Research Team*
*This document is the authoritative single source of truth for the PQVCF system architecture.*
