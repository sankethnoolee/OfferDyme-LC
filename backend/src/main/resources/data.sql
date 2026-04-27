-- ============================================================
-- Seed data: multiple lenders, field agents per lender,
-- customers who each have accounts across multiple lenders,
-- sample transcripts, and per-lender negotiation policies.
-- ============================================================

INSERT INTO lender (id, name, code, contact_email, contact_phone, address) VALUES
  (1, 'LC Bank',   'LC',   'collections@lcbank.com',   '+91-22-6160-6161', 'LC Bank House, Lower Parel, Mumbai 400013'),
  (2, 'HDFC Bank', 'HDFC', 'collections@hdfcbank.com', '+91-22-6652-1000', 'HDFC House, Senapati Bapat Marg, Mumbai 400013'),
  (3, 'ICICI Bank','ICICI','collections@icicibank.com','+91-22-2653-1414', 'ICICI Tower, Bandra-Kurla Complex, Mumbai 400051'),
  (4, 'Kotak Bank','KOTAK','collections@kotak.com',    '+91-22-6166-0000', 'Kotak Infiniti, Goregaon (E), Mumbai 400097');

INSERT INTO field_agent (id, name, employee_code, email, phone, region, lender_id) VALUES
  (1, 'Rahul Mehta',    'LC-FA-001',    'rahul.mehta@lcbank.com',    '+91-98200-11001', 'Mumbai West',  1),
  (2, 'Priya Sharma',   'LC-FA-002',    'priya.sharma@lcbank.com',   '+91-98200-11002', 'Mumbai South', 1),
  (3, 'Amit Kulkarni',  'HDFC-FA-003',  'amit.kulkarni@hdfcbank.com','+91-98200-11003', 'Pune',         2),
  (4, 'Sneha Iyer',     'HDFC-FA-004',  'sneha.iyer@hdfcbank.com',   '+91-98200-11004', 'Bangalore',    2),
  (5, 'Karan Malhotra', 'ICICI-FA-005', 'karan.m@icicibank.com',     '+91-98200-11005', 'Delhi',        3),
  (6, 'Divya Nair',     'KOTAK-FA-006', 'divya.nair@kotak.com',      '+91-98200-11006', 'Mumbai East',  4);

INSERT INTO customer (id, full_name, pan_number, aadhaar_masked, email, phone, address, city, state, pincode, date_of_birth, annual_income) VALUES
  (1, 'Vikram Singh',    'ABCPS1234F', 'XXXX-XXXX-4521', 'vikram.singh@gmail.com',    '+91-98765-43210', '12 Hiranandani Gardens', 'Mumbai',    'Maharashtra', '400076', '1985-04-12',  1450000),
  (2, 'Anjali Deshmukh', 'BXCPD5678G', 'XXXX-XXXX-7891', 'anjali.desh@yahoo.in',      '+91-98765-43211', '45 Koregaon Park',        'Pune',      'Maharashtra', '411001', '1990-09-22',   980000),
  (3, 'Rakesh Gupta',    'CXQPG9012H', 'XXXX-XXXX-3344', 'rakesh.gupta@outlook.com',  '+91-98765-43212', '88 MG Road',              'Bangalore', 'Karnataka',   '560001', '1978-02-03',  2100000),
  (4, 'Meera Nair',      'DXRPN3456I', 'XXXX-XXXX-5566', 'meera.nair@gmail.com',      '+91-98765-43213', '22 Juhu Tara Road',       'Mumbai',    'Maharashtra', '400049', '1988-11-30',  1200000),
  (5, 'Arjun Patel',     'EXSPP7890J', 'XXXX-XXXX-7788', 'arjun.patel@gmail.com',     '+91-98765-43214', '7 Satellite Area',        'Ahmedabad', 'Gujarat',     '380015', '1982-07-18',   850000);

-- Accounts — each account now has a LENDER, so one customer can have loans across
-- LC Bank, HDFC, ICICI, Kotak. The BUNDLE strategy only bundles accounts that
-- share the SAME lender.
INSERT INTO account (id, account_number, product_type, sanctioned_amount, outstanding_amount, principal_outstanding, interest_outstanding, penalty_amount, dpd_bucket, days_past_due, last_payment_date, last_payment_amount, sanction_date, interest_rate, status, customer_id, lender_id, assigned_agent_id) VALUES
  -- Vikram Singh — 2 LC Bank + 1 HDFC + 1 ICICI
  (1,  'LC-PL-10001',    'PERSONAL_LOAN', 500000,  312400, 280000, 25400, 7000,  'DPD_60',  68, '2026-02-15', 12000, '2023-05-10', 14.5, 'DELINQUENT', 1, 1, 1),
  (2,  'LC-CC-10002',    'CREDIT_CARD',   200000,  148200, 120000, 22200, 6000,  'DPD_90',  95, '2026-01-20',  5000, '2022-03-18', 36.0, 'DELINQUENT', 1, 1, 1),
  (3,  'HDFC-HL-10003',  'HOME_LOAN',    4500000, 4210000, 4050000, 140000, 20000, 'DPD_30', 32, '2026-03-28', 38000, '2020-06-01',  9.2, 'DELINQUENT', 1, 2, 3),
  (11, 'ICICI-PL-10011', 'PERSONAL_LOAN', 300000,  212500, 190000, 17500, 5000,  'DPD_60',  61, '2026-02-20',  9000, '2023-06-14', 14.0, 'DELINQUENT', 1, 3, 5),

  -- Anjali Deshmukh — 1 LC + 1 HDFC
  (4,  'LC-PL-10004',    'PERSONAL_LOAN', 300000,  195600, 175000, 16600, 4000,  'DPD_90',  112, '2026-01-05',  8000, '2023-09-12', 15.0, 'DELINQUENT', 2, 1, 2),
  (5,  'HDFC-AL-10005',  'AUTO_LOAN',     800000,  542100, 510000, 27100, 5000,  'DPD_60',  61, '2026-02-22', 16500, '2022-11-20', 10.5, 'DELINQUENT', 2, 2, 3),

  -- Rakesh Gupta — 2 HDFC + 1 Kotak
  (6,  'HDFC-CC-10006',  'CREDIT_CARD',   500000,  412000, 350000, 52000, 10000, 'NPA',    185, '2025-10-18', 10000, '2021-04-05', 36.0, 'DELINQUENT', 3, 2, 4),
  (7,  'HDFC-PL-10007',  'PERSONAL_LOAN', 700000,  515300, 480000, 28300, 7000,  'DPD_90',  98, '2026-01-12', 18000, '2023-01-15', 13.8, 'DELINQUENT', 3, 2, 4),
  (12, 'KOTAK-CC-10012', 'CREDIT_CARD',   350000,  268400, 220000, 38400, 10000, 'DPD_90',  101, '2026-01-10',  7000, '2022-02-20', 36.0, 'DELINQUENT', 3, 4, 6),

  -- Meera Nair — 1 LC + 1 ICICI
  (8,  'LC-HL-10008',    'HOME_LOAN',    3200000, 2910000, 2810000, 92000, 8000,  'DPD_30',  29, '2026-03-25', 28000, '2019-08-22',  8.9, 'DELINQUENT', 4, 1, 2),
  (9,  'ICICI-CC-10009', 'CREDIT_CARD',   150000,  102800,  85000, 14800, 3000,  'DPD_60',  72, '2026-02-10',  4000, '2022-07-01', 36.0, 'DELINQUENT', 4, 3, 5),

  -- Arjun Patel — 1 LC (only)
  (10, 'LC-PL-10010',    'PERSONAL_LOAN', 450000,  278400, 252000, 20400, 6000,  'DPD_60',  64, '2026-02-18', 11000, '2023-03-22', 14.0, 'DELINQUENT', 5, 1, 1);

