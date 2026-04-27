# OfferDyne — 7-minute demo script

**Problem:** PS#8 · Dynamic Settlement Optimizer
**Stack:** Java 17 · Spring Boot · H2 · React · deterministic NLP + rule-based strategy engine
**Deliverables on screen:** `architecture-flow.svg`, `scenario-matrix.svg`, live app, Reports modal

---

## Pre-show checklist (run 5 minutes before your slot)

1. Backend up on `:8080` — hit `/api/lenders` once to confirm.
2. Frontend up on `:3000` — log in as Rahul Mehta (LC Bank).
3. Run `POST /api/demo/reset` — wipes transcripts + settlements for a clean board.
4. Pre-open three browser tabs: (a) the app, (b) `architecture-flow.svg`, (c) `scenario-matrix.svg`.
5. Close Slack, Outlook, notifications — zero popups during the demo.
6. Zoom the browser to 110% so judges at the back can read the chat bubbles.

---

## Time budget

| Beat | Time | What lives on screen |
|------|------|----------------------|
| 1 · Hook | 0:00 – 0:30 | Title slide or scenario-matrix (blurred), voice only |
| 2 · Thesis | 0:30 – 1:15 | architecture-flow.svg |
| 3 · How it works | 1:15 – 2:00 | architecture-flow.svg (pointer-driven) |
| 4 · Strategy matrix | 2:00 – 3:00 | scenario-matrix.svg |
| 5 · Live demo | 3:00 – 6:00 | the app |
| 6 · Bonus + close | 6:00 – 7:00 | back to flow chart or blank |

---

## Beat 1 · Hook (0:00 – 0:30) — 30 seconds

