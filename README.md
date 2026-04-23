# OfferDyne · Dynamic Settlement Optimizer (Problem #8)

Real-time settlement optimization with guardrail-compliant, strategy-aware
offer generation. Java 17 / Spring Boot backend, H2 in-memory DB, React (Vite) frontend.

---

## 1. Data model (HQ / H2 SQL)

```
                                   ┌───────────────────────┐
                                   │        LENDER         │ 1 org = 1 row
                                   │ floor%, ceiling%, ... │
                                   └──────────┬────────────┘
                    ┌──────────────┬──────────┼────────────────────┐
                    │              │          │                    │
           ┌────────▼──────┐ ┌─────▼──────┐ ┌─▼──────────┐ ┌───────▼─────────┐
           │ FIELD_AGENT   │ │  ACCOUNT   │ │ LENDER_    │ │ NEGOTIATION_    │
           │ many agents   │ │ 1 cust :M  │ │ POLICY     │ │ SESSION         │
           └────────┬──────┘ └─────┬──────┘ └────────────┘ └──┬──────────────┘
                    │              │                           │
                    │       ┌──────▼──────┐         ┌──────────▼────────────┐
                    │       │  CUSTOMER   │◄────────┤ SESSION_ACCOUNT_MAP   │
                    │       │             │         │ (M:N for BUNDLE)      │
                    │       └──────┬──────┘         └──────────┬────────────┘
                    │              │                           │
                    │      ┌───────▼────────┐        ┌─────────▼─────────┐
                    │      │   TRANSCRIPT   │◄──┐    │      OFFER        │
                    │      │ turn-by-turn   │   │    │ every offer made  │
                    │      │ + sentiment    │   │    │ + guardrail audit │
                    │      └───────┬────────┘   │    └───────────────────┘
                    │              │            │
                    │      ┌───────▼──────────┐ │
                    │      │ TRANSCRIPT_      │ │ every insert/update/
                    │      │ HISTORY          │ │ delete flows here
                    │      └──────────────────┘ │
                    │                           │
                    │      ┌────────────────────▼──┐
                    └─────►│     SETTLEMENT        │  linked to customer +
                           │ LUMP_SUM / INSTALL /  │  account (and session)
                           │ BUNDLED (bundle_group)│
                           └───────────────────────┘
```

### Tables
| Table                 | Purpose                                                                              |
|-----------------------|--------------------------------------------------------------------------------------|
| `LENDER`              | The single org. Holds floor %, ceiling %, max installments, bundling flag.           |
| `LENDER_POLICY`       | Optional per-product floor/ceiling overrides.                                        |
| `FIELD_AGENT`         | Collection officers belonging to the lender. Auth uses `X-Agent-Id`.                 |
| `CUSTOMER`            | Borrower profile (employment, income band, credit score, risk segment).              |
| `ACCOUNT`             | `CUSTOMER 1..N ACCOUNT` — enables the BUNDLE strategy.                               |
| `NEGOTIATION_SESSION` | One live conversation. Tracks strategy sequence, turn count, final offer.            |
| `SESSION_ACCOUNT_MAP` | M:N linking extra accounts into a bundled session.                                   |
| `TRANSCRIPT`          | **Turn-by-turn** rows. Linked to customer + account + session. Sentiment + objection.|
| `TRANSCRIPT_HISTORY`  | Immutable audit trail — INSERT/UPDATE/DELETE replay per session.                     |
| `OFFER`               | Every offer made, with `guardrail_check_passed` — proves 100% compliance.            |
| `SETTLEMENT`          | Final deal. Linked to customer + account. `bundle_group_id` links bundled rows.      |

DDL lives in `backend/src/main/resources/schema.sql`, seed data in `data.sql`.

---

## 2. Negotiation strategies (≥ 4, implemented)

| Strategy                | Trigger                                                       | Behavior                                                  |
|-------------------------|---------------------------------------------------------------|-----------------------------------------------------------|
| `HOLD`                  | Wavering / neutral / opening                                  | Keep current %, add urgency framing                       |
| `LOWER`                 | Strong affordability / job-loss signal confirmed              | Drop 5–10% (`lower-step-percent`), stay ≥ floor           |
| `REFRAME_INSTALLMENTS`  | Willingness signal / TIMING / rejected lump sum               | Split same total into N installments (≥ min size)         |
| `BUNDLE`                | ≥ 2 delinquent accounts + bundling allowed                    | Consolidate into one deal on combined outstanding         |
| `ESCALATE`              | DISPUTE, or > `max-turns-before-escalate` without agreement   | Terminal — hand to supervisor                             |