-- A sample transcript with messages for account 1 (Vikram Singh's personal loan).
INSERT INTO transcript (id, customer_id, account_id, field_agent_id, channel, started_at, ended_at, sentiment, sentiment_score, summary, status, portfolio_level) VALUES
  (1, 1, 1, 1, 'CHAT', TIMESTAMP '2026-04-20 10:15:00', TIMESTAMP '2026-04-20 10:29:00', 'NEGATIVE', -0.35,
   'Customer expressed frustration over multiple calls, cited job loss 3 months ago, asking for EMI reduction and more time. Willing to consider one-time settlement at heavy discount.',
   'OPEN', FALSE);

INSERT INTO chat_message (id, transcript_id, sender_type, sender_name, content, sentiment, sentiment_score, created_at) VALUES
  (1, 1, 'AGENT',    'Rahul Mehta',   'Good morning Mr. Singh, this is Rahul from LC Bank regarding your personal loan LC-PL-10001. Your account is 68 days past due with an outstanding of INR 3,12,400. Can we discuss a way to regularise it?', 'NEUTRAL',   0.0,  TIMESTAMP '2026-04-20 10:15:00'),
  (2, 1, 'CUSTOMER', 'Vikram Singh',  'Look, I keep getting calls every week. I lost my job three months ago and I just got a new one last week. I cannot pay the full EMI right now.',                                                                           'NEGATIVE', -0.55, TIMESTAMP '2026-04-20 10:18:00'),
  (3, 1, 'AGENT',    'Rahul Mehta',   'I understand, and I am sorry to hear about the job loss. Given the circumstances, can we look at a settlement option or a restructured EMI plan that suits your current cashflow?',                                   'POSITIVE',  0.35, TIMESTAMP '2026-04-20 10:21:00'),
  (4, 1, 'CUSTOMER', 'Vikram Singh',  'If you can reduce the outstanding by 30-35 percent, I can try to arrange a lump sum within 45 days. Otherwise I will need at least 6 months of reduced EMIs.',                                                         'NEUTRAL',   0.05, TIMESTAMP '2026-04-20 10:25:00'),
  (5, 1, 'AGENT',    'Rahul Mehta',   'Understood, let me check with my supervisor on the exact discount we can offer and come back to you by EOD today.',                                                                                                      'POSITIVE',  0.30, TIMESTAMP '2026-04-20 10:28:00');

INSERT INTO transcript_history (id, transcript_id, full_text, sentiment, sentiment_score, summary, change_reason, captured_at) VALUES
  (1, 1,
   'Agent: Good morning Mr. Singh... | Customer: I lost my job three months ago... | Agent: Settlement or restructured EMI? | Customer: 30-35% discount for lump sum in 45 days, or 6 months reduced EMI. | Agent: Will confirm by EOD.',
   'NEGATIVE', -0.35,
   'Customer open to settlement at 30-35% discount or restructured EMI plan over 6 months.',
   'CONVERSATION_CLOSED',
   TIMESTAMP '2026-04-20 10:29:00');

-- A second sample transcript on account 4 (Anjali - personal loan).
INSERT INTO transcript (id, customer_id, account_id, field_agent_id, channel, started_at, ended_at, sentiment, sentiment_score, summary, status, portfolio_level) VALUES
  (2, 2, 4, 3, 'CHAT', TIMESTAMP '2026-04-22 15:00:00', NULL, 'NEUTRAL', 0.05,
   'Initial contact on 112 DPD personal loan. Customer acknowledges debt, trying to assess repayment options.',
   'OPEN', FALSE);

INSERT INTO chat_message (id, transcript_id, sender_type, sender_name, content, sentiment, sentiment_score, created_at) VALUES
  (6, 2, 'AGENT',    'Amit Kulkarni',   'Hello Ms. Deshmukh, Amit from LC Bank. Your personal loan LC-PL-10004 is 112 days overdue with INR 1,95,600 outstanding. We would like to help you close this out — what options work for you?', 'POSITIVE', 0.2,  TIMESTAMP '2026-04-22 15:00:00'),
  (7, 2, 'CUSTOMER', 'Anjali Deshmukh', 'Hi Amit, yes I know I am behind. My small business had a rough quarter. Can you tell me what settlement options are available?',                                                                        'NEUTRAL',  0.0,  TIMESTAMP '2026-04-22 15:04:00');

-- ============================================================
-- Negotiation policy — every lender has its own guardrails.
-- "Most specific wins": rows with product + dpd set outrank rows with
-- only one, which outrank the per-lender default.
-- ============================================================
INSERT INTO negotiation_policy
  (id, lender_id, product_type, dpd_bucket,
   discount_floor_pct, discount_ceiling_pct,
   offer_floor_pct_of_outstanding, offer_ceiling_pct_of_outstanding,
   min_installments, max_installments,
   priority, active, notes) VALUES
  -- --- LC Bank ------------------------------------------------
  (1, 1, NULL, NULL,          0.0,  25.0, 75.0, 100.0, 1, 6,  0, TRUE, 'LC Bank default negotiation room.'),
  (2, 1, NULL, 'DPD_30',      0.0,  10.0, 90.0, 100.0, 1, 3,  1, TRUE, 'LC Bank — early DPD.'),
  (3, 1, NULL, 'DPD_60',      5.0,  20.0, 80.0,  95.0, 1, 6,  1, TRUE, 'LC Bank — moderate DPD.'),
  (4, 1, NULL, 'DPD_90',     15.0,  35.0, 65.0,  85.0, 1, 12, 1, TRUE, 'LC Bank — heavy DPD.'),
  (5, 1, NULL, 'NPA',        25.0,  50.0, 50.0,  75.0, 1, 12, 1, TRUE, 'LC Bank — NPA.'),
  (6, 1, 'CREDIT_CARD', 'DPD_90', 25.0,  45.0, 55.0, 75.0, 1, 12, 2, TRUE, 'LC Bank credit card 90 DPD.'),
  (7, 1, 'CREDIT_CARD', 'NPA',    30.0,  55.0, 45.0, 70.0, 1, 12, 2, TRUE, 'LC Bank credit card NPA.'),
  (8, 1, 'HOME_LOAN', NULL,       0.0,  10.0, 90.0, 100.0, 1, 6,  2, TRUE, 'LC Bank home loan — minimal discount.'),

  -- --- HDFC Bank (slightly tighter than LC) ------------------
  (20, 2, NULL, NULL,          0.0,  20.0, 80.0, 100.0, 1, 6,  0, TRUE, 'HDFC default.'),
  (21, 2, NULL, 'DPD_60',      5.0,  15.0, 85.0,  95.0, 1, 6,  1, TRUE, 'HDFC moderate DPD.'),
  (22, 2, NULL, 'DPD_90',     10.0,  30.0, 70.0,  90.0, 1, 9,  1, TRUE, 'HDFC heavy DPD.'),
  (23, 2, NULL, 'NPA',        20.0,  45.0, 55.0,  80.0, 1, 12, 1, TRUE, 'HDFC NPA.'),
  (24, 2, 'CREDIT_CARD', 'NPA',25.0,  50.0, 50.0, 75.0, 1, 12, 2, TRUE, 'HDFC credit card NPA.'),

  -- --- ICICI Bank (most aggressive — willing to stretch further) ------
  (30, 3, NULL, NULL,          0.0,  30.0, 70.0, 100.0, 1, 6,  0, TRUE, 'ICICI default.'),
  (31, 3, NULL, 'DPD_60',     10.0,  25.0, 75.0,  95.0, 1, 6,  1, TRUE, 'ICICI moderate DPD.'),
  (32, 3, NULL, 'DPD_90',     20.0,  40.0, 60.0,  85.0, 1, 12, 1, TRUE, 'ICICI heavy DPD.'),
  (33, 3, NULL, 'NPA',        30.0,  55.0, 45.0,  75.0, 1, 12, 1, TRUE, 'ICICI NPA.'),

  -- --- Kotak Bank (conservative, home-loan heavy house) -------
  (40, 4, NULL, NULL,          0.0,  18.0, 82.0, 100.0, 1, 6,  0, TRUE, 'Kotak default.'),
  (41, 4, NULL, 'DPD_90',     10.0,  25.0, 75.0,  90.0, 1, 9,  1, TRUE, 'Kotak heavy DPD.'),
  (42, 4, 'CREDIT_CARD', 'DPD_90', 20.0, 40.0, 60.0, 80.0, 2, 12, 2, TRUE, 'Kotak credit card 90 DPD.');

-- ============================================================
-- Negotiation strategies — one set per lender.
-- Claude picks exactly ONE per turn. BUNDLE bundles all accounts of the
-- customer at the SAME lender into a single settlement offer.
-- ============================================================
INSERT INTO negotiation_strategy
  (id, lender_id, code, name, when_applied, action_template, priority, active) VALUES
  -- --- LC Bank strategies ---
  (1, 1, 'HOLD', 'Hold the offer',
   'Customer is wavering, pushing back mildly, or stalling — sentiment slightly negative to neutral. The current offer is still defensible.',
   'Keep the recommended offer/discount at the same point as the previous turn. Reinforce urgency (time-bound waiver, supervisor approval) without dropping price.',
   1, TRUE),
  (2, 1, 'LOWER', 'Lower the offer',
   'Customer has a strong, specific affordability objection (job loss, medical event, income drop) and has rejected the current offer.',
   'Move the recommended offer 5-10% lower toward the policy floor (never below it). Present the new number as final, management-approved, tied to quick closure.',
   2, TRUE),
  (3, 1, 'REFRAME_INSTALLMENTS', 'Reframe as installments',
   'Customer rejects the lump sum specifically — not the total amount. They have cash flow but not a single large cheque.',
   'Keep the total offer at the recommended amount but split into 2-3 installments within policy installment limits.',
   3, TRUE),
  (4, 1, 'BUNDLE', 'Bundle accounts',
   'Customer has multiple accounts at the same lender and objects to the per-account offer. Use when portfolio context is available.',
   'Package all of the customer''s accounts at this lender into a single settlement conversation. Offer a blended discount across the portfolio, contingent on closing ALL accounts together.',
   4, TRUE),
  (5, 1, 'ESCALATE', 'Escalate',
   'Customer is disputing the debt, refusing to engage, or repeatedly rejecting every offer at the policy floor. Further price movement is not defensible.',
   'Hand off to a senior collections officer or legal review. Do NOT make any fresh offer on this turn.',
   5, TRUE),

  -- --- HDFC strategies (same code set, same semantics) ---
  (11, 2, 'HOLD',     'Hold the offer',            'Customer wavering, offer defensible.', 'Keep offer, reinforce urgency.', 1, TRUE),
  (12, 2, 'LOWER',    'Lower the offer',           'Strong affordability objection.',      'Move 5-10% lower toward floor.', 2, TRUE),
  (13, 2, 'REFRAME_INSTALLMENTS', 'Installments',  'Rejects lump sum.',                    'Split into 2-3 installments.',   3, TRUE),
  (14, 2, 'BUNDLE',   'Bundle HDFC accounts',      'Customer has multiple HDFC accounts.', 'Bundle all HDFC accounts.',      4, TRUE),
  (15, 2, 'ESCALATE', 'Escalate to senior',        'Repeated rejections at floor, or dispute.', 'Hand off — no fresh offer.',  5, TRUE),

  -- --- ICICI strategies ---
  (21, 3, 'HOLD',     'Hold the offer', 'Customer wavering.',                 'Keep offer, add urgency.',    1, TRUE),
  (22, 3, 'LOWER',    'Lower the offer','Strong affordability.',              'Move 5-10% lower.',           2, TRUE),
  (23, 3, 'REFRAME_INSTALLMENTS', 'Installments','Rejects lump sum.',         'Split into installments.',    3, TRUE),
  (24, 3, 'BUNDLE',   'Bundle ICICI accounts','Multiple ICICI accounts.',     'Bundle all ICICI accounts.',  4, TRUE),
  (25, 3, 'ESCALATE', 'Escalate to senior', 'Repeated rejections or dispute.', 'Hand off — no fresh offer.', 5, TRUE),

  -- --- Kotak strategies ---
  (31, 4, 'HOLD',     'Hold the offer', 'Customer wavering.',                 'Keep offer.',                 1, TRUE),
  (32, 4, 'LOWER',    'Lower the offer','Strong affordability.',              'Move 5-10% lower.',           2, TRUE),
  (33, 4, 'REFRAME_INSTALLMENTS', 'Installments','Rejects lump sum.',         'Split into installments.',    3, TRUE),
  (34, 4, 'BUNDLE',   'Bundle Kotak accounts','Multiple Kotak accounts.',     'Bundle all Kotak accounts.',  4, TRUE),
  (35, 4, 'ESCALATE', 'Escalate to senior', 'Repeated rejections or dispute.', 'Hand off — no fresh offer.', 5, TRUE);




--INSERT INTO negotiation_strategy
--  (id, lender_id, code, name, when_applied, action_template, priority, active) VALUES
--  -- --- LC Bank strategies ---
--  (1, 1, 'HOLD', 'Hold the offer',
--   'Customer is wavering, pushing back mildly, or stalling — sentiment slightly negative to neutral. The current offer is still defensible.',
--   'Keep the recommended offer/discount at the same point as the previous turn. Reinforce urgency (time-bound waiver, supervisor approval) without dropping price.',
--   1, TRUE),
--  (2, 1, 'LOWER', 'Lower the offer',
--   'Customer has a strong, specific affordability objection (job loss, medical event, income drop) and has rejected the current offer.',
--   'Move the recommended offer 5-10% lower toward the policy floor (never below it). Present the new number as final, management-approved, tied to quick closure.',
--   2, TRUE),
--  (3, 1, 'REFRAME_INSTALLMENTS', 'Reframe as installments',
--   'Customer rejects the lump sum specifically — not the total amount. They have cash flow but not a single large cheque.',
--   'Keep the total offer at the recommended amount but split into 2-3 installments within policy installment limits.',
--   3, TRUE),
--  (4, 1, 'BUNDLE', 'Bundle accounts',
--   'Customer has multiple accounts at the same lender and objects to the per-account offer. Use when portfolio context is available.',
--   'Package all of the customer''s accounts at this lender into a single settlement conversation. Offer a blended discount across the portfolio, contingent on closing ALL accounts together.',
--   4, TRUE),
--
--  -- --- HDFC strategies (same code set, same semantics) ---
--  (11, 2, 'HOLD',  'Hold the offer',            'Customer wavering, offer defensible.', 'Keep offer, reinforce urgency.', 1, TRUE),
--  (12, 2, 'LOWER', 'Lower the offer',           'Strong affordability objection.',      'Move 5-10% lower toward floor.', 2, TRUE),
--  (13, 2, 'REFRAME_INSTALLMENTS', 'Installments','Rejects lump sum.',                    'Split into 2-3 installments.',   3, TRUE),
--  (14, 2, 'BUNDLE','Bundle HDFC accounts',      'Customer has multiple HDFC accounts.', 'Bundle all HDFC accounts.',      4, TRUE),
--
--  -- --- ICICI strategies ---
--  (21, 3, 'HOLD',  'Hold the offer', 'Customer wavering.',            'Keep offer, add urgency.',    1, TRUE),
--  (22, 3, 'LOWER', 'Lower the offer','Strong affordability.',         'Move 5-10% lower.',           2, TRUE),
--  (23, 3, 'REFRAME_INSTALLMENTS', 'Installments','Rejects lump sum.', 'Split into installments.',    3, TRUE),
--  (24, 3, 'BUNDLE','Bundle ICICI accounts','Multiple ICICI accounts.','Bundle all ICICI accounts.',  4, TRUE),
--
--  -- --- Kotak strategies ---
--  (31, 4, 'HOLD',  'Hold the offer', 'Customer wavering.',             'Keep offer.',                 1, TRUE),
--  (32, 4, 'LOWER', 'Lower the offer','Strong affordability.',          'Move 5-10% lower.',           2, TRUE),
--  (33, 4, 'REFRAME_INSTALLMENTS', 'Installments','Rejects lump sum.',  'Split into installments.',    3, TRUE),
--  (34, 4, 'BUNDLE','Bundle Kotak accounts','Multiple Kotak accounts.', 'Bundle all Kotak accounts.',  4, TRUE);

-- ============================================================
-- Prior settlements — enables PS#8 demo flow ("agent sees 2
-- prior rejected offers") and gives AnalyticsController real
-- numbers to prove "AI_BETTER" vs static agent baseline.
-- Every offered_amount is inside the matching policy's
-- offer_floor / offer_ceiling — guardrail compliance preserved.
-- ============================================================

-- Vikram Singh / LC-PL-10001 (acct 1, DPD_60 LC, outstanding 3,12,400)
--   policy 3 → discount 5-20%, offer floor 2,49,920 / ceiling 2,96,780
INSERT INTO settlement
  (id, customer_id, account_id, transcript_id, proposed_by_agent_id,
   outstanding_at_offer, offered_amount, discount_percent, payment_plan, number_of_installments,
   proposed_payment_date, status, source, strategy_code,
   rationale, customer_response, created_at, decided_at) VALUES
  -- REJECTED #1: static agent opener (too close to ceiling, customer bristled)
  (1, 1, 1, NULL, 1, 312400, 290000,  7.17, 'ONE_TIME', 1, DATE '2026-03-15',
   'REJECTED', 'AGENT', 'HOLD',
   'Static opening offer 7% discount, lump sum in 15 days.',
   'Customer said he lost job three months ago, cannot arrange lump sum.',
   TIMESTAMP '2026-03-10 11:20:00', TIMESTAMP '2026-03-10 11:32:00'),
  -- REJECTED #2: agent tried a second lump-sum run, no installment option
  (2, 1, 1, NULL, 1, 312400, 270000, 13.57, 'ONE_TIME', 1, DATE '2026-04-02',
   'REJECTED', 'AGENT', 'LOWER',
   'Manual 13.5% discount; still one-shot lump sum.',
   'Customer asked for installments; agent declined per lender policy snapshot.',
   TIMESTAMP '2026-03-28 14:05:00', TIMESTAMP '2026-03-28 14:18:00');

-- Anjali Deshmukh / LC-PL-10004 (acct 4, DPD_90 LC, outstanding 1,95,600)
--   policy 4 → discount 15-35%, offer floor 1,27,140 / ceiling 1,66,260
INSERT INTO settlement
  (id, customer_id, account_id, transcript_id, proposed_by_agent_id,
   outstanding_at_offer, offered_amount, discount_percent, payment_plan, number_of_installments,
   proposed_payment_date, status, source, strategy_code,
   rationale, customer_response, created_at, decided_at) VALUES
  -- AI ACCEPTED: dynamic REFRAME_INSTALLMENTS after hardship signal
  (3, 2, 4, NULL, 2, 195600, 155000, 20.75, 'EMI_3', 3, DATE '2026-04-10',
   'ACCEPTED', 'CLAUDE_AI', 'REFRAME_INSTALLMENTS',
   'Customer confirmed small-business cash-flow issue; AI split offer into 3 EMIs of 51,667 keeping total inside DPD_90 floor.',
   'Customer agreed, first EMI paid on 2026-04-10.',
   TIMESTAMP '2026-04-05 16:10:00', TIMESTAMP '2026-04-06 10:02:00');

-- Rakesh Gupta / HDFC-CC-10006 (acct 6, NPA HDFC CC, outstanding 4,12,000)
--   policy 24 → discount 25-50%, offer floor 2,06,000 / ceiling 3,09,000
INSERT INTO settlement
  (id, customer_id, account_id, transcript_id, proposed_by_agent_id,
   outstanding_at_offer, offered_amount, discount_percent, payment_plan, number_of_installments,
   proposed_payment_date, status, source, strategy_code,
   rationale, customer_response, created_at, decided_at) VALUES
  -- AI ACCEPTED: LOWER strategy on NPA credit card, close to floor
  (4, 3, 6, NULL, 4, 412000, 240000, 41.75, 'ONE_TIME', 1, DATE '2026-04-18',
   'ACCEPTED', 'CLAUDE_AI', 'LOWER',
   'AI detected strong AFFORDABILITY objection; moved offer toward floor 50% discount cap was avoided by closing at 41.75%.',
   'Customer accepted, paid full amount.',
   TIMESTAMP '2026-04-12 10:00:00', TIMESTAMP '2026-04-14 15:22:00');

-- Arjun Patel / LC-PL-10010 (acct 10, DPD_60 LC, outstanding 2,78,400)
--   policy 3 → discount 5-20%, offer floor 2,22,720 / ceiling 2,64,480
INSERT INTO settlement
  (id, customer_id, account_id, transcript_id, proposed_by_agent_id,
   outstanding_at_offer, offered_amount, discount_percent, payment_plan, number_of_installments,
   proposed_payment_date, status, source, strategy_code,
   rationale, customer_response, created_at, decided_at) VALUES
  -- AI ACCEPTED: REFRAME_INSTALLMENTS — customer wanted cash flow, not bigger discount
  (5, 5, 10, NULL, 1, 278400, 230000, 17.38, 'EMI_3', 3, DATE '2026-04-20',
   'ACCEPTED', 'CLAUDE_AI', 'REFRAME_INSTALLMENTS',
   'PARTIAL_WILLINGNESS signal; AI kept discount modest but split into 3 EMIs.',
   'Customer accepted installment plan.',
   TIMESTAMP '2026-04-15 13:30:00', TIMESTAMP '2026-04-15 14:40:00');

-- Meera Nair / LC-HL-10008 (acct 8, DPD_30 home loan, outstanding 29,10,000)
--   policy 8 → home loan, discount 0-10%, offer floor 26,19,000 / ceiling 29,10,000
INSERT INTO settlement
  (id, customer_id, account_id, transcript_id, proposed_by_agent_id,
   outstanding_at_offer, offered_amount, discount_percent, payment_plan, number_of_installments,
   proposed_payment_date, status, source, strategy_code,
   rationale, customer_response, created_at, decided_at) VALUES
  -- AGENT REJECTED: static 3% waiver on home loan, customer wanted EMI relief instead
  (6, 4, 8, NULL, 2, 2910000, 2822000, 3.02, 'ONE_TIME', 1, DATE '2026-04-15',
   'REJECTED', 'AGENT', 'HOLD',
   'Standard penalty-waiver opener on home loan; no cash-flow framing.',
   'Customer asked for EMI restructure; agent did not pivot.',
   TIMESTAMP '2026-04-08 11:00:00', TIMESTAMP '2026-04-09 09:15:00');

-- Rakesh Gupta / KOTAK-CC-10012 (acct 12, DPD_90 Kotak CC, outstanding 2,68,400)
--   policy 42 → Kotak CC DPD_90, discount 20-40%, offer floor 1,61,040 / ceiling 2,14,720
INSERT INTO settlement
  (id, customer_id, account_id, transcript_id, proposed_by_agent_id,
   outstanding_at_offer, offered_amount, discount_percent, payment_plan, number_of_installments,
   proposed_payment_date, status, source, strategy_code,
   rationale, customer_response, created_at, decided_at) VALUES
  -- AI REJECTED: BUNDLE attempted too early, customer not ready
  (7, 3, 12, NULL, 6, 268400, 210000, 21.76, 'ONE_TIME', 1, DATE '2026-04-16',
   'REJECTED', 'CLAUDE_AI', 'BUNDLE',
   'AI suggested bundling Kotak CC with HDFC accounts, but customer wanted to handle accounts separately.',
   'Customer declined cross-lender bundle (different bank).',
   TIMESTAMP '2026-04-10 15:20:00', TIMESTAMP '2026-04-11 10:45:00');

-- Anjali Deshmukh / HDFC-AL-10005 (acct 5, DPD_60 HDFC auto, outstanding 5,42,100)
--   policy 21 → HDFC DPD_60, discount 5-15%, offer floor 4,60,785 / ceiling 5,14,995
INSERT INTO settlement
  (id, customer_id, account_id, transcript_id, proposed_by_agent_id,
   outstanding_at_offer, offered_amount, discount_percent, payment_plan, number_of_installments,
   proposed_payment_date, status, source, strategy_code,
   rationale, customer_response, created_at, decided_at) VALUES
  -- AGENT ACCEPTED: lucky, customer had windfall and accepted standard offer
  (8, 2, 5, NULL, 3, 542100, 505000,  6.84, 'ONE_TIME', 1, DATE '2026-04-05',
   'ACCEPTED', 'AGENT', 'HOLD',
   'Standard 7% waiver, ONE_TIME. Offer sat near ceiling — accepted because customer had a lump sum ready.',
   'Customer cleared balance after partner paid bonus.',
   TIMESTAMP '2026-03-30 10:40:00', TIMESTAMP '2026-04-01 11:12:00');

-- Rakesh Gupta / HDFC-PL-10007 (acct 7, DPD_90 HDFC PL, outstanding 5,15,300)
--   policy 22 → HDFC DPD_90, discount 10-30%, offer floor 3,60,710 / ceiling 4,63,770
INSERT INTO settlement
  (id, customer_id, account_id, transcript_id, proposed_by_agent_id,
   outstanding_at_offer, offered_amount, discount_percent, payment_plan, number_of_installments,
   proposed_payment_date, status, source, strategy_code,
   rationale, customer_response, created_at, decided_at) VALUES
  -- AGENT REJECTED: static high offer on heavy DPD, customer walked
  (9, 3, 7, NULL, 4, 515300, 440000, 14.61, 'ONE_TIME', 1, DATE '2026-04-12',
   'REJECTED', 'AGENT', 'HOLD',
   'Manual 15% discount lump sum on 98 DPD account.',
   'Customer already juggling three lenders; wanted installments + deeper discount.',
   TIMESTAMP '2026-04-05 09:30:00', TIMESTAMP '2026-04-06 16:40:00');

-- Summary: AI = 3/4 accepted (75%), AGENT = 1/4 accepted (25%) → AnalyticsController verdict AI_BETTER.

-- Adjust sequences so app-generated IDs continue after seed data.

-- ============================================================
-- EXTENDED SEED DATA: 25 new customers (IDs 6-30)
-- ============================================================
INSERT INTO customer (id, full_name, pan_number, aadhaar_masked, email, phone, address, city, state, pincode, date_of_birth, annual_income) VALUES
  (6,  'Suresh Reddy',          'FXAPR2345K', 'XXXX-XXXX-1122', 'suresh.reddy@gmail.com',       '+91-98765-43215', '14 Banjara Hills Rd',        'Hyderabad',   'Telangana',    '500034', '1983-06-14',  1200000),
  (7,  'Preethi Krishnan',      'GXBPK6789L', 'XXXX-XXXX-3344', 'preethi.k@yahoo.in',           '+91-98765-43216', '7 Adyar Beach Road',          'Chennai',     'Tamil Nadu',   '600020', '1991-03-28',   950000),
  (8,  'Mohit Aggarwal',        'HXCPA1234M', 'XXXX-XXXX-5566', 'mohit.agg@outlook.com',        '+91-98765-43217', '22 Rajouri Garden',           'Delhi',       'Delhi',        '110027', '1979-09-05',  1800000),
  (9,  'Kavita Joshi',          'IXDPJ5678N', 'XXXX-XXXX-7788', 'kavita.joshi@gmail.com',       '+91-98765-43218', '3 Aundh Road',                'Pune',        'Maharashtra',  '411007', '1987-12-19',   750000),
  (10, 'Deepak Pandey',         'JXEPD9012O', 'XXXX-XXXX-9900', 'deepak.pandey@gmail.com',      '+91-98765-43219', '18 Hazratganj',               'Lucknow',     'Uttar Pradesh','226001', '1985-04-30',   620000),
  (11, 'Nisha Choudhary',       'KXFPC3456P', 'XXXX-XXXX-1133', 'nisha.choudhary@rediff.com',   '+91-98765-43220', '55 Malviya Nagar',            'Jaipur',      'Rajasthan',    '302017', '1993-07-22',   880000),
  (12, 'Ravi Shankar Iyer',     'LXGPI7890Q', 'XXXX-XXXX-2244', 'ravi.iyer@gmail.com',          '+91-98765-43221', '41 Indiranagar 100ft Rd',     'Bangalore',   'Karnataka',    '560038', '1976-11-11',  2200000),
  (13, 'Fatima Khan',           'MXHPK2345R', 'XXXX-XXXX-3355', 'fatima.khan@gmail.com',        '+91-98765-43222', '9 Mohammed Ali Road',         'Mumbai',      'Maharashtra',  '400003', '1990-01-08',  1100000),
  (14, 'Arun Pillai',           'NXIPL6789S', 'XXXX-XXXX-4466', 'arun.pillai@yahoo.com',        '+91-98765-43223', '12 Palarivattom',             'Kochi',       'Kerala',       '682025', '1984-05-20',  1350000),
  (15, 'Sunita Verma',          'OXJPV1234T', 'XXXX-XXXX-5577', 'sunita.verma@gmail.com',       '+91-98765-43224', '77 Lajpat Nagar III',         'Delhi',       'Delhi',        '110024', '1988-08-14',   960000),
  (16, 'Rajesh Kumar Singh',    'PXKPS5678U', 'XXXX-XXXX-6688', 'rajesh.ksingh@outlook.com',    '+91-98765-43225', '33 Salt Lake Sector V',       'Kolkata',     'West Bengal',  '700091', '1980-02-27',  1050000),
  (17, 'Pooja Mishra',          'QXLPM9012V', 'XXXX-XXXX-7799', 'pooja.mishra@gmail.com',       '+91-98765-43226', '5 Sigra Colony',              'Varanasi',    'Uttar Pradesh','221002', '1994-10-03',   580000),
  (18, 'Sameer Sheikh',         'RXMPS3456W', 'XXXX-XXXX-8800', 'sameer.sheikh@rediff.com',     '+91-98765-43227', '88 CG Road',                  'Ahmedabad',   'Gujarat',      '380009', '1982-12-15',  1450000),
  (19, 'Lakshmi Subramanian',   'SXNPS7890X', 'XXXX-XXXX-9911', 'lakshmi.sub@gmail.com',        '+91-98765-43228', '14 Anna Nagar West',          'Chennai',     'Tamil Nadu',   '600040', '1977-06-30',  1750000),
  (20, 'Harish Naidu',          'TXOPS2345Y', 'XXXX-XXXX-1023', 'harish.naidu@gmail.com',       '+91-98765-43229', '23 Kondapur Main Rd',         'Hyderabad',   'Telangana',    '500084', '1986-03-17',  1300000),
  (21, 'Dimple Kapoor',         'UXPPK6789Z', 'XXXX-XXXX-2034', 'dimple.kapoor@yahoo.in',       '+91-98765-43230', '66 Linking Road, Bandra',     'Mumbai',      'Maharashtra',  '400050', '1989-09-25',  2400000),
  (22, 'Vivek Tiwari',          'VXQPT1234A', 'XXXX-XXXX-3045', 'vivek.tiwari@gmail.com',       '+91-98765-43231', '12 Arera Colony',             'Bhopal',      'Madhya Pradesh','462016','1981-07-08',   720000),
  (23, 'Anita Saxena',          'WXRPS5678B', 'XXXX-XXXX-4056', 'anita.saxena@gmail.com',       '+91-98765-43232', '4 Taj Nagari Phase 2',        'Agra',        'Uttar Pradesh','282001', '1992-04-12',   650000),
  (24, 'Manish Dubey',          'XXSPM9012C', 'XXXX-XXXX-5067', 'manish.dubey@rediff.com',      '+91-98765-43233', '7 Boring Road Colony',        'Patna',       'Bihar',        '800001', '1983-11-29',   520000),
  (25, 'Shreya Bhatt',          'YXTPB3456D', 'XXXX-XXXX-6078', 'shreya.bhatt@gmail.com',       '+91-98765-43234', '19 Adajan Road',              'Surat',       'Gujarat',      '395009', '1990-08-04',   980000),
  (26, 'Lokesh Gowda',          'ZXUPG7890E', 'XXXX-XXXX-7089', 'lokesh.gowda@gmail.com',       '+91-98765-43235', '8 Saraswathipuram',           'Mysuru',      'Karnataka',    '570009', '1985-01-16',  1100000),
  (27, 'Chitra Balaji',         'AXVPB2345F', 'XXXX-XXXX-8091', 'chitra.balaji@yahoo.in',       '+91-98765-43236', '34 RS Puram',                 'Coimbatore',  'Tamil Nadu',   '641002', '1978-05-23',   830000),
  (28, 'Imran Siddiqui',        'BXWPS6789G', 'XXXX-XXXX-9102', 'imran.siddiqui@gmail.com',     '+91-98765-43237', '21 Dharampeth',               'Nagpur',      'Maharashtra',  '440010', '1987-02-11',   760000),
  (29, 'Reena Mahajan',         'CXXPM1234H', 'XXXX-XXXX-1213', 'reena.mahajan@gmail.com',      '+91-98765-43238', '5 Sector 17',                 'Chandigarh',  'Punjab',       '160017', '1991-07-19',  1200000),
  (30, 'Sandeep Rao',           'DXYPR5678I', 'XXXX-XXXX-2324', 'sandeep.rao@outlook.com',      '+91-98765-43239', '16 Dwaraka Nagar',            'Visakhapatnam','Andhra Pradesh','530016','1984-10-07',   890000);

-- ============================================================
-- 100 new accounts (IDs 13-112) across customers 6-30
-- ~4 accounts per customer across all 4 lenders
-- ============================================================
INSERT INTO account (id, account_number, product_type, sanctioned_amount, outstanding_amount, principal_outstanding, interest_outstanding, penalty_amount, dpd_bucket, days_past_due, last_payment_date, last_payment_amount, sanction_date, interest_rate, status, customer_id, lender_id, assigned_agent_id) VALUES
-- === Customer 6: Suresh Reddy ===
(13,  'ICICI-PL-20013', 'PERSONAL_LOAN', 350000, 224000, 200000, 18000,  6000, 'DPD_60',  67, '2026-02-18', 9500,  '2023-07-10', 14.5, 'DELINQUENT', 6, 3, 5),
(14,  'LC-CC-20014',    'CREDIT_CARD',   150000, 118500,  95000, 18500,  5000, 'DPD_90',  93, '2026-01-15', 4500,  '2022-04-22', 36.0, 'DELINQUENT', 6, 1, 2),
(15,  'HDFC-AL-20015',  'AUTO_LOAN',     600000, 421000, 395000, 21000,  5000, 'DPD_30',  34, '2026-03-22', 17000, '2022-09-15', 10.5, 'DELINQUENT', 6, 2, 3),
(16,  'KOTAK-PL-20016', 'PERSONAL_LOAN', 250000, 172000, 155000, 13000,  4000, 'DPD_60',  71, '2026-02-12', 8000,  '2023-11-08', 15.0, 'DELINQUENT', 6, 4, 6),
-- === Customer 7: Preethi Krishnan ===
(17,  'LC-PL-20017',    'PERSONAL_LOAN', 400000, 318000, 285000, 25000,  8000, 'DPD_90', 105, '2026-01-08', 11000, '2023-03-14', 14.0, 'DELINQUENT', 7, 1, 2),
(18,  'HDFC-PL-20018',  'PERSONAL_LOAN', 280000, 243500, 215000, 21500,  7000, 'NPA',    195, '2025-10-05',  6000, '2022-08-19', 15.5, 'DELINQUENT', 7, 2, 3),
(19,  'ICICI-CC-20019', 'CREDIT_CARD',   100000,  81200,  65000, 13200,  3000, 'DPD_60',  62, '2026-02-20', 3500,  '2021-12-01', 36.0, 'DELINQUENT', 7, 3, 5),
(20,  'KOTAK-AL-20020', 'AUTO_LOAN',     500000, 334000, 310000, 19000,  5000, 'DPD_30',  38, '2026-03-18', 14000, '2023-05-28', 11.0, 'DELINQUENT', 7, 4, 6),
-- === Customer 8: Mohit Aggarwal ===
(21,  'LC-PL-20021',    'PERSONAL_LOAN', 550000, 389000, 352000, 29000,  8000, 'DPD_60',  74, '2026-02-08', 13500, '2023-02-11', 13.8, 'DELINQUENT', 8, 1, 1),
(22,  'HDFC-CC-20022',  'CREDIT_CARD',   300000, 267000, 220000, 37000, 10000, 'DPD_90',  99, '2026-01-10',  9000, '2021-11-15', 36.0, 'DELINQUENT', 8, 2, 4),
(23,  'ICICI-PL-20023', 'PERSONAL_LOAN', 420000, 298000, 268000, 23000,  7000, 'DPD_60',  69, '2026-02-14', 11500, '2023-04-20', 14.5, 'DELINQUENT', 8, 3, 5),
(24,  'KOTAK-CC-20024', 'CREDIT_CARD',   200000, 182000, 148000, 26000,  8000, 'NPA',    210, '2025-09-20',  5500, '2021-06-10', 36.0, 'DELINQUENT', 8, 4, 6),
-- === Customer 9: Kavita Joshi ===
(25,  'LC-PL-20025',    'PERSONAL_LOAN', 200000, 162000, 148000, 11000,  3000, 'DPD_30',  35, '2026-03-20',  7500, '2024-01-15', 14.0, 'DELINQUENT', 9, 1, 2),
(26,  'HDFC-AL-20026',  'AUTO_LOAN',     450000, 318000, 295000, 18000,  5000, 'DPD_60',  66, '2026-02-16', 13000, '2022-10-07', 10.5, 'DELINQUENT', 9, 2, 3),
(27,  'ICICI-PL-20027', 'PERSONAL_LOAN', 300000, 248000, 222000, 20000,  6000, 'DPD_90',  97, '2026-01-14', 10000, '2023-01-22', 15.0, 'DELINQUENT', 9, 3, 5),
(28,  'LC-CC-20028',    'CREDIT_CARD',   120000, 105000,  84000, 16000,  5000, 'NPA',    198, '2025-10-01',  3000, '2021-09-30', 36.0, 'DELINQUENT', 9, 1, 1),
-- === Customer 10: Deepak Pandey ===
(29,  'LC-PL-20029',    'PERSONAL_LOAN', 250000, 204500, 184000, 15500,  5000, 'DPD_90', 112, '2026-01-05',  8500, '2023-06-18', 14.5, 'DELINQUENT',10, 1, 1),
(30,  'HDFC-PL-20030',  'PERSONAL_LOAN', 320000, 235000, 210000, 20000,  5000, 'DPD_60',  78, '2026-02-05', 10500, '2023-08-14', 15.0, 'DELINQUENT',10, 2, 4),
(31,  'KOTAK-CC-20031', 'CREDIT_CARD',    80000,  64000,  51000, 10000,  3000, 'DPD_30',  40, '2026-03-15',  2500, '2022-07-22', 36.0, 'DELINQUENT',10, 4, 6),
(32,  'ICICI-AL-20032', 'AUTO_LOAN',     350000, 243000, 222000, 16000,  5000, 'DPD_60',  63, '2026-02-22', 11000, '2023-03-09', 11.0, 'DELINQUENT',10, 3, 5),
-- === Customer 11: Nisha Choudhary ===
(33,  'LC-PL-20033',    'PERSONAL_LOAN', 220000, 198000, 175000, 16000,  7000, 'NPA',    220, '2025-09-10',  5000, '2022-01-25', 16.0, 'DELINQUENT',11, 1, 2),
(34,  'HDFC-CC-20034',  'CREDIT_CARD',   180000, 158000, 128000, 22000,  8000, 'DPD_90', 108, '2026-01-02',  5500, '2021-10-14', 36.0, 'DELINQUENT',11, 2, 3),
(35,  'ICICI-PL-20035', 'PERSONAL_LOAN', 260000, 197000, 176000, 16000,  5000, 'DPD_60',  75, '2026-02-09', 9000,  '2023-09-03', 14.5, 'DELINQUENT',11, 3, 5),
(36,  'KOTAK-AL-20036', 'AUTO_LOAN',     400000, 274000, 255000, 15000,  4000, 'DPD_30',  33, '2026-03-23', 12500, '2023-06-17', 10.0, 'DELINQUENT',11, 4, 6),
-- === Customer 12: Ravi Shankar Iyer ===
(37,  'LC-HL-20037',    'HOME_LOAN',    4200000,3780000,3640000,118000, 22000, 'DPD_30',  31, '2026-03-26', 36000, '2019-11-02',  9.0, 'DELINQUENT',12, 1, 1),
(38,  'HDFC-PL-20038',  'PERSONAL_LOAN', 600000, 468000, 422000, 36000, 10000, 'DPD_60',  82, '2026-02-06', 16000, '2023-01-08', 13.5, 'DELINQUENT',12, 2, 4),
(39,  'ICICI-CC-20039', 'CREDIT_CARD',   400000, 368000, 302000, 52000, 14000, 'DPD_90', 103, '2026-01-08', 12000, '2021-05-19', 36.0, 'DELINQUENT',12, 3, 5),
(40,  'KOTAK-PL-20040', 'PERSONAL_LOAN', 500000, 448000, 402000, 36000, 10000, 'NPA',    245, '2025-08-18', 13000, '2022-04-11', 15.0, 'DELINQUENT',12, 4, 6),
-- === Customer 13: Fatima Khan ===
(41,  'LC-PL-20041',    'PERSONAL_LOAN', 280000, 210000, 188000, 17000,  5000, 'DPD_60',  70, '2026-02-12', 9000,  '2023-10-14', 14.0, 'DELINQUENT',13, 1, 2),
(42,  'HDFC-AL-20042',  'AUTO_LOAN',     520000, 368000, 342000, 21000,  5000, 'DPD_30',  36, '2026-03-21', 15000, '2023-04-05', 10.5, 'DELINQUENT',13, 2, 3),
(43,  'ICICI-PL-20043', 'PERSONAL_LOAN', 310000, 237000, 213000, 19000,  5000, 'DPD_60',  64, '2026-02-17', 10000, '2023-07-23', 14.5, 'DELINQUENT',13, 3, 5),
(44,  'KOTAK-CC-20044', 'CREDIT_CARD',   160000, 143000, 115000, 21000,  7000, 'DPD_90', 116, '2025-12-28',  4500, '2021-08-30', 36.0, 'DELINQUENT',13, 4, 6),
-- === Customer 14: Arun Pillai ===
(45,  'LC-PL-20045',    'PERSONAL_LOAN', 380000, 302000, 272000, 24000,  6000, 'DPD_90', 125, '2025-12-22', 10500, '2023-02-20', 14.0, 'DELINQUENT',14, 1, 1),
(46,  'HDFC-CC-20046',  'CREDIT_CARD',   250000, 228000, 185000, 33000, 10000, 'NPA',    200, '2025-10-03',  6500, '2021-07-15', 36.0, 'DELINQUENT',14, 2, 3),
(47,  'ICICI-AL-20047', 'AUTO_LOAN',     700000, 504000, 472000, 27000,  5000, 'DPD_60',  68, '2026-02-14', 18000, '2022-12-10', 11.5, 'DELINQUENT',14, 3, 5),
(48,  'KOTAK-PL-20048', 'PERSONAL_LOAN', 300000, 228000, 205000, 18000,  5000, 'DPD_30',  37, '2026-03-19',  9500, '2023-11-15', 15.0, 'DELINQUENT',14, 4, 6),
-- === Customer 15: Sunita Verma ===
(49,  'LC-PL-20049',    'PERSONAL_LOAN', 340000, 268000, 241000, 22000,  5000, 'DPD_60',  76, '2026-02-08', 10000, '2023-05-18', 14.5, 'DELINQUENT',15, 1, 2),
(50,  'HDFC-PL-20050',  'PERSONAL_LOAN', 420000, 362000, 326000, 28000,  8000, 'DPD_90', 118, '2025-12-24', 12500, '2022-11-25', 15.0, 'DELINQUENT',15, 2, 4),
(51,  'ICICI-CC-20051', 'CREDIT_CARD',   130000, 105000,  84000, 16000,  5000, 'DPD_30',  39, '2026-03-17',  3500, '2022-06-12', 36.0, 'DELINQUENT',15, 3, 5),
(52,  'KOTAK-AL-20052', 'AUTO_LOAN',     480000, 336000, 312000, 19000,  5000, 'DPD_60',  72, '2026-02-11', 13500, '2022-10-28', 11.0, 'DELINQUENT',15, 4, 6),
-- === Customer 16: Rajesh Kumar Singh ===
(53,  'LC-CC-20053',    'CREDIT_CARD',   200000, 178000, 143000, 26000,  9000, 'DPD_90', 107, '2026-01-03',  5500, '2021-09-22', 36.0, 'DELINQUENT',16, 1, 1),
(54,  'HDFC-PL-20054',  'PERSONAL_LOAN', 460000, 414000, 372000, 32000, 10000, 'NPA',    188, '2025-10-14', 12000, '2022-03-07', 15.0, 'DELINQUENT',16, 2, 4),
(55,  'ICICI-PL-20055', 'PERSONAL_LOAN', 330000, 252000, 226000, 21000,  5000, 'DPD_60',  80, '2026-02-04', 10500, '2023-08-19', 14.0, 'DELINQUENT',16, 3, 5),
(56,  'KOTAK-CC-20056', 'CREDIT_CARD',   140000, 115000,  92000, 17000,  6000, 'DPD_30',  41, '2026-03-14',  4000, '2022-11-03', 36.0, 'DELINQUENT',16, 4, 6),
-- === Customer 17: Pooja Mishra ===
(57,  'LC-PL-20057',    'PERSONAL_LOAN', 180000, 147000, 133000, 11000,  3000, 'DPD_30',  32, '2026-03-24',  6500, '2024-02-05', 15.0, 'DELINQUENT',17, 1, 2),
(58,  'HDFC-AL-20058',  'AUTO_LOAN',     400000, 283000, 263000, 15000,  5000, 'DPD_60',  65, '2026-02-18', 12000, '2023-02-28', 10.0, 'DELINQUENT',17, 2, 3),
(59,  'ICICI-PL-20059', 'PERSONAL_LOAN', 240000, 200000, 180000, 16000,  4000, 'DPD_90', 132, '2025-12-10',  8000, '2023-03-15', 15.5, 'DELINQUENT',17, 3, 5),
(60,  'KOTAK-PL-20060', 'PERSONAL_LOAN', 200000, 182000, 163000, 14000,  5000, 'NPA',    212, '2025-09-18',  5000, '2022-05-22', 16.0, 'DELINQUENT',17, 4, 6),
-- === Customer 18: Sameer Sheikh ===
(61,  'LC-PL-20061',    'PERSONAL_LOAN', 480000, 359000, 322000, 28000,  9000, 'DPD_60',  73, '2026-02-10', 12500, '2023-06-10', 14.0, 'DELINQUENT',18, 1, 1),
(62,  'HDFC-CC-20062',  'CREDIT_CARD',   350000, 312000, 255000, 44000, 13000, 'DPD_90', 121, '2025-12-20', 10000, '2021-04-25', 36.0, 'DELINQUENT',18, 2, 4),
(63,  'ICICI-PL-20063', 'PERSONAL_LOAN', 380000, 286000, 257000, 24000,  5000, 'DPD_30',  36, '2026-03-20', 11500, '2023-09-12', 14.5, 'DELINQUENT',18, 3, 5),
(64,  'KOTAK-AL-20064', 'AUTO_LOAN',     550000, 387000, 360000, 22000,  5000, 'DPD_60',  69, '2026-02-13', 15500, '2022-12-18', 11.0, 'DELINQUENT',18, 4, 6),
-- === Customer 19: Lakshmi Subramanian ===
(65,  'LC-HL-20065',    'HOME_LOAN',    3800000,3420000,3298000,106000, 16000, 'DPD_30',  33, '2026-03-24', 33000, '2020-03-14',  8.9, 'DELINQUENT',19, 1, 2),
(66,  'HDFC-PL-20066',  'PERSONAL_LOAN', 520000, 396000, 355000, 32000,  9000, 'DPD_60',  77, '2026-02-07', 14000, '2023-04-18', 13.8, 'DELINQUENT',19, 2, 3),
(67,  'ICICI-CC-20067', 'CREDIT_CARD',   300000, 276000, 225000, 39000, 12000, 'NPA',    202, '2025-10-01',  8500, '2021-06-08', 36.0, 'DELINQUENT',19, 3, 5),
(68,  'KOTAK-PL-20068', 'PERSONAL_LOAN', 400000, 358000, 321000, 29000,  8000, 'DPD_90', 128, '2025-12-16', 12000, '2022-07-20', 15.5, 'DELINQUENT',19, 4, 6),
-- === Customer 20: Harish Naidu ===
(69,  'LC-PL-20069',    'PERSONAL_LOAN', 420000, 348000, 313000, 27000,  8000, 'DPD_90', 135, '2025-12-08', 11500, '2023-01-15', 14.5, 'DELINQUENT',20, 1, 1),
(70,  'HDFC-AL-20070',  'AUTO_LOAN',     650000, 458000, 426000, 27000,  5000, 'DPD_60',  72, '2026-02-11', 18000, '2022-11-05', 10.5, 'DELINQUENT',20, 2, 4),
(71,  'ICICI-PL-20071', 'PERSONAL_LOAN', 290000, 218000, 196000, 17000,  5000, 'DPD_30',  38, '2026-03-18', 9500,  '2024-01-08', 14.0, 'DELINQUENT',20, 3, 5),
(72,  'KOTAK-CC-20072', 'CREDIT_CARD',   220000, 198000, 160000, 28000,  10000,'DPD_60',  65, '2026-02-18', 6500,  '2022-03-15', 36.0, 'DELINQUENT',20, 4, 6),
-- === Customer 21: Dimple Kapoor ===
(73,  'LC-PL-20073',    'PERSONAL_LOAN', 600000, 546000, 490000, 42000, 14000, 'NPA',    232, '2025-08-28', 15000, '2022-02-18', 14.5, 'DELINQUENT',21, 1, 2),
(74,  'HDFC-CC-20074',  'CREDIT_CARD',   500000, 458000, 376000, 64000, 18000, 'DPD_90', 144, '2025-11-28', 15000, '2021-02-10', 36.0, 'DELINQUENT',21, 2, 4),
(75,  'ICICI-PL-20075', 'PERSONAL_LOAN', 550000, 418000, 374000, 34000, 10000, 'DPD_60',  84, '2026-01-30', 15000, '2023-03-22', 13.8, 'DELINQUENT',21, 3, 5),
(76,  'KOTAK-AL-20076', 'AUTO_LOAN',     700000, 494000, 460000, 29000,  5000, 'DPD_30',  35, '2026-03-22', 22000, '2022-08-14', 11.0, 'DELINQUENT',21, 4, 6),
-- === Customer 22: Vivek Tiwari ===
(77,  'LC-PL-20077',    'PERSONAL_LOAN', 260000, 201000, 181000, 16000,  4000, 'DPD_60',  68, '2026-02-14', 8500,  '2023-08-26', 15.0, 'DELINQUENT',22, 1, 1),
(78,  'HDFC-PL-20078',  'PERSONAL_LOAN', 300000, 244000, 219000, 20000,  5000, 'DPD_30',  42, '2026-03-13', 10000, '2023-10-20', 14.5, 'DELINQUENT',22, 2, 3),
(79,  'ICICI-CC-20079', 'CREDIT_CARD',   160000, 146000, 118000, 21000,  7000, 'DPD_90', 114, '2025-12-30',  4500, '2022-01-14', 36.0, 'DELINQUENT',22, 3, 5),
(80,  'KOTAK-PL-20080', 'PERSONAL_LOAN', 280000, 221000, 198000, 18000,  5000, 'DPD_60',  71, '2026-02-12', 9000,  '2023-07-09', 15.5, 'DELINQUENT',22, 4, 6),
-- === Customer 23: Anita Saxena ===
(81,  'LC-AL-20081',    'AUTO_LOAN',     380000, 298000, 276000, 17000,  5000, 'DPD_90', 119, '2025-12-23', 11000, '2022-10-16', 10.5, 'DELINQUENT',23, 1, 2),
(82,  'HDFC-PL-20082',  'PERSONAL_LOAN', 220000, 198000, 177000, 15000,  6000, 'NPA',    215, '2025-09-15',  5500, '2022-04-30', 15.5, 'DELINQUENT',23, 2, 3),
(83,  'ICICI-PL-20083', 'PERSONAL_LOAN', 250000, 189000, 170000, 15000,  4000, 'DPD_60',  66, '2026-02-16', 8000,  '2023-09-04', 14.5, 'DELINQUENT',23, 3, 5),
(84,  'KOTAK-CC-20084', 'CREDIT_CARD',   120000, 102000,  82000, 15000,  5000, 'DPD_30',  43, '2026-03-12',  3500, '2022-08-22', 36.0, 'DELINQUENT',23, 4, 6),
-- === Customer 24: Manish Dubey ===
(85,  'LC-PL-20085',    'PERSONAL_LOAN', 200000, 164000, 148000, 12000,  4000, 'DPD_60',  77, '2026-02-06', 7000,  '2023-10-05', 15.0, 'DELINQUENT',24, 1, 1),
(86,  'HDFC-CC-20086',  'CREDIT_CARD',   100000,  88500,  71000, 13500,  4000, 'DPD_90', 109, '2026-01-01',  2500, '2022-02-18', 36.0, 'DELINQUENT',24, 2, 3),
(87,  'ICICI-AL-20087', 'AUTO_LOAN',     320000, 222000, 206000, 13000,  3000, 'DPD_30',  37, '2026-03-19', 10000, '2023-06-30', 10.0, 'DELINQUENT',24, 3, 5),
(88,  'KOTAK-PL-20088', 'PERSONAL_LOAN', 240000, 192000, 172000, 15000,  5000, 'DPD_60',  63, '2026-02-19', 8000,  '2023-08-12', 15.5, 'DELINQUENT',24, 4, 6),
-- === Customer 25: Shreya Bhatt ===
(89,  'LC-PL-20089',    'PERSONAL_LOAN', 320000, 254000, 228000, 21000,  5000, 'DPD_30',  40, '2026-03-16', 10500, '2023-11-22', 14.0, 'DELINQUENT',25, 1, 2),
(90,  'HDFC-PL-20090',  'PERSONAL_LOAN', 380000, 317000, 284000, 26000,  7000, 'DPD_60',  74, '2026-02-09', 12000, '2023-06-04', 14.5, 'DELINQUENT',25, 2, 3),
(91,  'ICICI-CC-20091', 'CREDIT_CARD',   200000, 184000, 148000, 27000,  9000, 'NPA',    194, '2025-10-06',  5500, '2021-11-18', 36.0, 'DELINQUENT',25, 3, 5),
(92,  'KOTAK-AL-20092', 'AUTO_LOAN',     460000, 374000, 348000, 21000,  5000, 'DPD_90', 130, '2025-12-14', 14500, '2022-09-28', 11.5, 'DELINQUENT',25, 4, 6),
-- === Customer 26: Lokesh Gowda ===
(93,  'LC-PL-20093',    'PERSONAL_LOAN', 360000, 284000, 255000, 24000,  5000, 'DPD_60',  71, '2026-02-12', 11000, '2023-07-18', 14.0, 'DELINQUENT',26, 1, 1),
(94,  'HDFC-CC-20094',  'CREDIT_CARD',   220000, 200000, 162000, 29000,  9000, 'DPD_90', 126, '2025-12-18',  6500, '2021-12-20', 36.0, 'DELINQUENT',26, 2, 4),
(95,  'ICICI-PL-20095', 'PERSONAL_LOAN', 280000, 210000, 188000, 17000,  5000, 'DPD_30',  36, '2026-03-20',  9000, '2024-01-30', 14.5, 'DELINQUENT',26, 3, 5),
(96,  'KOTAK-PL-20096', 'PERSONAL_LOAN', 300000, 252000, 226000, 21000,  5000, 'DPD_60',  68, '2026-02-14', 10500, '2023-05-12', 15.0, 'DELINQUENT',26, 4, 6),
-- === Customer 27: Chitra Balaji ===
(97,  'LC-PL-20097',    'PERSONAL_LOAN', 240000, 218000, 195000, 17000,  6000, 'NPA',    208, '2025-09-24',  5500, '2022-03-15', 15.5, 'DELINQUENT',27, 1, 2),
(98,  'HDFC-AL-20098',  'AUTO_LOAN',     500000, 356000, 331000, 20000,  5000, 'DPD_60',  67, '2026-02-15', 14000, '2022-11-22', 10.5, 'DELINQUENT',27, 2, 3),
(99,  'KOTAK-CC-20099', 'CREDIT_CARD',   180000, 165000, 133000, 24000,  8000, 'DPD_90', 117, '2025-12-27',  5000, '2022-01-08', 36.0, 'DELINQUENT',27, 4, 6),
(100, 'ICICI-PL-20100', 'PERSONAL_LOAN', 270000, 203000, 182000, 16000,  5000, 'DPD_30',  38, '2026-03-19',  8500, '2024-02-14', 14.0, 'DELINQUENT',27, 3, 5),
-- === Customer 28: Imran Siddiqui ===
(101, 'LC-CC-20101',    'CREDIT_CARD',   170000, 151000, 122000, 22000,  7000, 'DPD_60',  80, '2026-02-04', 5000,  '2022-07-06', 36.0, 'DELINQUENT',28, 1, 1),
(102, 'HDFC-PL-20102',  'PERSONAL_LOAN', 350000, 307000, 276000, 24000,  7000, 'DPD_90', 142, '2025-11-30', 11000, '2022-09-19', 14.5, 'DELINQUENT',28, 2, 4),
(103, 'ICICI-PL-20103', 'PERSONAL_LOAN', 290000, 220000, 197000, 18000,  5000, 'DPD_30',  35, '2026-03-21',  9500, '2024-01-12', 14.0, 'DELINQUENT',28, 3, 5),
(104, 'KOTAK-AL-20104', 'AUTO_LOAN',     420000, 296000, 274000, 17000,  5000, 'DPD_60',  63, '2026-02-19', 12500, '2023-03-28', 11.0, 'DELINQUENT',28, 4, 6),
-- === Customer 29: Reena Mahajan ===
(105, 'LC-PL-20105',    'PERSONAL_LOAN', 400000, 323000, 290000, 27000,  6000, 'DPD_60',  74, '2026-02-09', 11500, '2023-05-28', 14.0, 'DELINQUENT',29, 1, 2),
(106, 'HDFC-CC-20106',  'CREDIT_CARD',   280000, 258000, 210000, 37000, 11000, 'NPA',    196, '2025-10-04',  7500, '2021-08-12', 36.0, 'DELINQUENT',29, 2, 3),
(107, 'KOTAK-PL-20107', 'PERSONAL_LOAN', 340000, 276000, 248000, 23000,  5000, 'DPD_30',  41, '2026-03-15', 11000, '2023-10-06', 15.0, 'DELINQUENT',29, 4, 6),
(108, 'ICICI-AL-20108', 'AUTO_LOAN',     580000, 412000, 383000, 24000,  5000, 'DPD_90', 122, '2025-12-22', 16000, '2022-08-18', 11.5, 'DELINQUENT',29, 3, 5),
-- === Customer 30: Sandeep Rao ===
(109, 'LC-PL-20109',    'PERSONAL_LOAN', 300000, 237000, 213000, 19000,  5000, 'DPD_90', 138, '2025-12-05', 9500,  '2022-12-14', 15.0, 'DELINQUENT',30, 1, 1),
(110, 'HDFC-PL-20110',  'PERSONAL_LOAN', 350000, 286000, 257000, 24000,  5000, 'DPD_60',  69, '2026-02-13', 11000, '2023-06-22', 14.5, 'DELINQUENT',30, 2, 4),
(111, 'ICICI-CC-20111', 'CREDIT_CARD',   200000, 184000, 148000, 27000,  9000, 'DPD_60',  75, '2026-02-08',  6000, '2022-05-30', 36.0, 'DELINQUENT',30, 3, 5),
(112, 'KOTAK-AL-20112', 'AUTO_LOAN',     490000, 343000, 318000, 20000,  5000, 'DPD_30',  34, '2026-03-23', 14000, '2023-07-15', 10.5, 'DELINQUENT',30, 4, 6);

-- ============================================================
-- SETTLEMENT DATA: 40 records across new accounts
-- Mix of: ACCEPTED/REJECTED, CLAUDE_AI/AGENT, all strategies
-- All offer amounts validated within lender policy guardrails
-- ============================================================
INSERT INTO settlement (id, customer_id, account_id, transcript_id, proposed_by_agent_id, outstanding_at_offer, offered_amount, discount_percent, payment_plan, number_of_installments, proposed_payment_date, status, source, strategy_code, rationale, customer_response, created_at, decided_at) VALUES

-- Customer 6: Suresh Reddy — LC CC DPD_90 (acct 14, o/s 118500, LC CC DPD_90 policy6: floor 55%, ceil 75%)
(10, 6, 14, NULL, 2, 118500,  78000, 34.18, 'ONE_TIME',    1, DATE '2026-03-20', 'REJECTED',  'AGENT',     'HOLD',
 'Static 34% discount on CC DPD_90. Agent did not probe for installment preference.',
 'Customer said amount still too high, insisted on 50% off.', TIMESTAMP '2026-03-15 10:30:00', TIMESTAMP '2026-03-15 11:00:00'),

(11, 6, 14, NULL, 2, 118500,  68000, 42.62, 'EMI_3',       3, DATE '2026-04-08', 'ACCEPTED',  'CLAUDE_AI', 'REFRAME_INSTALLMENTS',
 'AI detected PARTIAL_WILLINGNESS; split into 3 EMIs of 22,667 keeping total inside floor.',
 'Customer agreed to 3-installment plan. First payment made.', TIMESTAMP '2026-04-02 14:15:00', TIMESTAMP '2026-04-03 10:45:00'),

-- Customer 7: Preethi Krishnan — HDFC PL NPA (acct 18, o/s 243500, HDFC NPA policy23: floor 55%, ceil 80%)
(12, 7, 18, NULL, 3, 243500, 145000, 40.45, 'ONE_TIME',    1, DATE '2026-04-10', 'REJECTED',  'AGENT',     'LOWER',
 'Manual 40% discount on NPA PL. Customer acknowledged debt but rejected lump sum.',
 'Customer asked for deeper discount and installments.', TIMESTAMP '2026-04-05 09:00:00', TIMESTAMP '2026-04-05 09:30:00'),

(13, 7, 18, NULL, 3, 243500, 135000, 44.56, 'EMI_6',       6, DATE '2026-04-28', 'ACCEPTED',  'CLAUDE_AI', 'REFRAME_INSTALLMENTS',
 'AI pivot to REFRAME_INSTALLMENTS after HARDSHIP signal; 6 EMIs of 22,500. Floor 55% = 133,925 — within range.',
 'Customer relieved. Confirmed first EMI via UPI.', TIMESTAMP '2026-04-18 11:00:00', TIMESTAMP '2026-04-19 15:20:00'),

-- Customer 8: Mohit Aggarwal — Kotak CC NPA (acct 24, o/s 182000, Kotak default policy40: floor 82%, ceil 100%)
(14, 8, 24, NULL, 6, 182000, 162000, 10.99, 'ONE_TIME',    1, DATE '2026-03-28', 'REJECTED',  'AGENT',     'HOLD',
 'Conservative Kotak NPA opener at 11% discount. Customer expected more.',
 'Customer wanted at least 20% discount.', TIMESTAMP '2026-03-22 15:30:00', TIMESTAMP '2026-03-22 15:55:00'),

-- Customer 9: Kavita Joshi — LC CC NPA (acct 28, o/s 105000, LC CC NPA policy7: floor 45%, ceil 70%)
(15, 9, 28, NULL, 1, 105000,  58000, 44.76, 'ONE_TIME',    1, DATE '2026-04-12', 'ACCEPTED',  'CLAUDE_AI', 'LOWER',
 'AI confirmed AFFORDABILITY + HARDSHIP double signal; floor 45% = 47,250. Offered 55.2% recovery.',
 'Customer accepted immediately. Paid within 3 days.', TIMESTAMP '2026-04-08 10:00:00', TIMESTAMP '2026-04-10 14:30:00'),

-- Customer 10: Deepak Pandey — LC PL DPD_90 (acct 29, o/s 204500, LC DPD_90 policy4: floor 65%, ceil 85%)
(16, 10, 29, NULL, 1, 204500, 155000, 24.20, 'EMI_3',      3, DATE '2026-04-15', 'ACCEPTED',  'CLAUDE_AI', 'REFRAME_INSTALLMENTS',
 'PARTIAL_WILLINGNESS — customer rejected lump sum twice. 3 EMIs of 51,667. Floor 65% = 132,925.',
 'Accepted installment split. First EMI confirmed.', TIMESTAMP '2026-04-10 11:30:00', TIMESTAMP '2026-04-11 09:00:00'),

(17, 10, 30, NULL, 4, 235000, 185000, 21.28, 'ONE_TIME',   1, DATE '2026-04-18', 'REJECTED',  'AGENT',     'HOLD',
 'HDFC PL DPD_60 manual opener. Floor 85% = 199,750. Offered just above floor.',
 'Customer disputed interest calculation, refused to engage.', TIMESTAMP '2026-04-12 14:00:00', TIMESTAMP '2026-04-12 14:45:00'),

-- Customer 11: Nisha Choudhary — LC PL NPA (acct 33, o/s 198000, LC NPA policy5: floor 50%, ceil 75%)
(18, 11, 33, NULL, 2, 198000, 112000, 43.43, 'EMI_6',      6, DATE '2026-04-22', 'ACCEPTED',  'CLAUDE_AI', 'REFRAME_INSTALLMENTS',
 'HARDSHIP signal strong; job loss confirmed. 6 EMIs of 18,667. Floor 50% = 99,000 — compliant.',
 'Customer very grateful. Signed EMI arrangement.', TIMESTAMP '2026-04-16 10:15:00', TIMESTAMP '2026-04-17 12:30:00'),

(19, 11, 34, NULL, 3, 158000, 108000, 31.65, 'ONE_TIME',   1, DATE '2026-03-22', 'REJECTED',  'AGENT',     'LOWER',
 'HDFC CC DPD_90 manual 32% discount. Customer wanted installments.',
 'Declined. Will call back.', TIMESTAMP '2026-03-18 16:00:00', TIMESTAMP '2026-03-18 16:30:00'),

-- Customer 12: Ravi Shankar Iyer — BUNDLE (accts 38+39 HDFC, o/s 468k+368k=836k)
(20, 12, 38, NULL, 4, 836000, 612000, 26.79, 'EMI_6',      6, DATE '2026-04-25', 'ACCEPTED',  'CLAUDE_AI', 'BUNDLE',
 'BUNDLE strategy — HDFC PL + HDFC CC packaged. Blended discount 26.79%. HDFC NPA floor 55% = 459,800.',
 'Customer delighted with bundled deal. Agreed to 6 EMIs of 102,000.', TIMESTAMP '2026-04-20 11:00:00', TIMESTAMP '2026-04-21 10:00:00'),

(21, 12, 40, NULL, 6, 448000, 370000, 17.41, 'ONE_TIME',   1, DATE '2026-04-05', 'REJECTED',  'AGENT',     'HOLD',
 'Kotak PL NPA static opener at 17% discount. Kotak default floor 82% = 367,360. Very narrow room.',
 'Customer wants more time and better terms.', TIMESTAMP '2026-03-30 14:30:00', TIMESTAMP '2026-03-31 10:00:00'),

-- Customer 13: Fatima Khan — KOTAK CC DPD_90 (acct 44, o/s 143000, Kotak CC DPD_90 policy42: floor 60%, ceil 80%)
(22, 13, 44, NULL, 6, 143000,  98000, 31.47, 'EMI_3',      3, DATE '2026-04-18', 'ACCEPTED',  'CLAUDE_AI', 'REFRAME_INSTALLMENTS',
 'PARTIAL_WILLINGNESS. 3 EMIs of 32,667. Floor 60% = 85,800 — well within.',
 'Customer agreed. First installment cleared.', TIMESTAMP '2026-04-12 10:30:00', TIMESTAMP '2026-04-13 11:15:00'),

-- Customer 14: Arun Pillai — HDFC CC NPA (acct 46, o/s 228000, HDFC CC NPA policy24: floor 50%, ceil 75%)
(23, 14, 46, NULL, 3, 228000, 140000, 38.60, 'ONE_TIME',   1, DATE '2026-04-10', 'REJECTED',  'AGENT',     'LOWER',
 'Manual 39% discount on HDFC NPA CC. Still above floor 50% = 114,000.',
 'Customer disputed the outstanding figure, refused to pay.', TIMESTAMP '2026-04-06 09:00:00', TIMESTAMP '2026-04-06 09:45:00'),

(24, 14, 46, NULL, 5, 228000, 125000, 45.18, 'EMI_3',      3, DATE '2026-04-24', 'PROPOSED',  'CLAUDE_AI', 'LOWER',
 'AI re-engaged after dispute resolved. Revised to 45% discount, 3 EMIs. Floor 50% = 114,000 — compliant.',
 NULL, TIMESTAMP '2026-04-21 14:00:00', NULL),

-- Customer 15: Sunita Verma — BUNDLE LC+ICICI accounts (accts 49+51)
(25, 15, 49, NULL, 2, 373000, 290000, 22.25, 'EMI_3',      3, DATE '2026-04-20', 'ACCEPTED',  'CLAUDE_AI', 'REFRAME_INSTALLMENTS',
 'PARTIAL_WILLINGNESS — bundled LC PL + ICICI CC. 3 EMIs of 96,667. LC DPD_60 floor 80% = 214,400.',
 'Customer excited about closing two accounts. Confirmed.', TIMESTAMP '2026-04-15 10:00:00', TIMESTAMP '2026-04-16 14:30:00'),

-- Customer 16: Rajesh Kumar — HDFC PL NPA (acct 54, o/s 414000, HDFC NPA policy23: floor 55%, ceil 80%)
(26, 16, 54, NULL, 4, 414000, 262000, 36.71, 'ONE_TIME',   1, DATE '2026-04-15', 'REJECTED',  'AGENT',     'LOWER',
 'Manual 37% discount — agent did not have installment script.',
 'Customer asked for payment split — agent could not offer it.', TIMESTAMP '2026-04-10 11:00:00', TIMESTAMP '2026-04-10 11:35:00'),

(27, 16, 54, NULL, 4, 414000, 245000, 40.82, 'EMI_6',      6, DATE '2026-05-02', 'PROPOSED',  'CLAUDE_AI', 'REFRAME_INSTALLMENTS',
 'AI follow-up: 6 EMIs of 40,833. Floor 55% = 227,700. Offer at 59.2% recovery.',
 NULL, TIMESTAMP '2026-04-22 10:00:00', NULL),

-- Customer 17: Pooja Mishra — KOTAK PL NPA (acct 60, o/s 182000, Kotak DPD_90 policy41: floor 75%, ceil 90%)
(28, 17, 60, NULL, 6, 182000, 145000, 20.33, 'ONE_TIME',   1, DATE '2026-04-08', 'REJECTED',  'AGENT',     'HOLD',
 'Kotak NPA opener. Floor 75% = 136,500. Offered 79.7% — close to floor.',
 'Customer in genuine distress, cannot pay any lump sum.', TIMESTAMP '2026-04-03 15:00:00', TIMESTAMP '2026-04-03 15:30:00'),

(29, 17, 60, NULL, 6, 182000, 140000, 23.08, 'EMI_12',    12, DATE '2026-05-05', 'PROPOSED',  'CLAUDE_AI', 'REFRAME_INSTALLMENTS',
 'HARDSHIP confirmed. Stretched to 12 EMIs of 11,667. Floor 75% = 136,500 — compliant.',
 NULL, TIMESTAMP '2026-04-20 10:30:00', NULL),

-- Customer 18: Sameer Sheikh — HDFC CC DPD_90 (acct 62, o/s 312000, HDFC DPD_90 policy22: floor 70%, ceil 90%)
(30, 18, 62, NULL, 4, 312000, 234000, 25.00, 'ONE_TIME',   1, DATE '2026-03-25', 'REJECTED',  'AGENT',     'LOWER',
 'Manual 25% discount. Floor 70% = 218,400. Offered 75% recovery.',
 'Customer insisted on installment plan.', TIMESTAMP '2026-03-20 14:00:00', TIMESTAMP '2026-03-20 14:40:00'),

(31, 18, 62, NULL, 4, 312000, 225000, 27.88, 'EMI_3',      3, DATE '2026-04-15', 'ACCEPTED',  'CLAUDE_AI', 'REFRAME_INSTALLMENTS',
 'AI split into 3 EMIs of 75,000. Floor 70% = 218,400. Offer 72.1% recovery.',
 'Customer confirmed. EMI standing instruction set up.', TIMESTAMP '2026-04-10 11:00:00', TIMESTAMP '2026-04-11 09:30:00'),

-- Customer 19: Lakshmi Subramanian — ICICI CC NPA (acct 67, o/s 276000, ICICI NPA policy33: floor 45%, ceil 75%)
(32, 19, 67, NULL, 5, 276000, 172000, 37.68, 'ONE_TIME',   1, DATE '2026-04-12', 'ACCEPTED',  'CLAUDE_AI', 'LOWER',
 'AFFORDABILITY objection. AI moved to 37.68% discount. Floor 45% = 124,200 — well above floor.',
 'Customer paid full settlement amount within 5 days.', TIMESTAMP '2026-04-08 09:00:00', TIMESTAMP '2026-04-12 16:00:00'),

-- Customer 20: Harish Naidu — LC PL DPD_90 (acct 69, o/s 348000, LC DPD_90 policy4: floor 65%, ceil 85%)
(33, 20, 69, NULL, 1, 348000, 275000, 20.98, 'ONE_TIME',   1, DATE '2026-04-05', 'REJECTED',  'AGENT',     'HOLD',
 'Static opener 21% discount on DPD_90. Customer expected REFRAME.',
 'Customer said he can only pay in parts.', TIMESTAMP '2026-03-30 10:00:00', TIMESTAMP '2026-03-30 10:30:00'),

(34, 20, 69, NULL, 1, 348000, 252000, 27.59, 'EMI_3',      3, DATE '2026-04-22', 'ACCEPTED',  'CLAUDE_AI', 'REFRAME_INSTALLMENTS',
 '3 EMIs of 84,000. Floor 65% = 226,200. Offer at 72.4% recovery.',
 'Customer accepted and paid first installment.', TIMESTAMP '2026-04-17 14:00:00', TIMESTAMP '2026-04-18 10:30:00'),

-- Customer 21: Dimple Kapoor — LC PL NPA (acct 73, o/s 546000, LC NPA policy5: floor 50%, ceil 75%)
(35, 21, 73, NULL, 2, 546000, 360000, 34.07, 'EMI_6',      6, DATE '2026-04-28', 'PROPOSED',  'CLAUDE_AI', 'LOWER',
 'HARDSHIP + high outstanding. 6 EMIs of 60,000. Floor 50% = 273,000. Offer 65.9% recovery.',
 NULL, TIMESTAMP '2026-04-22 11:00:00', NULL),

-- Customer 22: Vivek Tiwari — ICICI CC DPD_90 (acct 79, o/s 146000, ICICI DPD_90 policy32: floor 60%, ceil 85%)
(36, 22, 79, NULL, 5, 146000,  95000, 34.93, 'ONE_TIME',   1, DATE '2026-04-18', 'ACCEPTED',  'CLAUDE_AI', 'LOWER',
 'AFFORDABILITY confirmed. Floor 60% = 87,600. Offered 65.1% recovery.',
 'Customer accepted immediately. Cleared same day.', TIMESTAMP '2026-04-14 11:30:00', TIMESTAMP '2026-04-17 14:00:00'),

-- Customer 23: Anita Saxena — HDFC PL NPA (acct 82, o/s 198000, HDFC NPA policy23: floor 55%, ceil 80%)
(37, 23, 82, NULL, 3, 198000, 125000, 36.87, 'EMI_3',      3, DATE '2026-04-20', 'ACCEPTED',  'CLAUDE_AI', 'REFRAME_INSTALLMENTS',
 '3 EMIs of 41,667. Floor 55% = 108,900. Offer 63.1% recovery.',
 'Customer agreed. Auto-debit set up.', TIMESTAMP '2026-04-15 10:30:00', TIMESTAMP '2026-04-16 09:00:00'),

-- Customer 24: Manish Dubey — LC PL DPD_60 (acct 85, o/s 164000, LC DPD_60 policy3: floor 80%, ceil 95%)
(38, 24, 85, NULL, 1, 164000, 136000, 17.07, 'ONE_TIME',   1, DATE '2026-04-15', 'REJECTED',  'AGENT',     'HOLD',
 'Static opener 17% discount. Floor 80% = 131,200. Offered 82.9% recovery.',
 'Customer refused. Very high DPD stress. Asked for write-off.', TIMESTAMP '2026-04-10 16:00:00', TIMESTAMP '2026-04-10 16:30:00'),

-- Customer 25: Shreya Bhatt — ICICI CC NPA (acct 91, o/s 184000, ICICI NPA policy33: floor 45%, ceil 75%)
(39, 25, 91, NULL, 5, 184000, 105000, 42.93, 'EMI_3',      3, DATE '2026-04-22', 'ACCEPTED',  'CLAUDE_AI', 'LOWER',
 'HARDSHIP confirmed. 3 EMIs of 35,000. Floor 45% = 82,800. Compliant.',
 'Customer very cooperative. Confirmed first installment.', TIMESTAMP '2026-04-17 10:00:00', TIMESTAMP '2026-04-18 14:00:00'),

-- Customer 26: Lokesh Gowda — HDFC CC DPD_90 (acct 94, o/s 200000, HDFC DPD_90 policy22: floor 70%, ceil 90%)
(40, 26, 94, NULL, 4, 200000, 152000, 24.00, 'ONE_TIME',   1, DATE '2026-04-12', 'REJECTED',  'AGENT',     'LOWER',
 'Manual 24% discount. Floor 70% = 140,000. Offered 76% recovery.',
 'Customer wants 6-month EMI plan.', TIMESTAMP '2026-04-07 11:00:00', TIMESTAMP '2026-04-07 11:30:00'),

(41, 26, 94, NULL, 4, 200000, 148000, 26.00, 'EMI_6',      6, DATE '2026-05-01', 'PROPOSED',  'CLAUDE_AI', 'REFRAME_INSTALLMENTS',
 '6 EMIs of 24,667. Floor 70% = 140,000. Offer 74% recovery.',
 NULL, TIMESTAMP '2026-04-21 14:30:00', NULL),

-- Customer 27: Chitra Balaji — LC PL NPA (acct 97, o/s 218000, LC NPA policy5: floor 50%, ceil 75%)
(42, 27, 97, NULL, 2, 218000, 130000, 40.37, 'EMI_3',      3, DATE '2026-04-20', 'ACCEPTED',  'CLAUDE_AI', 'LOWER',
 'AFFORDABILITY + NPA. 3 EMIs of 43,333. Floor 50% = 109,000. Offer 59.6% recovery.',
 'Customer relieved with split. First payment made.', TIMESTAMP '2026-04-14 14:00:00', TIMESTAMP '2026-04-15 11:00:00'),

-- Customer 28: Imran Siddiqui — HDFC PL DPD_90 (acct 102, o/s 307000, HDFC DPD_90 policy22: floor 70%, ceil 90%)
(43, 28, 102, NULL, 4, 307000, 230000, 25.08, 'ONE_TIME',  1, DATE '2026-04-10', 'REJECTED',  'AGENT',     'HOLD',
 'Manual 25% discount. Floor 70% = 214,900. Offered 74.9% recovery.',
 'Customer not ready to commit. Avoidance behaviour.', TIMESTAMP '2026-04-05 10:30:00', TIMESTAMP '2026-04-05 11:00:00'),

(44, 28, 102, NULL, 4, 307000, 218000, 29.00, 'EMI_3',     3, DATE '2026-04-25', 'PROPOSED',  'CLAUDE_AI', 'REFRAME_INSTALLMENTS',
 'AI switched to REFRAME after AVOIDANCE detected. 3 EMIs of 72,667. 71% recovery.',
 NULL, TIMESTAMP '2026-04-20 10:00:00', NULL),

-- Customer 29: Reena Mahajan — HDFC CC NPA (acct 106, o/s 258000, HDFC CC NPA policy24: floor 50%, ceil 75%)
(45, 29, 106, NULL, 3, 258000, 162000, 37.21, 'EMI_6',     6, DATE '2026-04-28', 'ACCEPTED',  'CLAUDE_AI', 'REFRAME_INSTALLMENTS',
 'HARDSHIP + NPA. 6 EMIs of 27,000. Floor 50% = 129,000. Offer 62.8% recovery.',
 'Customer signed up. 6 months EMI standing instruction activated.', TIMESTAMP '2026-04-18 11:00:00', TIMESTAMP '2026-04-20 14:00:00'),

-- Customer 30: Sandeep Rao — LC PL DPD_90 (acct 109, o/s 237000, LC DPD_90 policy4: floor 65%, ceil 85%)
(46, 30, 109, NULL, 1, 237000, 178000, 24.89, 'ONE_TIME',  1, DATE '2026-04-08', 'REJECTED',  'AGENT',     'HOLD',
 'Static opener 25% discount. Floor 65% = 154,050. Offered 75.1% recovery.',
 'Customer busy with family emergency, said will call back. Avoidance.',TIMESTAMP '2026-04-02 09:30:00', TIMESTAMP '2026-04-02 10:00:00'),

(47, 30, 109, NULL, 1, 237000, 162000, 31.65, 'EMI_3',     3, DATE '2026-04-22', 'ACCEPTED',  'CLAUDE_AI', 'LOWER',
 'HARDSHIP confirmed on follow-up. 3 EMIs of 54,000. Floor 65% = 154,050. 68.4% recovery.',
 'Customer accepted. First EMI cleared via NEFT.', TIMESTAMP '2026-04-17 14:00:00', TIMESTAMP '2026-04-18 09:00:00'),

-- Additional BUNDLE scenario — Customer 8: Mohit Aggarwal (LC accts 21+28)
(48, 8, 21, NULL, 1, 494000, 368000, 25.51, 'EMI_6',      6, DATE '2026-05-01', 'PROPOSED',  'CLAUDE_AI', 'BUNDLE',
 'BUNDLE: LC PL acct 21 (o/s 389k) + LC CC acct 28 (o/s 105k). Combined 389k floor 80% + CC NPA floor 45%.',
 NULL, TIMESTAMP '2026-04-22 10:00:00', NULL),

-- EXPIRED proposal — Customer 9 / ICICI PL DPD_90 (acct 27, o/s 248000, ICICI DPD_90 policy32: floor 60%, ceil 85%)
(49, 9, 27, NULL, 5, 248000, 182000, 26.61, 'ONE_TIME',   1, DATE '2026-04-01', 'EXPIRED',   'CLAUDE_AI', 'LOWER',
 'Offer sent 2 weeks ago, no response. Floor 60% = 148,800. Expired per 14-day rule.',
 'No response.', TIMESTAMP '2026-03-18 11:00:00', TIMESTAMP '2026-04-01 23:59:00'),

-- PAID status — Customer 15 / KOTAK AL DPD_60 (acct 52, o/s 336000, Kotak DPD_90 policy41: floor 75%, ceil 90%)
(50, 15, 52, NULL, 6, 336000, 262000, 22.02, 'ONE_TIME',  1, DATE '2026-04-10', 'PAID',      'CLAUDE_AI', 'LOWER',
 'AFFORDABILITY. Floor 75% = 252,000. Offered 78% recovery. Paid in full.',
 'Payment confirmed via NEFT. Account closed.', TIMESTAMP '2026-04-05 09:00:00', TIMESTAMP '2026-04-10 12:00:00');


-- ============================================================
-- Reset sequences so app-generated IDs continue after seed data
-- ============================================================
-- ============================================================
-- 10 customers (IDs 31-40) each with MULTIPLE accounts at the
-- SAME lender — specifically built to demo BUNDLE strategy.
-- Each customer has 4-5 accounts; 2-3 at one lender for bundle.
-- ============================================================

INSERT INTO customer (id, full_name, pan_number, aadhaar_masked, email, phone, address, city, state, pincode, date_of_birth, annual_income) VALUES
  (31, 'Karthik Nair',       'EAPKN1234J', 'XXXX-XXXX-3101', 'karthik.nair@gmail.com',    '+91-98765-43241', '9 Powai Lake Drive',          'Mumbai',      'Maharashtra',   '400076', '1986-08-22', 1650000),
  (32, 'Priya Agarwal',      'FBPPA5678K', 'XXXX-XXXX-3202', 'priya.agarwal@yahoo.in',    '+91-98765-43242', '14 DLF Phase 2',               'Gurugram',    'Haryana',       '122002', '1989-03-14', 2100000),
  (33, 'Santosh Kulkarni',   'GCPSK9012L', 'XXXX-XXXX-3303', 'santosh.kulkarni@gmail.com','+91-98765-43243', '33 Deccan Gymkhana',           'Pune',        'Maharashtra',   '411004', '1981-11-05', 1380000),
  (34, 'Meghna Rao',         'HDPMR3456M', 'XXXX-XXXX-3404', 'meghna.rao@outlook.com',    '+91-98765-43244', '22 Jayanagar 4th Block',       'Bangalore',   'Karnataka',     '560011', '1984-06-30', 1720000),
  (35, 'Ajay Mishra',        'IEPAM7890N', 'XXXX-XXXX-3505', 'ajay.mishra@gmail.com',     '+91-98765-43245', '7 Civil Lines',                'Allahabad',   'Uttar Pradesh', '211001', '1979-02-17',  890000),
  (36, 'Shruti Patel',       'JFPSP2345O', 'XXXX-XXXX-3606', 'shruti.patel@rediff.com',   '+91-98765-43246', '45 Navrangpura',               'Ahmedabad',   'Gujarat',       '380009', '1992-09-28', 1250000),
  (37, 'Naresh Bhat',        'KGPNB6789P', 'XXXX-XXXX-3707', 'naresh.bhat@gmail.com',     '+91-98765-43247', '18 Malleshwaram 15th Cross',   'Bangalore',   'Karnataka',     '560055', '1977-05-03', 1950000),
  (38, 'Geeta Sharma',       'LHPGS1234Q', 'XXXX-XXXX-3808', 'geeta.sharma@gmail.com',    '+91-98765-43248', '3 Vasant Vihar',               'Delhi',       'Delhi',         '110057', '1983-12-19', 1480000),
  (39, 'Vijay Menon',        'MIPVM5678R', 'XXXX-XXXX-3909', 'vijay.menon@yahoo.com',     '+91-98765-43249', '11 Pattom Junction',           'Thiruvananthapuram','Kerala',   '695004', '1988-04-11', 1120000),
  (40, 'Amita Gupta',        'NJPAG9012S', 'XXXX-XXXX-4010', 'amita.gupta@gmail.com',     '+91-98765-43250', '56 Ashok Nagar',               'Chennai',     'Tamil Nadu',    '600083', '1991-07-25', 1340000);

-- ============================================================
-- Accounts — deliberately give 2-3 accounts at same lender
-- so BUNDLE strategy can be triggered in demo
-- ============================================================
INSERT INTO account (id, account_number, product_type, sanctioned_amount, outstanding_amount, principal_outstanding, interest_outstanding, penalty_amount, dpd_bucket, days_past_due, last_payment_date, last_payment_amount, sanction_date, interest_rate, status, customer_id, lender_id, assigned_agent_id) VALUES

-- ── Customer 31: Karthik Nair — 3 LC accounts (bundle candidate) + 1 HDFC ──
(113, 'LC-PL-30113', 'PERSONAL_LOAN', 500000, 382000, 344000, 29000,  9000, 'DPD_90', 102, '2026-01-08', 13000, '2023-02-14', 14.5, 'DELINQUENT', 31, 1, 1),
(114, 'LC-CC-30114', 'CREDIT_CARD',   200000, 174000, 140000, 26000,  8000, 'DPD_90',  98, '2026-01-12',  6000, '2021-08-20', 36.0, 'DELINQUENT', 31, 1, 2),
(115, 'LC-AL-30115', 'AUTO_LOAN',     600000, 432000, 402000, 25000,  5000, 'DPD_60',  76, '2026-02-07', 16500, '2022-10-05', 10.5, 'DELINQUENT', 31, 1, 1),
(116, 'HDFC-PL-30116','PERSONAL_LOAN',350000, 284000, 255000, 23000,  6000, 'DPD_60',  68, '2026-02-15', 11000, '2023-05-20', 14.0, 'DELINQUENT', 31, 2, 4),

-- ── Customer 32: Priya Agarwal — 3 HDFC accounts (bundle candidate) + 1 ICICI ──
(117, 'HDFC-PL-30117','PERSONAL_LOAN',650000, 512000, 460000, 40000, 12000, 'DPD_90', 115, '2025-12-26', 17500, '2022-10-10', 13.8, 'DELINQUENT', 32, 2, 4),
(118, 'HDFC-CC-30118','CREDIT_CARD',  400000, 364000, 298000, 52000, 14000, 'NPA',    200, '2025-09-22', 11000, '2021-03-18', 36.0, 'DELINQUENT', 32, 2, 3),
(119, 'HDFC-AL-30119','AUTO_LOAN',    800000, 568000, 530000, 32000,  6000, 'DPD_60',  80, '2026-02-03', 22000, '2022-07-14', 10.5, 'DELINQUENT', 32, 2, 4),
(120, 'ICICI-PL-30120','PERSONAL_LOAN',420000,318000, 285000, 27000,  6000, 'DPD_60',  63, '2026-02-19', 12000, '2023-06-08', 14.5, 'DELINQUENT', 32, 3, 5),

-- ── Customer 33: Santosh Kulkarni — 2 LC + 2 ICICI + 1 Kotak ──
(121, 'LC-PL-30121', 'PERSONAL_LOAN', 450000, 356000, 320000, 28000,  8000, 'DPD_90', 108, '2026-01-02', 12500, '2022-12-05', 14.0, 'DELINQUENT', 33, 1, 2),
(122, 'LC-CC-30122', 'CREDIT_CARD',   180000, 162000, 130000, 24000,  8000, 'NPA',    190, '2025-10-10',  5000, '2021-09-22', 36.0, 'DELINQUENT', 33, 1, 1),
(123, 'ICICI-PL-30123','PERSONAL_LOAN',380000,294000, 264000, 24000,  6000, 'DPD_60',  72, '2026-02-11', 11500, '2023-04-16', 14.5, 'DELINQUENT', 33, 3, 5),
(124, 'ICICI-CC-30124','CREDIT_CARD', 220000, 198000, 160000, 28000, 10000, 'DPD_90',  96, '2026-01-14',  6500, '2022-02-28', 36.0, 'DELINQUENT', 33, 3, 5),
(125, 'KOTAK-PL-30125','PERSONAL_LOAN',300000,238000, 213000, 20000,  5000, 'DPD_60',  65, '2026-02-17',  9500, '2023-07-12', 15.0, 'DELINQUENT', 33, 4, 6),

-- ── Customer 34: Meghna Rao — 3 ICICI accounts (bundle candidate) + 1 LC + 1 HDFC ──
(126, 'ICICI-PL-30126','PERSONAL_LOAN',500000,392000, 352000, 32000,  8000, 'DPD_90', 120, '2025-12-22', 14000, '2022-09-28', 14.0, 'DELINQUENT', 34, 3, 5),
(127, 'ICICI-CC-30127','CREDIT_CARD', 300000, 274000, 222000, 39000, 13000, 'NPA',    208, '2025-09-16',  8500, '2021-04-10', 36.0, 'DELINQUENT', 34, 3, 5),
(128, 'ICICI-AL-30128','AUTO_LOAN',   700000, 504000, 470000, 29000,  5000, 'DPD_60',  82, '2026-02-02', 20000, '2022-11-30', 11.5, 'DELINQUENT', 34, 3, 5),
(129, 'LC-PL-30129',  'PERSONAL_LOAN',320000, 256000, 230000, 21000,  5000, 'DPD_60',  70, '2026-02-13', 10000, '2023-08-22', 15.0, 'DELINQUENT', 34, 1, 1),
(130, 'HDFC-AL-30130','AUTO_LOAN',    550000, 391000, 364000, 22000,  5000, 'DPD_30',  38, '2026-03-18', 15000, '2023-03-14', 10.5, 'DELINQUENT', 34, 2, 3),

-- ── Customer 35: Ajay Mishra — 3 HDFC accounts (bundle candidate) + 1 Kotak ──
(131, 'HDFC-PL-30131','PERSONAL_LOAN',280000, 242000, 217000, 19000,  6000, 'DPD_90', 127, '2025-12-17', 10000, '2023-01-08', 15.5, 'DELINQUENT', 35, 2, 3),
(132, 'HDFC-CC-30132','CREDIT_CARD',  200000, 183000, 148000, 27000,  8000, 'NPA',    215, '2025-09-14',  5500, '2021-07-20', 36.0, 'DELINQUENT', 35, 2, 4),
(133, 'HDFC-AL-30133','AUTO_LOAN',    450000, 318000, 296000, 17000,  5000, 'DPD_60',  67, '2026-02-15', 13000, '2022-08-05', 10.0, 'DELINQUENT', 35, 2, 3),
(134, 'KOTAK-CC-30134','CREDIT_CARD', 150000, 135000, 108000, 20000,  7000, 'DPD_90', 103, '2026-01-07',  4500, '2022-03-12', 36.0, 'DELINQUENT', 35, 4, 6),

-- ── Customer 36: Shruti Patel — 2 Kotak + 2 LC + 1 HDFC ──
(135, 'KOTAK-PL-30135','PERSONAL_LOAN',400000,322000, 289000, 27000,  6000, 'DPD_90', 110, '2025-12-30', 11500, '2022-10-25', 15.0, 'DELINQUENT', 36, 4, 6),
(136, 'KOTAK-CC-30136','CREDIT_CARD', 250000, 228000, 184000, 33000, 11000, 'NPA',    192, '2025-10-08',  7000, '2021-05-14', 36.0, 'DELINQUENT', 36, 4, 6),
(137, 'LC-PL-30137',  'PERSONAL_LOAN',360000, 284000, 255000, 24000,  5000, 'DPD_60',  74, '2026-02-09', 11000, '2023-06-18', 14.0, 'DELINQUENT', 36, 1, 2),
(138, 'LC-CC-30138',  'CREDIT_CARD',  160000, 144000, 116000, 21000,  7000, 'DPD_90',  95, '2026-01-16',  5000, '2022-04-30', 36.0, 'DELINQUENT', 36, 1, 1),
(139, 'HDFC-PL-30139','PERSONAL_LOAN',310000, 248000, 223000, 20000,  5000, 'DPD_30',  40, '2026-03-16', 10000, '2023-11-10', 14.5, 'DELINQUENT', 36, 2, 3),

-- ── Customer 37: Naresh Bhat — 2 LC + 2 HDFC + 1 ICICI ──
(140, 'LC-PL-30140',  'PERSONAL_LOAN',600000, 472000, 424000, 37000, 11000, 'DPD_90', 132, '2025-12-10', 16500, '2022-08-15', 14.5, 'DELINQUENT', 37, 1, 1),
(141, 'LC-HL-30141',  'HOME_LOAN',   4500000,4050000,3900000,127000, 23000, 'DPD_30',  34, '2026-03-23', 38000, '2019-06-20',  8.9, 'DELINQUENT', 37, 1, 2),
(142, 'HDFC-PL-30142','PERSONAL_LOAN',480000, 386000, 347000, 31000,  8000, 'DPD_60',  79, '2026-02-04', 14000, '2023-03-22', 13.8, 'DELINQUENT', 37, 2, 4),
(143, 'HDFC-CC-30143','CREDIT_CARD', 350000, 320000, 261000, 46000, 13000, 'DPD_90', 108, '2026-01-03', 10500, '2021-10-08', 36.0, 'DELINQUENT', 37, 2, 3),
(144, 'ICICI-AL-30144','AUTO_LOAN',   700000, 498000, 464000, 29000,  5000, 'DPD_60',  73, '2026-02-10', 20000, '2022-09-18', 11.5, 'DELINQUENT', 37, 3, 5),

-- ── Customer 38: Geeta Sharma — 2 LC + 2 HDFC + 1 Kotak ──
(145, 'LC-PL-30145',  'PERSONAL_LOAN',400000, 316000, 284000, 25000,  7000, 'DPD_60',  77, '2026-02-06', 11500, '2023-04-12', 14.0, 'DELINQUENT', 38, 1, 2),
(146, 'LC-CC-30146',  'CREDIT_CARD',  250000, 228000, 184000, 33000, 11000, 'DPD_90', 106, '2026-01-04',  7500, '2021-11-22', 36.0, 'DELINQUENT', 38, 1, 1),
(147, 'HDFC-PL-30147','PERSONAL_LOAN',550000, 432000, 388000, 35000,  9000, 'DPD_90', 118, '2025-12-24', 15000, '2022-11-18', 13.8, 'DELINQUENT', 38, 2, 4),
(148, 'HDFC-CC-30148','CREDIT_CARD', 300000, 275000, 224000, 39000, 12000, 'NPA',    196, '2025-10-04',  8500, '2021-06-15', 36.0, 'DELINQUENT', 38, 2, 3),
(149, 'KOTAK-AL-30149','AUTO_LOAN',   600000, 425000, 396000, 24000,  5000, 'DPD_60',  66, '2026-02-17', 17000, '2022-12-06', 11.0, 'DELINQUENT', 38, 4, 6),

-- ── Customer 39: Vijay Menon — 3 ICICI accounts (bundle candidate) + 1 LC + 1 HDFC ──
(150, 'ICICI-PL-30150','PERSONAL_LOAN',400000,318000, 285000, 26000,  7000, 'DPD_60',  71, '2026-02-12', 11500, '2023-05-10', 14.5, 'DELINQUENT', 39, 3, 5),
(151, 'ICICI-CC-30151','CREDIT_CARD', 200000, 182000, 147000, 26000,  9000, 'DPD_90',  99, '2026-01-11',  5500, '2022-01-25', 36.0, 'DELINQUENT', 39, 3, 5),
(152, 'ICICI-AL-30152','AUTO_LOAN',   550000, 390000, 364000, 22000,  4000, 'DPD_30',  36, '2026-03-20', 14500, '2023-07-04', 11.0, 'DELINQUENT', 39, 3, 5),
(153, 'LC-PL-30153',  'PERSONAL_LOAN',300000, 237000, 213000, 19000,  5000, 'DPD_90', 124, '2025-12-20',  9000, '2023-02-18', 15.0, 'DELINQUENT', 39, 1, 1),
(154, 'HDFC-CC-30154','CREDIT_CARD', 180000, 162000, 131000, 23000,  8000, 'DPD_60',  63, '2026-02-19',  5000, '2022-06-10', 36.0, 'DELINQUENT', 39, 2, 4),

-- ── Customer 40: Amita Gupta — 2 LC + 1 HDFC + 1 ICICI + 1 Kotak ──
(155, 'LC-PL-30155',  'PERSONAL_LOAN',350000, 278000, 250000, 22000,  6000, 'DPD_60',  78, '2026-02-05', 10500, '2023-06-24', 14.0, 'DELINQUENT', 40, 1, 2),
(156, 'LC-CC-30156',  'CREDIT_CARD',  190000, 172000, 138000, 25000,  9000, 'DPD_90', 114, '2025-12-29',  5500, '2021-10-16', 36.0, 'DELINQUENT', 40, 1, 1),
(157, 'HDFC-AL-30157','AUTO_LOAN',    500000, 354000, 330000, 20000,  4000, 'DPD_30',  39, '2026-03-17', 14000, '2023-09-08', 10.5, 'DELINQUENT', 40, 2, 3),
(158, 'ICICI-PL-30158','PERSONAL_LOAN',280000,214000, 192000, 17000,  5000, 'DPD_60',  73, '2026-02-10',  9000, '2023-07-30', 14.5, 'DELINQUENT', 40, 3, 5),
(159, 'KOTAK-PL-30159','PERSONAL_LOAN',260000,208000, 186000, 17000,  5000, 'DPD_60',  68, '2026-02-14',  8500, '2023-08-18', 15.0, 'DELINQUENT', 40, 4, 6);

-- ============================================================
-- SETTLEMENTS for these multi-account customers
-- Focus on BUNDLE strategy + cross-account scenarios
-- ============================================================
INSERT INTO settlement (id, customer_id, account_id, transcript_id, proposed_by_agent_id, outstanding_at_offer, offered_amount, discount_percent, payment_plan, number_of_installments, proposed_payment_date, status, source, strategy_code, rationale, customer_response, created_at, decided_at) VALUES

-- Customer 31: Karthik Nair — BUNDLE all 3 LC accounts (113+114+115, o/s 382k+174k+432k = 988k)
-- LC DPD_90 policy4 floor 65%; LC CC DPD_90 policy6 floor 55%; LC AL DPD_60 policy3 floor 80%
(51, 31, 113, NULL, 1, 988000, 712000, 27.93, 'EMI_6',  6, DATE '2026-04-25', 'ACCEPTED',  'CLAUDE_AI', 'BUNDLE',
 'BUNDLE: all 3 LC accounts (PL+CC+AL). Combined outstanding ₹9.88L. Blended discount 27.93%. 6 EMIs of ₹1,18,667. All floors respected.',
 'Customer overjoyed to clear 3 LC accounts in one deal. First EMI confirmed.', TIMESTAMP '2026-04-20 10:00:00', TIMESTAMP '2026-04-21 14:30:00'),

-- Customer 31: HDFC account individual (116, o/s 284000, HDFC DPD_60 policy21: floor 85%, ceil 95%)
(52, 31, 116, NULL, 4, 284000, 248000, 12.68, 'ONE_TIME', 1, DATE '2026-04-28', 'PROPOSED', 'CLAUDE_AI', 'HOLD',
 'After LC bundle success, agent approached HDFC account. HOLD strategy — customer still processing LC deal.',
 NULL, TIMESTAMP '2026-04-21 15:30:00', NULL),

-- Customer 32: Priya Agarwal — BUNDLE 3 HDFC accounts (117+118+119, o/s 512k+364k+568k = 1444k)
-- HDFC DPD_90 floor 70%; HDFC NPA CC floor 50%; HDFC DPD_60 floor 85%
(53, 32, 117, NULL, 4, 1444000, 1008000, 30.19, 'EMI_6', 6, DATE '2026-04-30', 'ACCEPTED', 'CLAUDE_AI', 'BUNDLE',
 'BUNDLE: 3 HDFC accounts (PL+CC+AL). Combined ₹14.44L. Blended 30% discount. 6 EMIs of ₹1,68,000. All guardrails met.',
 'Customer relieved. Long pending — accepted immediately. EMI mandate signed.', TIMESTAMP '2026-04-22 10:00:00', TIMESTAMP '2026-04-22 16:00:00'),

-- Customer 32: ICICI separate (120, o/s 318000, ICICI DPD_60 policy31: floor 75%, ceil 95%)
(54, 32, 120, NULL, 5, 318000, 258000, 18.87, 'EMI_3',  3, DATE '2026-05-05', 'PROPOSED', 'CLAUDE_AI', 'REFRAME_INSTALLMENTS',
 'Post HDFC bundle, customer approached ICICI. PARTIAL_WILLINGNESS — offered 3 EMIs.',
 NULL, TIMESTAMP '2026-04-22 17:00:00', NULL),

-- Customer 33: Santosh Kulkarni — BUNDLE 2 LC accounts (121+122, o/s 356k+162k = 518k)
(55, 33, 121, NULL, 2, 518000, 364000, 29.73, 'EMI_6',  6, DATE '2026-04-24', 'ACCEPTED', 'CLAUDE_AI', 'BUNDLE',
 'BUNDLE: LC PL (DPD_90) + LC CC (NPA). Combined ₹5.18L. 6 EMIs of ₹60,667. LC NPA CC floor 45% = ₹72,900 — met.',
 'Customer agreed. Two LC accounts closing together.', TIMESTAMP '2026-04-18 11:00:00', TIMESTAMP '2026-04-19 10:00:00'),

-- Customer 33: BUNDLE 2 ICICI accounts (123+124, o/s 294k+198k = 492k)
(56, 33, 123, NULL, 5, 492000, 350000, 28.86, 'EMI_3',  3, DATE '2026-05-10', 'PROPOSED', 'CLAUDE_AI', 'BUNDLE',
 'BUNDLE: ICICI PL + ICICI CC. Combined ₹4.92L. 3 EMIs of ₹1,16,667. ICICI DPD_90 floor 60% = ₹1,76,400 — met.',
 NULL, TIMESTAMP '2026-04-22 11:00:00', NULL),

-- Customer 34: Meghna Rao — BUNDLE 3 ICICI accounts (126+127+128, o/s 392k+274k+504k = 1170k)
(57, 34, 126, NULL, 5, 1170000, 820000, 29.91, 'EMI_6', 6, DATE '2026-04-28', 'ACCEPTED', 'CLAUDE_AI', 'BUNDLE',
 'BUNDLE: 3 ICICI accounts (PL+CC+AL). Combined ₹11.7L. 6 EMIs of ₹1,36,667. ICICI NPA CC floor 45% = ₹1,23,300 — met.',
 'Customer thrilled. Wanted to close all ICICI accounts together. Mandate submitted.', TIMESTAMP '2026-04-20 11:00:00', TIMESTAMP '2026-04-21 09:30:00'),

-- Customer 34: Individual HDFC follow-up (130, o/s 391000, HDFC DPD_30 policy20: floor 80%, ceil 100%)
(58, 34, 130, NULL, 3, 391000, 322000, 17.65, 'ONE_TIME', 1, DATE '2026-05-02', 'PROPOSED', 'CLAUDE_AI', 'HOLD',
 'After ICICI bundle, HDFC DPD_30 account. HOLD — offer 82.4% recovery, within DPD_30 range.',
 NULL, TIMESTAMP '2026-04-21 10:00:00', NULL),

-- Customer 35: Ajay Mishra — BUNDLE 3 HDFC accounts (131+132+133, o/s 242k+183k+318k = 743k)
(59, 35, 131, NULL, 3, 743000, 520000, 30.01, 'EMI_6',  6, DATE '2026-04-26', 'REJECTED', 'CLAUDE_AI', 'BUNDLE',
 'BUNDLE: HDFC PL + CC + AL. ₹7.43L combined. 6 EMIs of ₹86,667. Customer rejected — wanted more discount.',
 'Customer insisted on 40% off. BUNDLE price is best available.', TIMESTAMP '2026-04-15 14:00:00', TIMESTAMP '2026-04-16 11:00:00'),

(60, 35, 131, NULL, 3, 743000, 490000, 34.05, 'EMI_6',  6, DATE '2026-05-05', 'PROPOSED', 'CLAUDE_AI', 'BUNDLE',
 'Revised BUNDLE after rejection. Moved 4% lower, still above all floors. 6 EMIs of ₹81,667.',
 NULL, TIMESTAMP '2026-04-22 10:00:00', NULL),

-- Customer 36: Shruti Patel — BUNDLE 2 Kotak accounts (135+136, o/s 322k+228k = 550k)
(61, 36, 135, NULL, 6, 550000, 392000, 28.73, 'EMI_3',  3, DATE '2026-04-24', 'ACCEPTED', 'CLAUDE_AI', 'BUNDLE',
 'BUNDLE: Kotak PL (DPD_90) + Kotak CC (NPA). Combined ₹5.5L. 3 EMIs of ₹1,30,667. Kotak NPA floor via policy40 = 82% but specific DPD_90 policy41 = 75%. Floor respected.',
 'Customer happy. Both Kotak accounts settling together.', TIMESTAMP '2026-04-18 10:00:00', TIMESTAMP '2026-04-19 14:00:00'),

-- Customer 36: LC accounts already open (137+138 — agent rejected static, AI proposed installments)
(62, 36, 138, NULL, 1, 144000, 106000, 26.39, 'ONE_TIME', 1, DATE '2026-04-10', 'REJECTED', 'AGENT', 'HOLD',
 'Static opener on LC CC DPD_90. Floor LC CC DPD_90 = 55% = ₹79,200. Offered ₹1.06L.',
 'Customer already dealing with Kotak bundle. Said to call back.', TIMESTAMP '2026-04-05 11:00:00', TIMESTAMP '2026-04-05 11:30:00'),

-- Customer 37: Naresh Bhat — BUNDLE 2 HDFC accounts (142+143, o/s 386k+320k = 706k)
(63, 37, 142, NULL, 4, 706000, 515000, 27.05, 'EMI_6',  6, DATE '2026-04-28', 'ACCEPTED', 'CLAUDE_AI', 'BUNDLE',
 'BUNDLE: HDFC PL + HDFC CC. Combined ₹7.06L. 6 EMIs of ₹85,833. HDFC DPD_90 floor 70% = ₹2,70,200 — met.',
 'Customer accepted. Wants to keep LC Home Loan separate (lower DPD).', TIMESTAMP '2026-04-20 14:00:00', TIMESTAMP '2026-04-21 10:30:00'),

-- Customer 37: LC PL DPD_90 individual (140, o/s 472000, LC DPD_90 policy4: floor 65%, ceil 85%)
(64, 37, 140, NULL, 1, 472000, 358000, 24.15, 'EMI_3',  3, DATE '2026-04-22', 'REJECTED', 'AGENT', 'LOWER',
 'Manual 24% discount on LC PL DPD_90. Floor 65% = ₹3,06,800. Offered ₹3.58L recovery.',
 'Customer wants it bundled with Home Loan — not possible (different DPD buckets).', TIMESTAMP '2026-04-16 10:00:00', TIMESTAMP '2026-04-16 10:40:00'),

-- Customer 38: Geeta Sharma — BUNDLE 2 LC accounts (145+146, o/s 316k+228k = 544k)
(65, 38, 145, NULL, 2, 544000, 390000, 28.31, 'EMI_3',  3, DATE '2026-04-25', 'ACCEPTED', 'CLAUDE_AI', 'BUNDLE',
 'BUNDLE: LC PL (DPD_60) + LC CC (DPD_90). Combined ₹5.44L. 3 EMIs of ₹1,30,000. LC DPD_90 CC floor 55% = ₹1,25,400 — met.',
 'Customer happy to close both LC accounts. Confirmed EMIs.', TIMESTAMP '2026-04-19 11:00:00', TIMESTAMP '2026-04-20 09:00:00'),

-- Customer 38: HDFC bundle (147+148, o/s 432k+275k = 707k)
(66, 38, 147, NULL, 4, 707000, 498000, 29.56, 'EMI_6',  6, DATE '2026-05-05', 'PROPOSED', 'CLAUDE_AI', 'BUNDLE',
 'BUNDLE: HDFC PL (DPD_90) + HDFC CC (NPA). Combined ₹7.07L. 6 EMIs of ₹83,000. HDFC NPA CC floor 50% = ₹1,37,500 — met.',
 NULL, TIMESTAMP '2026-04-22 11:00:00', NULL),

-- Customer 39: Vijay Menon — BUNDLE 3 ICICI accounts (150+151+152, o/s 318k+182k+390k = 890k)
(67, 39, 150, NULL, 5, 890000, 630000, 29.21, 'EMI_6',  6, DATE '2026-04-27', 'ACCEPTED', 'CLAUDE_AI', 'BUNDLE',
 'BUNDLE: 3 ICICI accounts (PL+CC+AL). Combined ₹8.9L. 6 EMIs of ₹1,05,000. ICICI DPD_90 CC floor 60% = ₹1,09,200 — met.',
 'Customer excited. All ICICI accounts settling in one call. Mandate signed.', TIMESTAMP '2026-04-21 10:00:00', TIMESTAMP '2026-04-21 16:30:00'),

-- Customer 39: LC individual follow-up (153, o/s 237000, LC DPD_90 policy4: floor 65%, ceil 85%)
(68, 39, 153, NULL, 1, 237000, 172000, 27.43, 'EMI_3',  3, DATE '2026-05-02', 'PROPOSED', 'CLAUDE_AI', 'LOWER',
 'Post ICICI bundle, LC PL DPD_90. 3 EMIs of ₹57,333. Floor 65% = ₹1,54,050. Offer 72.6%.',
 NULL, TIMESTAMP '2026-04-21 17:00:00', NULL),

-- Customer 40: Amita Gupta — BUNDLE 2 LC accounts (155+156, o/s 278k+172k = 450k)
(69, 40, 155, NULL, 2, 450000, 320000, 28.89, 'EMI_3',  3, DATE '2026-04-24', 'ACCEPTED', 'CLAUDE_AI', 'BUNDLE',
 'BUNDLE: LC PL (DPD_60) + LC CC (DPD_90). Combined ₹4.5L. 3 EMIs of ₹1,06,667. LC DPD_90 CC floor 55% = ₹94,600 — met.',
 'Customer happy to clear both LC accounts together. First installment paid.', TIMESTAMP '2026-04-18 14:00:00', TIMESTAMP '2026-04-19 11:00:00');


-- ============================================================
-- Reset sequences — above IDs go up to 159 (accounts) & 69 (settlements)
-- ============================================================
ALTER TABLE lender               ALTER COLUMN id RESTART WITH 10;
ALTER TABLE field_agent          ALTER COLUMN id RESTART WITH 10;
ALTER TABLE customer             ALTER COLUMN id RESTART WITH 300;
ALTER TABLE account              ALTER COLUMN id RESTART WITH 300;
ALTER TABLE transcript           ALTER COLUMN id RESTART WITH 300;
ALTER TABLE chat_message         ALTER COLUMN id RESTART WITH 300;
ALTER TABLE transcript_history   ALTER COLUMN id RESTART WITH 300;
ALTER TABLE settlement           ALTER COLUMN id RESTART WITH 300;
ALTER TABLE negotiation_policy   ALTER COLUMN id RESTART WITH 300;
ALTER TABLE negotiation_strategy ALTER COLUMN id RESTART WITH 300;