> *(Do not introduce yourself yet. Open cold, in the borrower's voice.)*

"**I lost my job last month. I can't pay everything right now.**"
"**I can pay — just not all at once.**"

Both borrowers owe the exact same three-lakh rupees on the exact same personal loan. Today, every collections agent across India reads from the same script — same offer, same 35% discount, same wait-and-escalate.

**The gap between a 35% and a 55% settlement is rarely the amount. It's whether the agent listens to what the borrower is actually saying.**

*(Beat. Now introduce.)*

I'm Sanketh, and this is **OfferDyne — the Dynamic Settlement Optimizer**.

---

## Beat 2 · Thesis (0:30 – 1:15) — 45 seconds

*(Switch to `architecture-flow.svg`.)*

Today's settlement is **static** — one floor, one offer, one script. PS#8 asked us to make it **dynamic**: adjust the offer's amount, framing, and sequencing turn-by-turn, based on what the borrower says and how they say it, while staying **100% inside** every lender's floor and ceiling.

We built that in four parts — a React agent workbench, a Spring Boot backend, a deterministic NLP + strategy engine, and a non-bypassable guardrail clamp. Five lender profiles, five negotiation strategies, and every offer can be traced back to the borrower utterance that produced it.

**The headline claim is this:** on 200 simulated sessions, dynamic beats static by roughly 17 percentage points of acceptance, and the guardrail was never breached — not once — even when the simulator deliberately tried to push below floor.

---

## Beat 3 · How it works (1:15 – 2:00) — 45 seconds

*(Pointer on `architecture-flow.svg`, tracing left-to-right.)*

A borrower utterance comes in from the agent's chat panel, hits `ChatService`, and lands in `ClaudeService` — the brain.

Three things happen in parallel:

First, the **NLP layer** runs a sentiment score and a deterministic objection classifier. It labels every utterance as one of six types — hardship, affordability, partial willingness, avoidance, dispute, or none — with a confidence score.

Second, **policy lookup** pulls the negotiation policy for this lender, this product, this DPD bucket. That's where the floor and ceiling come from — LC Bank's credit-card NPA policy is not Kotak's home-loan policy.

Third, the **strategy picker** combines the NLP labels, the session signals — turn count, rejection streak, offer ledger — and selects exactly one of five strategies: HOLD, LOWER, REFRAME, BUNDLE, or ESCALATE.

*(Point at the red box at the bottom.)*

Before anything leaves the backend, the **guardrail clamp** enforces `floor ≤ offer ≤ ceiling`. That single step is why we can promise 100% compliance — it's not a check, it's a clamp.

---

## Beat 4 · Strategy matrix (2:00 – 3:00) — 60 seconds

*(Switch to `scenario-matrix.svg`. This is the most persuasive slide — spend time on it.)*

Five rows, same base account, same outstanding, same DPD. Only the borrower signal changes — and the offer changes with it.

*(Pointer to row 1.)* "**I lost my job**" — the classifier says HARDSHIP 89%, sentiment dives to minus 0.55. The engine fires **LOWER**, shaves the discount from 18 to 25%, and time-boxes it.

*(Pointer to row 2 — same sentence structure as PS#8's own brief.)* "**I can pay, just not everything**" — this is literally the example in the problem statement. Same underlying account, but now the classifier sees PARTIAL_WILLINGNESS. We don't drop the price — we **REFRAME** it into three EMIs of 83,307.

*(Jump to row 4.)* Multi-account borrower. The engine detects Rakesh has three HDFC products open and picks **BUNDLE** — one blended 30% offer across the full 9.27 lakh.

*(Jump to row 5 — this is the showstopper.)* Row 5 is the attack case. Dispute, borrower's been rejecting at the floor for three turns straight. A naive system would keep shaving and breach. Ours flips to **ESCALATE** — no new offer, route to grievance. Guardrail held.

Now let me show you this live.

---

## Beat 5 · Live demo (3:00 – 6:00) — 3 minutes

*(Switch to the app. Lender: LC Bank. Agent: Rahul Mehta.)*

### Demo 1 — LOWER (55s)

1. Click **Vikram Singh** → LC-PL-10001.
2. Type in the chat: **"I lost my job last month, I just can't pay this amount."**
3. Hit Submit.

*(Narrate while it resolves.)*

Right panel: you can see the **objection pill flipped to HARDSHIP with confidence around 85%**, sentiment negative, strategy **LOWER**. Offer slider moved from around ₹2.56L to ₹2.34L — discount shaved by 8%. Guardrail pill is green. The blue "Say this now" card has the exact empathetic script — the agent reads it verbatim. I hit Save Settlement — it appears in the history timeline below with strategy code `LOWER`.

### Demo 2 — REFRAME_INSTALLMENTS (55s)

*(Same customer, same account — crucial that it's the SAME account.)*

4. Type: **"I can pay it, just not all at once."**
5. Submit.

*(Narrate.)*

Objection flipped to **PARTIAL_WILLINGNESS**. Strategy flipped to **REFRAME**. Notice — the discount percent didn't move, but the payment plan changed to **EMI_3** with 3 installments. Same total value, different shape. That's PS#8's exact ask: the gap wasn't the amount, it was the sequencing.

### Demo 3 — BUNDLE + ESCALATE (70s)

6. Switch lender to **HDFC Bank** in the topbar.
7. Click **Rakesh Gupta** → hit the gold **Portfolio** bundle button.
8. Type: **"I have three loans with you, can we do something together?"**

Strategy: **BUNDLE**. Notice the orange badge — two HDFC accounts linked, blended ₹6.49L offer across six EMIs. Save it. The settlement row in history shows **two account tags**, not one — proof the bundle links them properly.

9. Now the **guardrail attack**. Type: **"That's still too much. I can't go above 30%"** — submit. Offer moves toward floor. Type: **"Still too high"** — submit. Guardrail pill goes red — **FLOORED**. Type: **"This isn't fair, I'll go to court"** — submit.

Strategy: **ESCALATE**. Save button is disabled. No new offer. Guardrail held at floor across three rejection turns. That's the 100% compliance claim in one visual.

---

## Beat 6 · Bonus + close (6:00 – 7:00) — 60 seconds

**What you just saw, measured.** Behind the UI we ran this same engine on 200 simulated sessions — reproducible, seeded, five borrower archetypes. Dynamic engine beats the static baseline by roughly **+17 percentage points** of acceptance, with **zero guardrail breaches** across all 200. And the objection classifier clears the PS#8 **75% accuracy bar** on a 42-utterance annotated fixture. The numbers are queryable live from the app if you want to inspect them after the session.

**Bonus challenge.** The PS#8 bonus asks for a Stable Baselines3 RL agent benchmarked against the rule-based engine. We have the simulator plumbed for it — reward = signed settlement value, action space is exactly our five strategies, and the sim runs reproducibly with a fixed seed. Training the agent and dropping the rollouts into the same comparison view is a one-week extension, not a re-architecture.

*(Close.)*

To recap — every hard acceptance criterion passes:

- ✅ 100% guardrail compliance, zero breaches across 200 automated sessions
- ✅ Objection-type accuracy above 75% on the annotated fixture
- ✅ Five distinct strategies with different observable behaviours
- ✅ Dynamic beats static by ~17 points on simulated acceptance
- ✅ Three live sessions just run, judge can play borrower next

**The product isn't "Claude negotiates." The product is: every borrower signal that enters this system produces a traceable, guardrail-safe offer — and the offer is always the best one the lender's policy allows for that specific signal.**

Thank you. Happy to take questions.

---

## Q&A cheat sheet — top 10 judge questions, one-line answers

1. **"Is this real NLP or just keywords?"** — Deterministic weighted-keyword classifier, picked for explainability and reproducibility. Same input → same label, every time. No LLM drift in the decisioning layer.

2. **"Can you push it below the floor?"** — Try it. The clamp is non-bypassable — it's not a check that can be skipped, it's a `Math.min(Math.max(...))` on the last line before the DTO is returned. 200 simulated sessions confirm it.

3. **"What if the model says the wrong label?"** — Falls back to NONE, strategy picker defaults to HOLD, guardrail still applies. Worst case we say nothing wrong, not something wrong.

4. **"Why rule-based strategy picker, not an LLM?"** — Explainability and determinism. The judge can read the exact reason every strategy was chosen in the `rationale` field. An LLM would trade that for marginal decision quality — wrong trade for a collections product that must be audited.

5. **"How do lenders configure their floors?"** — `NegotiationPolicy` table, keyed on lender + product + DPD bucket. Priority field handles overlaps — more specific rules win. Four lender profiles seeded with 18 policies covering the common grid.

6. **"What about secured loans like home loans?"** — Blocked at the settlement-save level. `SettlementService` guard rejects HOME_LOAN, AUTO_LOAN, GOLD_LOAN, MORTGAGE, and LAP because the brief explicitly scopes unsecured only.

7. **"How do you handle multi-account customers?"** — Portfolio-scope transcript. When the strategy picker sees multiple open accounts at the same lender, BUNDLE becomes eligible. The join table `settlement_accounts` links every account the bundle covers.

8. **"What if the borrower responds after we've escalated?"** — ESCALATE flips `shouldOfferSettlement=false` and disables the save button, but the transcript stays open. A senior agent can pick it up and override; the override chip lights up in the audit trail.

9. **"How scalable is this?"** — Stateless decisioning — every call is `(utterance, account) → decision`. Horizontally scales with Spring. The only persistence is transcript + settlement; we could move H2 to Postgres in one properties change.

10. **"What would you build in week two?"** — RL agent for the bonus challenge, voice-tone integration (the brief scopes it out for v1), and multi-language classifier support. The architecture already isolates the NLP layer behind a clean interface, so swapping it is a one-file change.

---

## Delivery tips

- **Don't read this script.** Internalise the beats and improvise the connective tissue.
- **Slow down on the numbers.** 17 points, 75%, 0 breaches, 200 sessions — these should land like hammer blows, not run-on bullets.
- **Point, don't hover.** Use a single finger or pointer — show the chip that just turned red, show the strategy pill, show the guardrail badge.
- **If something breaks on stage**, fall back to the scenario-matrix SVG and narrate row-by-row — every strategy is defensible even without the live UI cooperating.
- **Don't apologise for the stack.** Java and rule-based systems are the right choice for a regulated collections product. Own it.
- **End on the signal → offer line.** That's the single sentence a judge will remember when they score you.
