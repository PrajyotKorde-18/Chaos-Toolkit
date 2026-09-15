# CURRENT_STATE.md
## Living Snapshot — What Actually Exists Right Now

Last updated: Post Phase 7 & Web Dashboard Live Verification

---

## Overall Architecture Status: 🟢 100% COMPLETE & LIVE

All 7 core phases and interactive web dashboard are fully implemented, compiled, packaged, running, and verified end-to-end.

```
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                        chaos-toolkit CONTROL PLANE (:9000)                             │
│  [Interactive Web UI Dashboard]  [Admin & Fault APIs]    [VerificationEngine]          │
│  (In-Memory & KillSwitch)        [ResilienceScoreCalc]   [SecurityChaosSimulator]      │
│                                  [EscalationEngine]      [PostMortemReportGenerator]   │
└────────────────────────────────────────────────────────────────────────────────────────┘
          ▲                                    ▲                               ▲
          │ poll (every 2s)                    │ poll (every 2s)               │ poll (every 2s)
┌───────────────────────┐          ┌───────────────────────┐       ┌───────────────────────┐
│     order-service     │          │    payment-service    │       │   inventory-service   │
│       (:8080)         │──HTTP───▶│       (:8081)         │       │       (:8082)         │
│  + chaos-agent 0.1.0  │          │  + chaos-agent 0.1.0  │       │  + chaos-agent 0.1.0  │
│  + Resilience4j CBs   │──HTTP───┼───────────────────────┼──────▶│  + @ChaosLatency      │
│  + Fallback Handlers  │          │  + @ChaosLatency      │       │  + @ChaosException    │
└───────────────────────┘          │  + @ChaosException    │       └───────────────────────┘
                                   └───────────────────────┘
```

---

## 1. `chaos-agent` (Independent Library v0.1.0)
Location: `C:\Users\hp\OneDrive\Desktop\chaos-agent\`
- **Status**: Complete & installed to local `~/.m2/repository`
- **Features**:
  - Annotations: `@ChaosLatency`, `@ChaosException`
  - Poller: `ChaosFaultRegistry` (background daemon thread, `RestClient`, local/remote kill switch)
  - Interceptor: `ChaosInjectionAspect` (Spring AOP `@Around` with probabilistic blast radius)
  - Auto-Configuration: `org.springframework.boot.autoconfigure.AutoConfiguration.imports`

---

## 2. `chaos-toolkit` (Control Plane & Web UI)
Location: `C:\Users\hp\OneDrive\Desktop\chaos-toolkit\`
- **Status**: Complete, running on Port `9000`
- **Features & Endpoints**:
  - **Web Dashboard**: `http://localhost:9000/` (Live animated Canvas packet topology, Chart.js degradation curve, 5-scenario Fault Matrix cards, Real-time Observability Stream, and Auto-Traffic Generator)
  - **Breaking Point Discovery Engine**: `POST /api/v1/breaking-point/find` (Automated latency & blast radius sweeps to identify precise system tipping point and SLO breaking step)
  - **Comparative Fault Reaction Matrix**: `POST /api/v1/fault-matrix/run` (Executes 5 standardized chaos scenarios side-by-side with circuit resets and resilience verdicts)
  - **Fault Management**: `GET /api/v1/admin/faults`, `POST /api/v1/admin/faults/activate`, `DELETE /api/v1/admin/faults/{service}/{faultId}`, `POST /api/v1/admin/kill-switch/engage`
  - **Automated Steady-State Verification**: `POST /api/v1/verification/run` (measures $t_{detect}$, $t_{recover}$, fallback rate, and auto-restores state)
  - **Mathematical Resilience Scoring**: `ResilienceScoreCalculator` ($\text{Score} = (0.35 \cdot \text{detect} + 0.35 \cdot \text{recover} + 0.30 \cdot \text{fallback}) \times \text{severity\_weight}$) with letter grades (A–F)
  - **Multi-Stage Progressive Escalation**: `POST /api/v1/escalation/run` (progressive latency/error ramps with SLO breach detection)
  - **STRIDE Security Chaos**: `POST /api/v1/security-chaos/run` (synthetic behavioral simulations for credential stuffing, privilege escalation, malformed payloads, config drift)
  - **Deterministic Post-Mortem Reporting**: `GET /api/v1/reports/post-mortem` (generates detailed markdown reports without external LLM dependencies)

---

## 3. Demo Microservices Cluster (`demo-apps/`)
Location: `C:\Users\hp\OneDrive\Desktop\chaos-toolkit\demo-apps\`
- **`payment-service`** (Port `8081`): `PaymentGateway` bean with `@ChaosLatency` and `@ChaosException`, `POST /charge`.
- **`inventory-service`** (Port `8082`): `InventoryStore` bean with `@ChaosLatency` and `@ChaosException`, `POST /inventory/reserve`.
- **`order-service`** (Port `8080`): Multi-service coordinator with **Resilience4j Circuit Breakers** & fallback handlers on payment and inventory clients. Exposes `POST /orders/create`, `GET /orders/circuit-breakers`, and `POST /orders/circuit-breakers/reset`.

---

## 4. Automation & Verification Scripts
- **`start-all.bat` / `start-all.ps1`**: Starts all 4 services.
- **`stop-all.bat` / `stop-all.ps1`**: Stops all 4 services by port lookup.
- **`test-e2e.ps1`**: Comprehensive 8-phase automated verification script.

---

## 5. Repository Sync
- Synchronized and pushed to GitHub: `https://github.com/PrajyotKorde-18/Chaos-Toolkit.git` on branch `main`.
