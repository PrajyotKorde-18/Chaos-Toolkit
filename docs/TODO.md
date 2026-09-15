# TODO.md
## Granular Task List & Project Completion Status

---

## Phase 0 & 1: Core Scaffolding & Single-Service Fault Injection — [COMPLETED & VERIFIED]
- [x] `chaos-toolkit` control plane (`FaultStore`, `FaultQueryController`, `AdminController`, kill switches)
- [x] `chaos-agent` library v0.1.0 (`@ChaosLatency`, `@ChaosException`, `ChaosFaultRegistry`, `ChaosInjectionAspect`, `AutoConfiguration`)
- [x] `payment-service` demo target app (`PaymentGateway`, `PaymentController`, port 8081)
- [x] Verified all 8 single-service verification steps live (latency, exception, blast radius, kill switch engage/release)

---

## Phase 2: Multi-Service + Resilience4j Verification — [COMPLETED & VERIFIED]
- [x] `inventory-service` demo app (port 8082, `@ChaosLatency`, `@ChaosException`)
- [x] `order-service` demo app (port 8080, Resilience4j Circuit Breakers on Payment & Inventory clients with graceful fallback handlers)
- [x] Multi-service baseline flow verified (`POST /orders/create` -> `200 OK COMPLETED`)
- [x] `VerificationService` automated test execution engine measuring $t_{detect}$, $t_{recover}$, and fallback rate
- [x] `GET /orders/circuit-breakers` live telemetry endpoint

---

## Phase 3: Resilience Scoring Engine — [COMPLETED & VERIFIED]
- [x] `ResilienceScoreCalculator` implementing mathematical formula:
      $$\text{Score} = (0.35 \cdot \text{detect} + 0.35 \cdot \text{recover} + 0.30 \cdot \text{fallback}) \times \text{severity\_weight}$$
- [x] Per-service customizable thresholds (`T_detect_threshold`, `T_recover_threshold`)
- [x] Letter grading (A through F) and detailed plain-text evaluation
- [x] `GET /api/v1/verification/scores` endpoint

---

## Phase 5: Automated Escalation Engine — [COMPLETED & VERIFIED]
- [x] Multi-stage progressive degradation plans (`EscalationPlan`, `EscalationStage`)
- [x] `EscalationEngine` executing staged fault sequences with real-time SLO latency & error rate monitoring
- [x] Automated breaking point discovery upon SLO breach
- [x] `POST /api/v1/escalation/run` and `GET /api/v1/escalation/history`

---

## Phase 6: STRIDE Security Chaos Module — [COMPLETED & VERIFIED]
- [x] `SecurityChaosSimulator` covering STRIDE categories safely with synthetic probes:
      - `CREDENTIAL_STUFFING_SIMULATION`
      - `PRIVILEGE_ESCALATION_SIMULATION`
      - `MALFORMED_PAYLOAD_SIMULATION`
      - `LATERAL_MOVEMENT_SIMULATION`
      - `CONFIG_DRIFT_SIMULATION`
- [x] `POST /api/v1/security-chaos/run` and `GET /api/v1/security-chaos/history`

---

## Phase 7: Post-Mortems, Reporting & History — [COMPLETED & VERIFIED]
- [x] `PostMortemReportGenerator` generating deterministic markdown post-mortem reports with telemetry and RCA (no external LLM calls)
- [x] `ExperimentHistoryStore` tracking all experiment runs and comparisons
- [x] `GET /api/v1/reports/post-mortem`

---

## Optional / Future Enhancements (What is Left if Desired)

- [ ] **Docker Compose & Infrastructure Chaos (Phase 4)**:
      - `docker-compose.yml` packaging all 4 services into a container network
      - Container pausing (`docker pause`) and network partition scripts (`pumba` / `tc` / `iptables`)
- [ ] **Persistent PostgreSQL DB Storage**:
      - Replace in-memory `ExperimentHistoryStore` with Spring Data JPA + PostgreSQL database
- [ ] **Visual Web UI / Dashboard**:
      - Single-page React/Vite dashboard to view active faults, real-time circuit breaker states, trigger experiments, and display post-mortem reports
- [ ] **Housekeeping**:
      - Delete leftover empty nested folder `chaos-agent/chaos-agent` (inert IDE artifact)
