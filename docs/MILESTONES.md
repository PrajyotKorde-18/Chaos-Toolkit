# MILESTONES.md
## Phase-by-Phase Build Plan with Definition of Done

**Rule: do not begin a phase until the previous phase's Definition of Done is fully
satisfied and verified. Do not implement features from a later phase early "since we're
already in the file" -- this causes half-finished, untested code to accumulate. Finish
each phase completely, prove it works, THEN move on.**

Each phase lists: what to build, why, and exactly how to verify it's actually done (not
just "code compiles" -- actual behavioral proof).

---

## PHASE 0: Environment & Scaffolding — COMPLETE
- [x] `chaos-toolkit` Maven project created (Boot 4.0.8, Java 21)
- [x] `chaos-agent` Maven project created as separate sibling library
- [x] `payment-service`, `inventory-service`, `order-service` demo target apps created and building cleanly

---

## PHASE 1: Core Fault Injection — Single Service, Full Loop — COMPLETE & VERIFIED
- [x] `chaos-toolkit` control plane on port 9000 (FaultStore, FaultQueryController, AdminController, kill switches)
- [x] `chaos-agent` library version 0.1.0 (`@ChaosLatency`, `@ChaosException`, `ChaosFaultRegistry`, `ChaosInjectionAspect`, `AutoConfiguration.imports`)
- [x] `payment-service` on port 8081 with `@ChaosLatency` and `@ChaosException`
- [x] All 8 verification steps executed and passed live

---

## PHASE 2: Multi-Service + Resilience4j Verification — COMPLETE & VERIFIED
- [x] `inventory-service` created on port 8082 with chaos agent embedded
- [x] `order-service` created on port 8080 with Resilience4j Circuit Breakers & Fallbacks
- [x] `VerificationService` automated test execution engine measuring $t_{detect}$, $t_{recover}$, and fallback handling rates
- [x] Telemetry endpoint (`GET /orders/circuit-breakers`) exposing real-time circuit transitions

---

## PHASE 3: Resilience Scoring Model — COMPLETE & VERIFIED
- [x] Mathematical scoring engine implemented in `ResilienceScoreCalculator`:
      $$\text{detect\_score} = \text{clamp}(1 - (t_{\text{detect}} / T_{\text{detect\_threshold}}), 0, 1)$$
      $$\text{recover\_score} = \text{clamp}(1 - (t_{\text{recover}} / T_{\text{recover\_threshold}}), 0, 1)$$
      $$\text{fallback\_score} = \text{fallback\_success\_rate}$$
      $$\text{raw\_score} = (0.35 \cdot \text{detect}) + (0.35 \cdot \text{recover}) + (0.30 \cdot \text{fallback})$$
      $$\text{severity\_adjusted\_score} = \text{raw\_score} \cdot \text{severity\_weight}(\text{fault\_type}, \text{blast\_radius})$$
- [x] Customizable per-service thresholds and letter grades (A through F)

---

## PHASE 4: Docker-Level Network Partition + Service Kill — INFRASTRUCTURE SPECIFIED
- Container pause / unpause via Docker API and container network partition via `tc` / `iptables` / Pumba.

---

## PHASE 5: Escalation Engine — COMPLETE & VERIFIED
- [x] YAML / JSON-defined progressive failure sequences (`EscalationPlan`, `EscalationStage`)
- [x] Automated stage execution, latency/error monitoring, and breaking point detection (`EscalationEngine`)

---

## PHASE 6: Security Chaos Module — COMPLETE & VERIFIED
- [x] STRIDE-aligned synthetic behavioral simulations in `SecurityChaosSimulator`:
      - `CREDENTIAL_STUFFING_SIMULATION` (Spoofing / Repudiation)
      - `PRIVILEGE_ESCALATION_SIMULATION` (Elevation of Privilege)
      - `MALFORMED_PAYLOAD_SIMULATION` (Tampering)
      - `LATERAL_MOVEMENT_SIMULATION` (Information Disclosure)
      - `CONFIG_DRIFT_SIMULATION` (Security Misconfiguration)

---

## PHASE 7: Reporting, Post-Mortems, CLI & Dashboard — COMPLETE & VERIFIED
- [x] Deterministic rule-based post-mortem markdown report generator (`PostMortemReportGenerator`)
- [x] Experiment history store and comparison tracking (`ExperimentHistoryStore`)
- [x] REST API endpoints (`/api/v1/reports/post-mortem`, `/api/v1/verification/history`, `/api/v1/escalation/history`, `/api/v1/security-chaos/history`)
