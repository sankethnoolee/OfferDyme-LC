# OfferDyne — Dynamic Settlement Optimizer

**Problem Statement #8 · Collections AI Hackathon**

Real-time settlement negotiation workbench. The collections agent is on a live chat with the borrower; as the borrower speaks, the system classifies the objection, pulls account history, and suggests an offer (amount + strategy + one-line script) — always inside lender floor/ceiling guardrails.

---

## 1. How it maps to PS#8

| PS#8 requirement | Where it lives | Status |
|---|---|---|
| 100% guardrail compliance (never below floor, never above ceiling) | `ClaudeService.finalize()` — hard clamps after the LLM responds; reports `WITHIN_LIMITS \| FLOORED \| CAPPED` | ✅ |
| Objection classification (5 types) | `ClaudeService.java` prompt + `ClaudeDecisionDto.objectionType` — `AFFORDABILITY \| HARDSHIP \| PARTIAL_WILLINGNESS \| AVOIDANCE \| DISPUTE` | ✅ |
| ≥ 4 negotiation strategies with distinct behaviour | `negotiation_strategy` table + `ClaudeService.applyStrategyContext()` — `HOLD`, `LOWER`, `REFRAME_INSTALLMENTS`, `BUNDLE` | ✅ |
| 3+ lender profiles with different floors/ceilings | `negotiation_policy` seed — LC / HDFC / ICICI / Kotak with product × DPD-bucket overrides | ✅ |
| Settlement acceptance rate vs static baseline | `GET /api/analytics/acceptance-rate` — buckets by `source` (`CLAUDE_AI` vs `AGENT`), returns `verdict` | ✅ |
| Multi-turn session state | `Transcript` + `ChatMessage` rows; `SuggestController` keeps `session_id → transcript_id` map | ✅ |
| PS#8 live demo API contract | `POST /api/settlement/suggest` → `TurnResultDto` (objection_type, objection_confidence, suggested_strategy, suggested_offer_amount, suggested_offer_percent, script_line, guardrail_status, lender_floor, lender_ceiling, customer_history_summary) | ✅ |

---

## 2. Negotiation strategies

| Code | When the AI picks it | Effect |
|---|---|---|
| `HOLD` | Borrower wavering, not rejecting | Keep offer, add urgency (supervisor approval, time-bound waiver) |
| `LOWER` | Strong affordability/hardship objection | Move 5–10% toward the policy floor — never below |
| `REFRAME_INSTALLMENTS` | Borrower accepts total but rejects lump sum | Split total into 2–3 EMIs inside policy's `min/max_installments` |
| `BUNDLE` | Borrower has multiple accounts at the same lender | Package all accounts into one settlement, blended discount |

---

## 3. Tech stack

- **Backend** — Java 17, Spring Boot 3.2.5, JPA/Hibernate, Lombok
- **Database** — H2 in-memory, auto-seeded from `data.sql`
- **AI layer** — Claude (Sonnet 4.6) via AWS Bedrock Converse API
- **Frontend** — React 18, plain CSS (teal + gold theme), no heavy UI framework
- **Session state** — `ConcurrentHashMap` in `SuggestController` (no Redis needed for 36h demo)

---

## 4. Run it

Prereqs: Java 17+, Maven 3.8+, Node 18+.

```bash
# Backend  (port 8080)
cd backend
mvn spring-boot:run

# Frontend (port 3000, proxies /api to 8080)
cd frontend
npm install
npm start
```

- H2 console → `http://localhost:8080/h2-console` · JDBC URL `jdbc:h2:mem:settlementdb` · user `sa` · empty password
- Claude key already embedded in `application.properties` (Bedrock API key). Override with env var `CLAUDE_API_KEY` if needed.

If the Bedrock call fails the backend falls back to a heuristic recommendation — the UI still demos the full plumbing.

---

## 5. PS#8 live-demo flow (7 minutes)

1. **Pick a customer** — left pane shows `Vikram Singh` with 4 accounts (LC × 2, HDFC, ICICI) and a gold "Portfolio" pill.
2. **Open `LC-PL-10001`** — outstanding ₹3,12,400, DPD 68, and **2 prior rejected agent offers** visible in the Settlements tab.
3. **Judge plays borrower** — types `"I don't have money right now"` in ChatPanel.
4. **System responds** — right panel shows: objection = `AFFORDABILITY`, strategy = `LOWER`, offer clamped inside `2,49,920 – 2,96,780` band (LC DPD_60 policy), one-line script.
5. **Judge pushes back** — `"I really can't pay in one shot"` → objection flips to `PARTIAL_WILLINGNESS`, strategy switches to `REFRAME_INSTALLMENTS`, offer stays the same total but splits into 3 EMIs.
6. **Guardrail probe** — judge insists on absurdly low number 10 times; `guardrail_status` stays `FLOORED` and the offer never drops below floor.
7. **Switch lender live** — in the H2 console update `account.lender_id` (or use the portfolio view of a customer with multiple lenders) — floor/ceiling updates instantly.
8. **Show analytics** — `GET /api/analytics/acceptance-rate` returns `verdict: AI_BETTER` (AI 75% vs agent 25% in seeded history).

---

## 6. API contract — PS#8 `/suggest` endpoint

```
POST /api/settlement/suggest
```

Request:
```json
{
  "account_id": 1,
  "customer_utterance": "I can't pay the full amount right now, I lost my job",
  "session_id": "SESS_789",
  "turn_number": 3
}
```