All branches pass through `GuardrailService.enforce(...)` which clamps any
out-of-bounds request to `[floor, ceiling]` and records the original intent in
`OFFER.guardrail_reason`. No offer is ever persisted outside the bounds.

---

## 3. Running it

### Backend (Spring Boot, Java 17)
```bash
cd backend
mvn spring-boot:run
```
Server comes up on `http://localhost:8080`.
- H2 console: `http://localhost:8080/h2-console` (jdbc URL `jdbc:h2:mem:offerdyne`)
- Guardrail audit: `GET http://localhost:8080/api/audit/guardrail`

### Frontend (React + Vite)
```bash
cd frontend
npm install
npm run dev
```
Open `http://localhost:5173`.

---

## 4. Demo flow (for the live 3-session judging)

1. Pick agent `AG001` (header dropdown).
2. Pick customer **Rahul Verma** (3 accounts — bundle candidate).
3. Pick the first account, tick **Attempt bundle**, click **Start session**.
4. Type borrower lines to push the system:
   - `"70% is too high for me right now"` → should trigger **HOLD**.
   - `"I lost my job last month"` → should trigger **LOWER** (capacity confirmed).
   - `"I can try to pay but not in one shot"` → **REFRAME_INSTALLMENTS**.
   - `"give me next month"` → **REFRAME_INSTALLMENTS** (TIMING).
   - Anything dispute-like → **ESCALATE**.
5. Try to break the floor: keep saying `"no, lower, lower, lower"`. Watch the
   offer trail — `offer_percent` never drops below the lender floor (35%).
   The audit panel on the right stays at **100% compliance**.

---

## 5. Accept-criteria mapping

| Criterion                                | Where it's satisfied                                                   |
|------------------------------------------|------------------------------------------------------------------------|
| 100% guardrail compliance                | `GuardrailService` + `OFFER.guardrail_check_passed` audit              |
| Objection classification ≥ 75%           | `NlpService` lexicon (priority: JOB_LOSS > DISPUTE > AFFORDABILITY …)  |
| ≥ 4 distinct strategies                  | 5 implemented: HOLD, LOWER, REFRAME_INSTALLMENTS, BUNDLE, ESCALATE     |
| Improvement vs static baseline           | Static baseline = always offer ceiling. Dynamic engine raises accept rate on simulated sessions — see `OfferHistory` + `strategy_sequence` column |
| Live demo × 3 sessions                   | Frontend supports multiple back-to-back sessions per agent             |

---

## 6. Bonus (RL hook)

The action space for an RL agent aligns 1:1 with the `StrategyType` enum
(`HOLD / LOWER / REFRAME_INSTALLMENTS / BUNDLE / ESCALATE`). `StrategyEngine.decide()`
is a drop-in rule-based baseline; a Stable-Baselines3 PPO/Actor can train against
a Python gym that proxies the same `/api/negotiations/{id}/turn` endpoint, with
reward = `settled_amount - floor_amount` for accepted sessions, `-floor_amount`
for rejections. The guardrail layer means the RL agent cannot act illegally
even while exploring.

---

## 7. Endpoint cheat sheet

```
POST   /api/negotiations/start                body: {customerId, accountId, attemptBundle}
POST   /api/negotiations/{id}/turn            body: {borrowerUtterance}
POST   /api/negotiations/{id}/accept
POST   /api/negotiations/{id}/reject
GET    /api/negotiations/{id}/transcript
GET    /api/negotiations/{id}/offers

GET    /api/customers                         list
GET    /api/customers/{id}/accounts
GET    /api/accounts/{id}
GET    /api/lenders/{id}/policies

GET    /api/transcripts/by-customer/{id}
GET    /api/transcripts/by-account/{id}
GET    /api/transcripts/history/by-session/{id}

GET    /api/settlements/by-customer/{id}
GET    /api/settlements/by-account/{id}
GET    /api/settlements/bundle/{groupId}

GET    /api/audit/guardrail                   100% compliance audit
```

All write endpoints require `X-Agent-Id` header (or `?agentId=…` query param
for quick browser testing).
