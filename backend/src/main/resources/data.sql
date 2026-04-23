-- =====================================================================
-- OfferDyne sample seed data (H2)
-- 1 lender (org), 4 field agents, 5 customers, 9 accounts,
-- 2 historical sessions with transcripts, 1 settled account.
-- =====================================================================

-- ----- LENDER (the single org) -----
INSERT INTO LENDER (lender_name, lender_code, floor_percent, ceiling_percent, max_installments, min_installment_amt, bundling_allowed, active)
VALUES ('OfferDyne Finance Ltd', 'OFFDYNE', 35.00, 70.00, 3, 1000.00, TRUE, TRUE);

-- ----- LENDER_POLICY (per product) -----
INSERT INTO LENDER_POLICY (lender_id, product_type, floor_percent, ceiling_percent, max_installments, bundling_allowed, active)
VALUES (1, 'PERSONAL_LOAN', 35.00, 70.00, 3, TRUE, TRUE);
INSERT INTO LENDER_POLICY (lender_id, product_type, floor_percent, ceiling_percent, max_installments, bundling_allowed, active)
VALUES (1, 'CREDIT_CARD',   40.00, 75.00, 3, TRUE, TRUE);
INSERT INTO LENDER_POLICY (lender_id, product_type, floor_percent, ceiling_percent, max_installments, bundling_allowed, active)
VALUES (1, 'BNPL',          45.00, 80.00, 2, TRUE, TRUE);

-- ----- FIELD_AGENTS -----
INSERT INTO FIELD_AGENT (lender_id, agent_code, agent_name, agent_email, agent_phone, role, active)
VALUES (1, 'AG001', 'Riya Sharma',   'riya@offerdyne.ai',   '+91-9999000001', 'AGENT',      TRUE);
INSERT INTO FIELD_AGENT (lender_id, agent_code, agent_name, agent_email, agent_phone, role, active)
VALUES (1, 'AG002', 'Arjun Mehta',   'arjun@offerdyne.ai',  '+91-9999000002', 'AGENT',      TRUE);
INSERT INTO FIELD_AGENT (lender_id, agent_code, agent_name, agent_email, agent_phone, role, active)
VALUES (1, 'AG003', 'Priya Iyer',    'priya@offerdyne.ai',  '+91-9999000003', 'AGENT',      TRUE);
INSERT INTO FIELD_AGENT (lender_id, agent_code, agent_name, agent_email, agent_phone, role, active)
VALUES (1, 'AG004', 'Vikram Rao',    'vikram@offerdyne.ai', '+91-9999000004', 'SUPERVISOR', TRUE);

-- ----- CUSTOMERS -----
INSERT INTO CUSTOMER (customer_code, customer_name, phone, email, employment_status, income_band, credit_score, risk_segment)
VALUES ('CU1001', 'Rahul Verma',     '+91-9811100001', 'rahul.v@example.com',   'EMPLOYED',      'MID',  680, 'MED_RISK');
INSERT INTO CUSTOMER (customer_code, customer_name, phone, email, employment_status, income_band, credit_score, risk_segment)
VALUES ('CU1002', 'Sneha Kapoor',    '+91-9811100002', 'sneha.k@example.com',   'SELF_EMPLOYED', 'MID',  640, 'MED_RISK');
INSERT INTO CUSTOMER (customer_code, customer_name, phone, email, employment_status, income_band, credit_score, risk_segment)
VALUES ('CU1003', 'Aditya Nair',     '+91-9811100003', 'aditya.n@example.com',  'UNEMPLOYED',    'LOW',  560, 'HIGH_RISK');
INSERT INTO CUSTOMER (customer_code, customer_name, phone, email, employment_status, income_band, credit_score, risk_segment)
VALUES ('CU1004', 'Meera Joshi',     '+91-9811100004', 'meera.j@example.com',   'EMPLOYED',      'HIGH', 740, 'LOW_RISK');
INSERT INTO CUSTOMER (customer_code, customer_name, phone, email, employment_status, income_band, credit_score, risk_segment)
VALUES ('CU1005', 'Karan Singh',     '+91-9811100005', 'karan.s@example.com',   'EMPLOYED',      'MID',  620, 'MED_RISK');

-- ----- ACCOUNTS (1 customer -> many accounts to cover BUNDLE scenario) -----
-- Rahul has 3 accounts (bundle candidate)
INSERT INTO ACCOUNT (account_number, customer_id, lender_id, product_type, principal_amount, outstanding_amount, dpd, account_status)
VALUES ('ACC-50001', 1, 1, 'PERSONAL_LOAN', 200000.00, 180000.00, 120, 'DELINQUENT');
INSERT INTO ACCOUNT (account_number, customer_id, lender_id, product_type, principal_amount, outstanding_amount, dpd, account_status)
VALUES ('ACC-50002', 1, 1, 'CREDIT_CARD',    80000.00,  95000.00,  90, 'DELINQUENT');
INSERT INTO ACCOUNT (account_number, customer_id, lender_id, product_type, principal_amount, outstanding_amount, dpd, account_status)
VALUES ('ACC-50003', 1, 1, 'BNPL',           25000.00,  27000.00,  60, 'DELINQUENT');