Response (inside `TurnResultDto.decision`):
```json
{
  "objectionType": "HARDSHIP",
  "objectionConfidence": 0.91,
  "selectedStrategyCode": "REFRAME_INSTALLMENTS",
  "recommendedOfferAmount": 280000,
  "suggestedOfferPercent": 56.0,
  "scriptLine": "I understand. We can split this into 2 payments of ₹14,000 — would that work?",
  "guardrailStatus": "WITHIN_LIMITS",
  "offerFloor": 249920,
  "offerCeiling": 296780,
  "customerHistorySummary": "Rejected lump sum twice (Mar 10, Mar 28). Not yet offered installments."
}
```

Session state: the `session_id` is mapped to an internal `transcript_id` by `SuggestController`, so subsequent turns with the same `session_id` feed context (last 6 messages) to Claude.

---

## 7. Key REST endpoints

```
# Live negotiation
POST /api/settlement/suggest                    PS#8 contract endpoint
POST /api/chat/accounts/turn                    internal turn handler (account scope)
POST /api/chat/customers/turn                   internal turn handler (portfolio scope)
GET  /api/chat/transcripts/{id}                 full transcript + messages
GET  /api/chat/transcripts/{id}/history         immutable audit snapshots

# Settlements
GET  /api/settlements/accounts/{accountId}      proposals per account
POST /api/settlements/from-claude               persist AI-suggested offer
POST /api/settlements/manual                    agent-authored offer
POST /api/settlements/{id}/status               accept / reject / counter

# Config (lender guardrails + strategies)
GET  /api/policy/policies?lenderId=X
POST /api/policy/policies                       upsert
GET  /api/policy/strategies?lenderId=X&onlyActive=true

# Master data
GET  /api/agents                                login dropdown
GET  /api/lenders
GET  /api/customers?lenderId=X                  customers + nested accounts
GET  /api/customers/{id}
GET  /api/accounts/{id}

# PS#8 acceptance metric
GET  /api/analytics/acceptance-rate             AI vs static baseline
```

---

## 8. Data model

```
Lender (1)     ── (*) FieldAgent
Lender (1)     ── (*) NegotiationPolicy     — product × DPD × floor/ceiling
Lender (1)     ── (*) NegotiationStrategy   — HOLD / LOWER / REFRAME_INSTALLMENTS / BUNDLE
Customer (1)   ── (*) Account               — one customer, many accounts (possibly across lenders)
Account (*)    ── (1) Lender
Account (*)    ── (0..1) FieldAgent
Transcript (*) ── (1) Customer, (0..1) Account, (0..1) FieldAgent
ChatMessage (*)── (1) Transcript
TranscriptHistory (*) ── (1) Transcript     — immutable turn snapshots (audit)
Settlement (*) ── (1) Account, (1) Customer, (0..1) Transcript
Settlement (*) ── (*) Account               — linkedAccounts for BUNDLE
```

`Transcript.account == null` flags a **portfolio-level** transcript (all customer accounts at once) — needed for the `BUNDLE` strategy.

---

## 9. Guardrails — how they stay unbreachable

1. Every turn calls `PolicyService.resolveForAccount()` which picks the most specific `negotiation_policy` row (`product × DPD` > `DPD only` > `lender default`).
2. The resolved floor/ceiling are **injected into the system prompt** so Claude sees them.
3. After Claude responds, `ClaudeService.finalize()` **hard-clamps** `discount%` and `offer amount` to the policy range, regardless of what Claude returned.
4. `guardrail_status` is computed **before** clamping, so the UI shows `FLOORED` / `CAPPED` / `WITHIN_LIMITS` and the demo can prove the clamp fired.
5. Every `Settlement` row is re-validated on save — the DB never stores an offer outside its policy.

No code path exists for the LLM's output to bypass step 3 or 5.

---

## 10. Layout

```
settlement/
├── README.md
├── .gitignore
├── backend/
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/lc/settlement/
│       │   ├── SettlementApplication.java
│       │   ├── config/CorsConfig.java
│       │   ├── entity/        Lender · FieldAgent · Customer · Account · Transcript
│       │   │                  ChatMessage · TranscriptHistory · Settlement
│       │   │                  NegotiationPolicy · NegotiationStrategy
│       │   ├── repository/
│       │   ├── dto/           ClaudeDecisionDto · TurnResultDto · …
│       │   ├── service/       ClaudeService · PolicyService · ChatService
│       │   │                  SettlementService · AccountService · …
│       │   └── controller/    SuggestController (PS#8) · ChatController
│       │                      SettlementController · PolicyController
│       │                      AnalyticsController · …
│       └── resources/
│           ├── application.properties
│           └── data.sql       lenders, agents, customers, accounts,
│                              policies, strategies, prior settlements
└── frontend/
    ├── package.json
    └── src/
        ├── App.jsx                3-column live-call layout
        ├── theme.css
        ├── api/client.js
        └── components/
            ├── Login.jsx           agent picker
            ├── CustomerList.jsx    customers + nested accounts + Portfolio pill
            ├── AccountDetail.jsx
            ├── PortfolioDetail.jsx customer-level view (BUNDLE scope)
            ├── ChatPanel.jsx       customer-utterance input, auto agent reply
            ├── RecommendationPanel.jsx  sliders, objection badge, script line
            ├── SettlementPanel.jsx proposals tab
            └── HistoryPanel.jsx    audit snapshots
```

---

## 11. What's intentionally out of scope

- Voice / audio — text input only (PS#8 explicitly scopes this out).
- Actual payment processing, agreement signing, telephony.
- RL agent (Stable Baselines3) — listed as PS#8 bonus, not implemented.
- Multi-tenant deployment, auth, persistence beyond H2 in-memory.
