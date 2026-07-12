# Module 9 — Continuous Compliance Monitor

## 1. Objective
Establish a continuous compliance monitoring framework to audit cloud data transfer events in real-time, generate alerts on regulatory violations, and maintain a rolling Service Level Agreement (SLA) status metric.

## 2. Research Motivation
Modern cloud systems process hundreds of data packets every second. Relying on periodic audits can miss critical transient violations (e.g. temporary routing errors that violate data residency boundaries). By continuously ingesting traffic events and evaluating them against active data governance rules, operators can immediately detect and respond to compliance drifts.

## 3. Mathematical Model (Rolling SLA Calculation)
Let $E = \{e_1, e_2, \dots, e_N\}$ represent the set of ingested cloud events. Let $V = \{v \in V \mid v.severity = \text{CRITICAL}\}$ represent the set of critical compliance violations raised.
The rolling compliance SLA rate ($SLA_{rate}$) is calculated as:
$$SLA_{rate} = \begin{cases}
100.0 & \text{if } |E| = 0 \\
\max\left(0.0, \min\left(100.0, \frac{|E| - |V|}{|E|} \times 100.0\right)\right) & \text{otherwise}
\end{cases}$$

## 4. Assumptions
1. Data movement ingestion represents the actual production traffic channel.

## 5. Inputs & Outputs
- **Ingestion**: Inputs: Source hostname/geo, target hostname/geo, data category, size in bytes. Outputs: Void (asynchronous ingestion).
- **Ledger query**: Inputs: Void. Outputs: Event log list, violation alert list, rolling SLA rate percentage.

## 6. Algorithms
### Real-Time Event Violation Check
```text
Algorithm IngestEvent(Event):
    SaveEvent(Event)
    isLegallyApproved = CallGovernance(Event.Source, Event.Destination, Event.Category)
    if not isLegallyApproved:
        RaiseAlarm(Event.Id, CRITICAL, "Data residency restriction violated")
        IncrementViolationCount()
```

## 7. Complexity Analysis
- **Ingestion processing time**: $O(1)$ constant time operations using local caching lists. Ingestion checking executes in $<1\text{ms}$.

## 8. Security Analysis
- Generated violation warning reports are written to persistent ledgers for audit evidence verification.

## 9. Design Decisions
- **Severity-based Filtering**: SLA rate calculations ignore INFO/WARNING level flags and deduct percentage points only on CRITICAL residency violations, preventing false alarms.

## 10. APIs
- **REST Endpoints**:
  - `POST /api/v1/monitor/ingest` (Ingest packet event)
  - `GET /api/v1/monitor/metrics` (Get SLA rates)
- **gRPC Services**:
  - `IngestSimulatedEvent`

## 11. Unit & Integration Tests
- **SlaMetricsTest**: Domain model checks.
- **ComplianceMonitoringServiceTest**: Service operations check.
- **ComplianceMonitorControllerIT**: End-to-end events ingestion, alert triggers, and SLA updates integration checks.

## 12. Example Execution
1. Traffic simulator logs transfer of personal records from `RU` (Russia) to `DE` (Germany).
2. Monitor service verifies pathway with the data governance client, which returns `BLOCKED`.
3. Critical warning alarm is immediately raised, reducing rolling SLA health dials score.

## 13. Limitations
- Does not automatically block physical connections (acts as passive observer, leaving enforcement to the SDN controller).

## 14. Future Improvements
- Integrate with SDN OpenFlow APIs to dynamically reroute blocked traffic paths.