-- Sneha has 2 accounts
INSERT INTO ACCOUNT (account_number, customer_id, lender_id, product_type, principal_amount, outstanding_amount, dpd, account_status)
VALUES ('ACC-50010', 2, 1, 'PERSONAL_LOAN', 150000.00, 140000.00, 150, 'DELINQUENT');
INSERT INTO ACCOUNT (account_number, customer_id, lender_id, product_type, principal_amount, outstanding_amount, dpd, account_status)
VALUES ('ACC-50011', 2, 1, 'CREDIT_CARD',    60000.00,  70000.00, 100, 'DELINQUENT');

-- Aditya - 1 account, unemployed (affordability case)
INSERT INTO ACCOUNT (account_number, customer_id, lender_id, product_type, principal_amount, outstanding_amount, dpd, account_status)
VALUES ('ACC-50020', 3, 1, 'PERSONAL_LOAN', 100000.00, 110000.00, 180, 'DELINQUENT');

-- Meera - 1 account already settled (history)
INSERT INTO ACCOUNT (account_number, customer_id, lender_id, product_type, principal_amount, outstanding_amount, dpd, account_status)
VALUES ('ACC-50030', 4, 1, 'CREDIT_CARD',    50000.00,      0.00,   0, 'SETTLED');

-- Karan - 2 accounts
INSERT INTO ACCOUNT (account_number, customer_id, lender_id, product_type, principal_amount, outstanding_amount, dpd, account_status)
VALUES ('ACC-50040', 5, 1, 'PERSONAL_LOAN', 120000.00, 115000.00, 75,  'DELINQUENT');
INSERT INTO ACCOUNT (account_number, customer_id, lender_id, product_type, principal_amount, outstanding_amount, dpd, account_status)
VALUES ('ACC-50041', 5, 1, 'BNPL',           15000.00,  16500.00, 45,  'DELINQUENT');

-- ----- HISTORICAL NEGOTIATION SESSION (already closed) -----
-- Session 1: Meera accepted 55% lump sum on her credit card
INSERT INTO NEGOTIATION_SESSION
  (customer_id, account_id, agent_id, lender_id, session_status, started_at, ended_at,
   initial_offer_percent, final_offer_percent, final_offer_amount, strategy_sequence, bundle_flag, turn_count)
VALUES
  (4, 7, 1, 1, 'ACCEPTED', TIMESTAMP '2026-03-10 10:00:00', TIMESTAMP '2026-03-10 10:14:00',
   70.00, 55.00, 27500.00, 'HOLD,LOWER', FALSE, 6);

-- Transcripts for session 1
INSERT INTO TRANSCRIPT (session_id, customer_id, account_id, turn_index, speaker, utterance, sentiment, sentiment_score, objection_type)
VALUES (1, 4, 7, 0, 'AGENT',    'Hello Meera, we can close your card at 70% today.',                'NEUTRAL',     0.000, 'NONE');
INSERT INTO TRANSCRIPT (session_id, customer_id, account_id, turn_index, speaker, utterance, sentiment, sentiment_score, objection_type)
VALUES (1, 4, 7, 1, 'BORROWER', '70% is too high for me right now.',                                'NEGATIVE',   -0.400, 'AFFORDABILITY');
INSERT INTO TRANSCRIPT (session_id, customer_id, account_id, turn_index, speaker, utterance, sentiment, sentiment_score, objection_type)
VALUES (1, 4, 7, 2, 'AGENT',    'I hear you. Let me hold and check the best number for you.',       'NEUTRAL',     0.100, 'NONE');
INSERT INTO TRANSCRIPT (session_id, customer_id, account_id, turn_index, speaker, utterance, sentiment, sentiment_score, objection_type)
VALUES (1, 4, 7, 3, 'BORROWER', 'I want to close this, just not at that price.',                    'COOPERATIVE', 0.200, 'WILLINGNESS');
INSERT INTO TRANSCRIPT (session_id, customer_id, account_id, turn_index, speaker, utterance, sentiment, sentiment_score, objection_type)
VALUES (1, 4, 7, 4, 'AGENT',    'Best I can do is 55% as a one-time settlement.',                   'NEUTRAL',     0.000, 'NONE');
INSERT INTO TRANSCRIPT (session_id, customer_id, account_id, turn_index, speaker, utterance, sentiment, sentiment_score, objection_type)
VALUES (1, 4, 7, 5, 'BORROWER', 'Okay, 55% works. Please send the letter.',                         'POSITIVE',    0.700, 'ACCEPTANCE');

