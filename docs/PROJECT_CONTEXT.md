# PROJECT_CONTEXT.md
## Chaos Engineering Toolkit — Master Context for AI Coding Agents

**Read this file first, completely, before writing or modifying any code.**
This document is the single source of truth for what this project is, how it is
structured, and what rules must never be violated. If anything in your training data
or general assumptions about "how chaos engineering tools are usually built" conflicts
with this document, THIS DOCUMENT WINS. Do not substitute your own architecture
preferences. Do not "improve" the design by adding things not listed here. Do not skip
steps. If something is genuinely ambiguous or missing, STOP and ask, rather than
guessing and proceeding.

---

## 1. What this project is

A Chaos Engineering Toolkit for Spring Boot microservices. It deliberately injects
controlled failures (latency, exceptions, network partitions, service kills, security-
behavior anomalies) into running Spring Boot applications, then verifies and scores how
well those applications detect and recover from the failures.

It is built by a final-year CS student as a long-term portfolio project, to be reused
across multiple job applications (primary target: Juspay, a payments infrastructure
company; secondary targets: Amadeus, DE Shaw). It must be genuinely complete and
correct, not a rushed prototype — this is a "build once, keep, reuse for years" asset,
not a one-off demo.

---

## 2. Non-negotiable ground rules

1. **NO real malware, virus, exploit code, or attack tooling of any kind.** Security
   chaos scenarios simulate the *behavioral symptoms* of compromise (anomalous calls,
   malformed payloads, permission anomalies) using safe, synthetic actions against the
   developer's own test environment ONLY. Never write anything resembling actual
   malicious payloads, signature-based malware, or real exploit code. Name all such
   components by what they DO (e.g. `simulate_credential_stuffing`), never by what they
   pretend to be (never `trojan.py`, `virus.py`, etc.)
2. **Decoupled architecture is mandatory, not optional.** `chaos-agent` (the library
   embedded in target apps) and `chaos-toolkit` (the control plane) are SEPARATE Maven
   projects. The control plane must NEVER contain code specific to any one target
   application. It only ever deals in generic `(serviceName, faultId, faultType,
   blastRadius, ...)` data. Adding a new target application must require ONLY: (a) add
   the `chaos-agent` dependency, (b) add a `chaos.agent.*` config block naming the
   service, (c) annotate a method. Zero control-plane code changes for a new target app.
3. **YAML everywhere, never `.properties`.** All Spring config files are
   `application.yml`. All experiment/escalation definitions (built in later phases) are
   YAML files.
4. **Fail-safe, always.** Any uncertainty (control plane unreachable, fault lookup
   fails, kill switch state unknown) must resolve to "apply NO fault" — never assume
   chaos should be active. This applies at every layer: agent startup before first poll,
   poll failures, kill switch engaged, fault expired.
5. **No LLM calls inside the toolkit itself.** Any "plain-language observations" or
   report generation (Phase 7) must be deterministic, rule-based logic over captured
   metrics/thresholds — never a call to an LLM API. This is a design decision made
   deliberately for determinism and explainability, not an oversight.
6. **Build order matters. Do not skip ahead.** See MILESTONES.md for the exact phase
   sequence. Do not implement Phase 3 (scoring) before Phase 2 (verification) is working
   end to end, etc. Each phase must be provably working (see "Definition of Done" in
   MILESTONES.md) before starting the next.
7. **Java 21, Spring Boot 4.0.8.** Do not downgrade or use APIs from Spring Boot 3.x
   documentation without verifying they still exist in 4.0.8 — several artifact names
   changed between major versions (see NAMING_AND_VERSIONS.md for the exact list of
   known renames already discovered).

---

## 3. High-level architecture

```
┌───────────────────────────────────────────────────────────────────────────┐
│                         CHAOS CONTROL PLANE (chaos-toolkit)                 │
│                                                                               │
│  AdminController   FaultQueryController   Escalation Engine (later)         │
│  (operator API)    (agent-facing API)     Verification/Scoring (later)      │
│         │                    │                        │                     │
│         └────────────────────┴──────────┬─────────────┘                    │
│                                          ▼                                   │
│                                  FaultStore (in-memory, Phase 1)             │
└───────────────────────────────────────────────────────────────────────────┘
              ▲ poll every 2s (agents pull, control plane never pushes)
              │
   ┌──────────┴──────────┐   ┌────────────────────┐   ┌────────────────────┐
   │  payment-service      │   │  order-service       │   │ inventory-service   │
   │  + chaos-agent         │   │  + chaos-agent         │   │  + chaos-agent       │
   │  (Phase 1 target)       │   │  (Phase 2 target)       │   │  (Phase 2 target)     │
   └────────────────────────┘   └────────────────────────┘   └────────────────────┘
```

**chaos-agent** (thin library, embedded in every target app):
- Contains ONLY: annotations, AOP advice, a polling client, local fault cache
- Never decides WHAT fault to run or WHY — purely mechanical: "does the cache say
  there's an active fault for this method right now? if yes and blast-radius roll
  passes, apply it."
- Talks to the control plane ONLY via the polling loop, on a background thread. The
  actual request-handling path (e.g. a REST controller method) NEVER makes a network
  call to the control plane — it only reads the already-polled local cache. This keeps
  fault-injection overhead low and means control-plane downtime never blocks real
  traffic.

**chaos-toolkit** (the control plane, standalone Spring Boot app):
- Owns all "intelligence": experiment definitions, active fault state, the escalation
  engine, verification/scoring, history, reporting, dashboard (all later phases)
- Exposes two kinds of API: agent-facing (read-only, polled) and operator-facing
  (admin: activate/deactivate faults, kill switch)

---

## 4. Actual project locations on disk (Windows, current developer machine)

```
C:\Users\hp\OneDrive\Desktop\chaos-toolkit\      <- the control plane project
C:\Users\hp\OneDrive\Desktop\chaos-agent\         <- the agent library project (SIBLING folder, not nested)
```

These are two SEPARATE, INDEPENDENT Maven projects/IntelliJ projects. Do not merge them
into one project or one pom.xml. Do not create one inside the other.

**Known naming facts (do not deviate):**
- Control plane: groupId `in.strikes`, artifactId `chaos-toolkit`, base package
  `in.strikes.chaostoolkit`, main class `ChaosToolkitApplication`, runs on port 9000
- Agent library: groupId `in.strikes`, artifactId `chaos-agent`, base package
  `in.strikes.chaosagent`, version `0.1.0`, NO main class (it's a library, not a
  runnable app), packaging `jar`
- Demo target app (Phase 1, not yet built as of this document): will be
  `payment-service`, runs on port 8081, depends on `chaos-agent`

**A stray, harmless leftover folder** named `chaos-agent` may exist nested inside the
`chaos-agent` project root itself (containing only `.idea/workspace.xml`, no source
code) from earlier IDE setup mistakes. It is inert and can be ignored or deleted; it is
NOT a real module and must not be treated as one.

---

## 5. Companion documents (read these too, in this order)

1. `PROJECT_CONTEXT.md` (this file) — the master rules and architecture
2. `MILESTONES.md` — the exact phase-by-phase build order with Definition-of-Done for each
3. `TODO.md` — granular, checkbox-level task list, updated as work progresses
4. `NAMING_AND_VERSIONS.md` — exact package names, artifact names, dependency versions,
   and every Spring Boot 3->4 rename already discovered through trial and error
5. `CURRENT_STATE.md` — a living snapshot of exactly what has been built so far and what
   hasn't, so no phase gets redone or skipped by mistake

Do not proceed with implementation until you have read all five documents.
