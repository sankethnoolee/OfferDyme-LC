-- =====================================================================
-- OfferDyne - Dynamic Settlement Optimizer
-- H2 Database Schema (Problem #8)
-- =====================================================================
-- Covers:
--   * 1 Lender (org) owning many accounts / policies
--   * Multiple field agents in the org
--   * 1 Customer -> many Accounts (enables "bundle" strategy)
--   * Transcripts: turn-by-turn rows per session, linked to customer + account
--   * Transcript_History: audit trail of every insert/update/delete
--   * Settlements: linked to customer + account (final outcome)
--   * Offers: every offer made in a session (for guardrail audit)
-- =====================================================================

DROP TABLE IF EXISTS TRANSCRIPT_HISTORY;
DROP TABLE IF EXISTS TRANSCRIPT;
DROP TABLE IF EXISTS OFFER;
DROP TABLE IF EXISTS SETTLEMENT;
DROP TABLE IF EXISTS SESSION_ACCOUNT_MAP;
DROP TABLE IF EXISTS NEGOTIATION_SESSION;
DROP TABLE IF EXISTS ACCOUNT;
DROP TABLE IF EXISTS CUSTOMER;
DROP TABLE IF EXISTS LENDER_POLICY;
DROP TABLE IF EXISTS FIELD_AGENT;
DROP TABLE IF EXISTS LENDER;

-- ---------------------------------------------------------------------
-- LENDER: the organization running the collections process
-- ---------------------------------------------------------------------
CREATE TABLE LENDER (
    lender_id            BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
    lender_name          VARCHAR(120)  NOT NULL,
    lender_code          VARCHAR(30)   NOT NULL UNIQUE,
    floor_percent        DECIMAL(5,2)  NOT NULL,  -- e.g. 35.00
    ceiling_percent      DECIMAL(5,2)  NOT NULL,  -- e.g. 70.00
    max_installments     INT           NOT NULL DEFAULT 3,
    min_installment_amt  DECIMAL(12,2) NOT NULL DEFAULT 1000.00,
    bundling_allowed     BOOLEAN       NOT NULL DEFAULT TRUE,
    active               BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at           TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ---------------------------------------------------------------------
-- FIELD_AGENT: collection officers inside the org
-- ---------------------------------------------------------------------
CREATE TABLE FIELD_AGENT (
    agent_id       BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
    lender_id      BIGINT        NOT NULL,
    agent_code     VARCHAR(30)   NOT NULL UNIQUE,
    agent_name     VARCHAR(120)  NOT NULL,
    agent_email    VARCHAR(150),
    agent_phone    VARCHAR(20),
    role           VARCHAR(30)   NOT NULL DEFAULT 'AGENT', -- AGENT / SUPERVISOR / ADMIN
    active         BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_agent_lender FOREIGN KEY (lender_id) REFERENCES LENDER(lender_id)
);

-- ---------------------------------------------------------------------
-- LENDER_POLICY: flexible per-product policy (optional extension)
-- ---------------------------------------------------------------------
CREATE TABLE LENDER_POLICY (
    policy_id        BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
    lender_id        BIGINT        NOT NULL,
    product_type     VARCHAR(50)   NOT NULL, -- e.g. PERSONAL_LOAN, CREDIT_CARD
    floor_percent    DECIMAL(5,2)  NOT NULL,
    ceiling_percent  DECIMAL(5,2)  NOT NULL,
    max_installments INT           NOT NULL DEFAULT 3,
    bundling_allowed BOOLEAN       NOT NULL DEFAULT TRUE,
    active           BOOLEAN       NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_policy_lender FOREIGN KEY (lender_id) REFERENCES LENDER(lender_id)
);

-- ---------------------------------------------------------------------
-- CUSTOMER: a borrower. 1 customer can have many accounts.
-- ---------------------------------------------------------------------
CREATE TABLE CUSTOMER (
    customer_id        BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
    customer_code      VARCHAR(40)   NOT NULL UNIQUE,
    customer_name      VARCHAR(150)  NOT NULL,
    phone              VARCHAR(20),
    email              VARCHAR(150),
    employment_status  VARCHAR(40),           -- EMPLOYED / UNEMPLOYED / SELF_EMPLOYED / STUDENT
    income_band        VARCHAR(40),           -- LOW / MID / HIGH
    credit_score       INT,
    risk_segment       VARCHAR(30),           -- LOW_RISK / MED_RISK / HIGH_RISK
    created_at         TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ---------------------------------------------------------------------
-- ACCOUNT: a loan/card account. Linked to customer + lender.
-- Unsecured debt only per problem scope.
-- ---------------------------------------------------------------------
CREATE TABLE ACCOUNT (
    account_id           BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
    account_number       VARCHAR(40)   NOT NULL UNIQUE,
    customer_id          BIGINT        NOT NULL,
    lender_id            BIGINT        NOT NULL,
    product_type         VARCHAR(50)   NOT NULL, -- PERSONAL_LOAN / CREDIT_CARD / BNPL
    principal_amount     DECIMAL(14,2) NOT NULL,
    outstanding_amount   DECIMAL(14,2) NOT NULL,
    dpd                  INT           NOT NULL DEFAULT 0, -- days past due
    account_status       VARCHAR(30)   NOT NULL DEFAULT 'DELINQUENT', -- DELINQUENT / IN_NEGOTIATION / SETTLED / ESCALATED / WRITTEN_OFF
    opened_at            TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_account_customer FOREIGN KEY (customer_id) REFERENCES CUSTOMER(customer_id),
    CONSTRAINT fk_account_lender   FOREIGN KEY (lender_id)   REFERENCES LENDER(lender_id)
);

CREATE INDEX idx_account_customer ON ACCOUNT(customer_id);
CREATE INDEX idx_account_lender   ON ACCOUNT(lender_id);

-- ---------------------------------------------------------------------
-- NEGOTIATION_SESSION: one live conversation between an agent and borrower
-- Primary account = the account the session began on. If bundled, extra
-- accounts are added via SESSION_ACCOUNT_MAP.
-- ---------------------------------------------------------------------
CREATE TABLE NEGOTIATION_SESSION (
    session_id            BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
    customer_id           BIGINT        NOT NULL,
    account_id            BIGINT        NOT NULL,    -- anchor account
    agent_id              BIGINT        NOT NULL,
    lender_id             BIGINT        NOT NULL,
    session_status        VARCHAR(30)   NOT NULL DEFAULT 'ACTIVE', -- ACTIVE / ACCEPTED / REJECTED / ESCALATED / EXPIRED
    started_at            TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ended_at              TIMESTAMP,
    initial_offer_percent DECIMAL(5,2),
    final_offer_percent   DECIMAL(5,2),
    final_offer_amount    DECIMAL(14,2),
    strategy_sequence     VARCHAR(1000),             -- CSV of strategies used e.g. "HOLD,LOWER,REFRAME"
    bundle_flag           BOOLEAN       NOT NULL DEFAULT FALSE,
    turn_count            INT           NOT NULL DEFAULT 0,
    CONSTRAINT fk_sess_customer FOREIGN KEY (customer_id) REFERENCES CUSTOMER(customer_id),
    CONSTRAINT fk_sess_account  FOREIGN KEY (account_id)  REFERENCES ACCOUNT(account_id),
    CONSTRAINT fk_sess_agent    FOREIGN KEY (agent_id)    REFERENCES FIELD_AGENT(agent_id),
    CONSTRAINT fk_sess_lender   FOREIGN KEY (lender_id)   REFERENCES LENDER(lender_id)
);

CREATE INDEX idx_session_customer ON NEGOTIATION_SESSION(customer_id);
CREATE INDEX idx_session_agent    ON NEGOTIATION_SESSION(agent_id);

-- ---------------------------------------------------------------------
-- SESSION_ACCOUNT_MAP: M:N link for BUNDLE strategy
-- When agent bundles multiple accounts into one deal, all accounts are
-- mapped here with the anchor session.
-- ---------------------------------------------------------------------
CREATE TABLE SESSION_ACCOUNT_MAP (
    map_id       BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    session_id   BIGINT NOT NULL,
    account_id   BIGINT NOT NULL,
    added_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_map_session FOREIGN KEY (session_id) REFERENCES NEGOTIATION_SESSION(session_id),
    CONSTRAINT fk_map_account FOREIGN KEY (account_id) REFERENCES ACCOUNT(account_id),
    CONSTRAINT uq_session_account UNIQUE (session_id, account_id)
);

-- ---------------------------------------------------------------------
-- TRANSCRIPT: turn-by-turn rows in a session.
-- Links to customer + account (per user requirement) AND to session.
-- Stores sentiment + objection classification per utterance.
-- ---------------------------------------------------------------------
CREATE TABLE TRANSCRIPT (
    transcript_id    BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
    session_id       BIGINT        NOT NULL,
    customer_id      BIGINT        NOT NULL,
    account_id       BIGINT        NOT NULL,
    turn_index       INT           NOT NULL,      -- 0,1,2,...
    speaker          VARCHAR(20)   NOT NULL,      -- AGENT / BORROWER / SYSTEM
    utterance        CLOB          NOT NULL,
    sentiment        VARCHAR(30),                 -- POSITIVE / NEUTRAL / NEGATIVE / DISTRESSED / COOPERATIVE
    sentiment_score  DECIMAL(5,3),                -- -1.000 .. +1.000
    objection_type   VARCHAR(40),                 -- AFFORDABILITY / WILLINGNESS / TIMING / DISPUTE / JOB_LOSS / ACCEPTANCE / NONE
    signals_json     CLOB,                        -- free-form capacity signals etc.
    created_at       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tr_session  FOREIGN KEY (session_id)  REFERENCES NEGOTIATION_SESSION(session_id),
    CONSTRAINT fk_tr_customer FOREIGN KEY (customer_id) REFERENCES CUSTOMER(customer_id),
    CONSTRAINT fk_tr_account  FOREIGN KEY (account_id)  REFERENCES ACCOUNT(account_id),
    CONSTRAINT uq_transcript_turn UNIQUE (session_id, turn_index)
);

CREATE INDEX idx_tr_session  ON TRANSCRIPT(session_id);
CREATE INDEX idx_tr_customer ON TRANSCRIPT(customer_id);
CREATE INDEX idx_tr_account  ON TRANSCRIPT(account_id);

-- ---------------------------------------------------------------------
-- TRANSCRIPT_HISTORY: immutable audit trail.
-- Every INSERT/UPDATE/DELETE on TRANSCRIPT lands here for replay / debug.
-- ---------------------------------------------------------------------
CREATE TABLE TRANSCRIPT_HISTORY (
    history_id       BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
    transcript_id    BIGINT        NOT NULL,
    session_id       BIGINT        NOT NULL,
    customer_id      BIGINT        NOT NULL,
    account_id       BIGINT        NOT NULL,
    turn_index       INT           NOT NULL,
    speaker          VARCHAR(20)   NOT NULL,
    utterance        CLOB          NOT NULL,
    sentiment        VARCHAR(30),
    sentiment_score  DECIMAL(5,3),
    objection_type   VARCHAR(40),
    signals_json     CLOB,
    operation        VARCHAR(10)   NOT NULL,  -- INSERT / UPDATE / DELETE
    changed_by_agent BIGINT,
    archived_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_th_transcript ON TRANSCRIPT_HISTORY(transcript_id);
CREATE INDEX idx_th_session    ON TRANSCRIPT_HISTORY(session_id);

-- ---------------------------------------------------------------------
-- OFFER: every offer made during a session (agent-facing audit).
-- Used to prove 100% guardrail compliance.
-- ---------------------------------------------------------------------
CREATE TABLE OFFER (
    offer_id                BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
    session_id              BIGINT        NOT NULL,
    turn_index              INT           NOT NULL,
    strategy                VARCHAR(30)   NOT NULL,   -- HOLD / LOWER / REFRAME_INSTALLMENTS / BUNDLE / ESCALATE
    offer_percent           DECIMAL(5,2)  NOT NULL,
    offer_amount            DECIMAL(14,2) NOT NULL,
    framing_text            CLOB,
    installment_plan_json   CLOB,                     -- null unless strategy = REFRAME_INSTALLMENTS
    guardrail_check_passed  BOOLEAN       NOT NULL,
    guardrail_reason        VARCHAR(300),
    accepted                BOOLEAN,                  -- null = pending response
    made_at                 TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_offer_session FOREIGN KEY (session_id) REFERENCES NEGOTIATION_SESSION(session_id)
);

CREATE INDEX idx_offer_session ON OFFER(session_id);

-- ---------------------------------------------------------------------
-- SETTLEMENT: final accepted deal. Linked to customer + account.
-- For BUNDLE, there is one SETTLEMENT row per included account, all
-- sharing the same session_id and bundle_group_id.
-- ---------------------------------------------------------------------
CREATE TABLE SETTLEMENT (
    settlement_id      BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
    session_id         BIGINT        NOT NULL,
    customer_id        BIGINT        NOT NULL,
    account_id         BIGINT        NOT NULL,
    agent_id           BIGINT        NOT NULL,
    lender_id          BIGINT        NOT NULL,
    settled_percent    DECIMAL(5,2)  NOT NULL,
    settled_amount     DECIMAL(14,2) NOT NULL,
    settlement_type    VARCHAR(30)   NOT NULL, -- LUMP_SUM / INSTALLMENT / BUNDLED
    installment_count  INT           NOT NULL DEFAULT 1,
    installment_plan   CLOB,
    bundle_group_id    VARCHAR(40),            -- UUID shared across bundled rows
    status             VARCHAR(20)   NOT NULL DEFAULT 'PENDING', -- PENDING / COMPLETED / BROKEN
    settled_at         TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_st_session  FOREIGN KEY (session_id)  REFERENCES NEGOTIATION_SESSION(session_id),
    CONSTRAINT fk_st_customer FOREIGN KEY (customer_id) REFERENCES CUSTOMER(customer_id),
    CONSTRAINT fk_st_account  FOREIGN KEY (account_id)  REFERENCES ACCOUNT(account_id),
    CONSTRAINT fk_st_agent    FOREIGN KEY (agent_id)    REFERENCES FIELD_AGENT(agent_id),
    CONSTRAINT fk_st_lender   FOREIGN KEY (lender_id)   REFERENCES LENDER(lender_id)
);

CREATE INDEX idx_st_customer ON SETTLEMENT(customer_id);
CREATE INDEX idx_st_account  ON SETTLEMENT(account_id);
CREATE INDEX idx_st_bundle   ON SETTLEMENT(bundle_group_id);