-- Offers log for session 1 (proves guardrail compliance)
INSERT INTO OFFER (session_id, turn_index, strategy, offer_percent, offer_amount, framing_text, guardrail_check_passed, accepted)
VALUES (1, 0, 'HOLD',  70.00, 35000.00, 'Opening offer at ceiling',                TRUE, FALSE);
INSERT INTO OFFER (session_id, turn_index, strategy, offer_percent, offer_amount, framing_text, guardrail_check_passed, accepted)
VALUES (1, 2, 'HOLD',  70.00, 35000.00, 'Holding the offer; adding urgency',       TRUE, FALSE);
INSERT INTO OFFER (session_id, turn_index, strategy, offer_percent, offer_amount, framing_text, guardrail_check_passed, accepted)
VALUES (1, 4, 'LOWER', 55.00, 27500.00, 'Lowered 15% given affordability signal',  TRUE, TRUE);

INSERT INTO SETTLEMENT (session_id, customer_id, account_id, agent_id, lender_id, settled_percent, settled_amount, settlement_type, installment_count, status)
VALUES (1, 4, 7, 1, 1, 55.00, 27500.00, 'LUMP_SUM', 1, 'COMPLETED');

-- ----- HISTORICAL SESSION 2: Rahul bundle attempt, rejected -----
INSERT INTO NEGOTIATION_SESSION
  (customer_id, account_id, agent_id, lender_id, session_status, started_at, ended_at,
   initial_offer_percent, final_offer_percent, final_offer_amount, strategy_sequence, bundle_flag, turn_count)
VALUES
  (1, 1, 2, 1, 'REJECTED', TIMESTAMP '2026-04-01 11:00:00', TIMESTAMP '2026-04-01 11:12:00',
   65.00, 45.00, 136800.00, 'HOLD,LOWER,BUNDLE', TRUE, 5);

INSERT INTO SESSION_ACCOUNT_MAP (session_id, account_id) VALUES (2, 1);
INSERT INTO SESSION_ACCOUNT_MAP (session_id, account_id) VALUES (2, 2);
INSERT INTO SESSION_ACCOUNT_MAP (session_id, account_id) VALUES (2, 3);

INSERT INTO TRANSCRIPT (session_id, customer_id, account_id, turn_index, speaker, utterance, sentiment, sentiment_score, objection_type)
VALUES (2, 1, 1, 0, 'AGENT',    'Rahul, you have 3 accounts. Let us close them together.', 'NEUTRAL', 0.100, 'NONE');
INSERT INTO TRANSCRIPT (session_id, customer_id, account_id, turn_index, speaker, utterance, sentiment, sentiment_score, objection_type)
VALUES (2, 1, 1, 1, 'BORROWER', 'I just lost my job last month, nothing is possible now.', 'DISTRESSED', -0.700, 'JOB_LOSS');
INSERT INTO TRANSCRIPT (session_id, customer_id, account_id, turn_index, speaker, utterance, sentiment, sentiment_score, objection_type)
VALUES (2, 1, 1, 2, 'AGENT',    'Understood. Can we split into 3 installments across all 3 accounts?', 'NEUTRAL', 0.000, 'NONE');
INSERT INTO TRANSCRIPT (session_id, customer_id, account_id, turn_index, speaker, utterance, sentiment, sentiment_score, objection_type)
VALUES (2, 1, 1, 3, 'BORROWER', 'Sorry, I cannot commit to anything this month.', 'NEGATIVE', -0.500, 'TIMING');
INSERT INTO TRANSCRIPT (session_id, customer_id, account_id, turn_index, speaker, utterance, sentiment, sentiment_score, objection_type)
VALUES (2, 1, 1, 4, 'AGENT',    'Noted. I will reach out next month. Take care.', 'NEUTRAL', 0.000, 'NONE');

INSERT INTO OFFER (session_id, turn_index, strategy, offer_percent, offer_amount, framing_text, guardrail_check_passed, accepted)
VALUES (2, 0, 'BUNDLE',               65.00, 196300.00, 'Bundle 3 accounts at 65%',            TRUE, FALSE);
INSERT INTO OFFER (session_id, turn_index, strategy, offer_percent, offer_amount, framing_text, guardrail_check_passed, accepted)
VALUES (2, 2, 'REFRAME_INSTALLMENTS', 45.00, 136800.00, '3 monthly installments bundle',      TRUE, FALSE);
