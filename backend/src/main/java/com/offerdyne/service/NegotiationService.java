package com.offerdyne.service;

import com.offerdyne.dto.ApiDtos;
import com.offerdyne.entity.*;
import com.offerdyne.nlp.NlpAnalysis;
import com.offerdyne.nlp.NlpService;
import com.offerdyne.repository.*;
import com.offerdyne.strategy.NegotiationContext;
import com.offerdyne.strategy.OfferDecision;
import com.offerdyne.strategy.StrategyEngine;
import com.offerdyne.strategy.StrategyType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Orchestrates a live negotiation: start session, process turn, accept/reject.
 * Persists every offer to OFFER (guardrail audit) and every utterance to
 * TRANSCRIPT (+ TRANSCRIPT_HISTORY via TranscriptService).
 */
@Service
public class NegotiationService {

    private final LenderRepository lenders;
    private final CustomerRepository customers;
    private final AccountRepository accounts;
    private final NegotiationSessionRepository sessions;
    private final SessionAccountMapRepository sessionAccountMap;
    private final OfferRepository offers;
    private final SettlementRepository settlements;
    private final TranscriptService transcriptService;
    private final NlpService nlp;
    private final StrategyEngine engine;

    public NegotiationService(LenderRepository lenders,
                              CustomerRepository customers,
                              AccountRepository accounts,
                              NegotiationSessionRepository sessions,
                              SessionAccountMapRepository sessionAccountMap,
                              OfferRepository offers,
                              SettlementRepository settlements,
                              TranscriptService transcriptService,
                              NlpService nlp,
                              StrategyEngine engine) {
        this.lenders = lenders;
        this.customers = customers;
        this.accounts = accounts;
        this.sessions = sessions;
        this.sessionAccountMap = sessionAccountMap;
        this.offers = offers;
        this.settlements = settlements;
        this.transcriptService = transcriptService;
        this.nlp = nlp;
        this.engine = engine;
    }

    @Transactional
    public ApiDtos.SessionResponse startSession(Long agentId, ApiDtos.StartSessionRequest req) {
        Account acct = accounts.findById(req.getAccountId())
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + req.getAccountId()));
        Customer cust = customers.findById(req.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + req.getCustomerId()));
        Lender lender = lenders.findById(acct.getLenderId())
                .orElseThrow(() -> new IllegalStateException("Lender missing for account"));

        NegotiationSession s = new NegotiationSession();
        s.setCustomerId(cust.getCustomerId());
        s.setAccountId(acct.getAccountId());
        s.setAgentId(agentId);
        s.setLenderId(lender.getLenderId());
        s.setSessionStatus("ACTIVE");
        s.setStartedAt(LocalDateTime.now());
        s.setBundleFlag(Boolean.TRUE.equals(req.getAttemptBundle()));
        s.setTurnCount(0);
        s.setInitialOfferPercent(lender.getCeilingPercent());
        s.setStrategySequence("");
        s = sessions.save(s);

        List<Long> bundled = new ArrayList<>();
        if (Boolean.TRUE.equals(req.getAttemptBundle())) {
            for (Account a : accounts.findByCustomerId(cust.getCustomerId())) {
                if ("DELINQUENT".equalsIgnoreCase(a.getAccountStatus())
                        || "IN_NEGOTIATION".equalsIgnoreCase(a.getAccountStatus())) {
                    SessionAccountMap m = new SessionAccountMap();
                    m.setSessionId(s.getSessionId());
                    m.setAccountId(a.getAccountId());
                    m.setAddedAt(LocalDateTime.now());
                    sessionAccountMap.save(m);
                    bundled.add(a.getAccountId());
                }
            }
        }

        // Log system/opening turn
        Transcript t0 = new Transcript();
        t0.setSessionId(s.getSessionId());
        t0.setCustomerId(cust.getCustomerId());
        t0.setAccountId(acct.getAccountId());
        t0.setTurnIndex(0);
        t0.setSpeaker("SYSTEM");
        t0.setUtterance("Session started. Opening at ceiling "
                + lender.getCeilingPercent() + "% of outstanding "
                + acct.getOutstandingAmount() + ".");
        t0.setSentiment("NEUTRAL");
        t0.setSentimentScore(BigDecimal.ZERO);
        t0.setObjectionType("NONE");
        transcriptService.save(t0, agentId, "INSERT");

        // Persist opening HOLD offer so audit is clean
        Offer openingOffer = new Offer();
        openingOffer.setSessionId(s.getSessionId());
        openingOffer.setTurnIndex(0);
        openingOffer.setStrategy(StrategyType.HOLD.name());
        openingOffer.setOfferPercent(lender.getCeilingPercent());
        openingOffer.setOfferAmount(acct.getOutstandingAmount()
                .multiply(lender.getCeilingPercent())
                .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP));
        openingOffer.setFramingText("Opening offer at ceiling.");
        openingOffer.setGuardrailCheckPassed(true);
        openingOffer.setGuardrailReason("WITHIN_BOUNDS");
        openingOffer.setAccepted(false);
        openingOffer.setMadeAt(LocalDateTime.now());
        offers.save(openingOffer);

        s.setStrategySequence("HOLD");
        s.setTurnCount(1);
        sessions.save(s);

        ApiDtos.SessionResponse resp = new ApiDtos.SessionResponse();
        resp.setSessionId(s.getSessionId());
        resp.setCustomerId(cust.getCustomerId());
        resp.setAccountId(acct.getAccountId());
        resp.setAgentId(agentId);
        resp.setStatus(s.getSessionStatus());
        resp.setInitialOfferPercent(s.getInitialOfferPercent());
        resp.setCurrentOfferPercent(openingOffer.getOfferPercent());
        resp.setTurnCount(s.getTurnCount());
        resp.setBundleFlag(s.getBundleFlag());
        resp.setBundledAccountIds(bundled);
        return resp;
    }

    @Transactional
    public ApiDtos.TurnResponse processTurn(Long sessionId, Long agentId, ApiDtos.TurnRequest req) {
        NegotiationSession s = sessions.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));
        if (!"ACTIVE".equals(s.getSessionStatus())) {
            throw new IllegalStateException("Session not active: " + s.getSessionStatus());
        }
        Account acct = accounts.findById(s.getAccountId()).orElseThrow();
        Lender lender = lenders.findById(s.getLenderId()).orElseThrow();
        List<Account> custAccts = accounts.findByCustomerId(s.getCustomerId());

        // 1) Record the borrower utterance + NLP
        NlpAnalysis analysis = nlp.analyze(req.getBorrowerUtterance());
        int nextTurn = (s.getTurnCount() == null ? 0 : s.getTurnCount());
        Transcript tBor = new Transcript();
        tBor.setSessionId(sessionId);
        tBor.setCustomerId(s.getCustomerId());
        tBor.setAccountId(s.getAccountId());
        tBor.setTurnIndex(nextTurn);
        tBor.setSpeaker("BORROWER");
        tBor.setUtterance(req.getBorrowerUtterance());
        tBor.setSentiment(analysis.getSentiment().name());
        tBor.setSentimentScore(analysis.getSentimentScore());
        tBor.setObjectionType(analysis.getObjectionType().name());
        transcriptService.save(tBor, agentId, "INSERT");

        // 2) Build NegotiationContext
        List<Offer> offerLog = offers.findBySessionIdOrderByMadeAtAsc(sessionId);
        BigDecimal currentPct = offerLog.isEmpty()
                ? lender.getCeilingPercent()
                : offerLog.get(offerLog.size() - 1).getOfferPercent();
        int rejections = 0;
        for (Offer o : offerLog) if (Boolean.FALSE.equals(o.getAccepted())) rejections++;

        NegotiationContext ctx = new NegotiationContext();
        ctx.lender = lender;
        ctx.session = s;
        ctx.anchorAccount = acct;
        ctx.customerAccounts = custAccts;
        ctx.latestSignal = analysis;
        ctx.currentOfferPercent = currentPct;
        ctx.consecutiveRejections = rejections;
        ctx.turnIndex = nextTurn;
        ctx.isBundled = Boolean.TRUE.equals(s.getBundleFlag());

        // 3) Decide
        OfferDecision decision = engine.decide(ctx);

        // If BUNDLE was selected, flip session flag + map all accounts
        if (decision.strategy == StrategyType.BUNDLE && !Boolean.TRUE.equals(s.getBundleFlag())) {
            s.setBundleFlag(true);
            for (Account a : custAccts) {
                if (!"SETTLED".equalsIgnoreCase(a.getAccountStatus())) {
                    SessionAccountMap m = new SessionAccountMap();
                    m.setSessionId(sessionId);
                    m.setAccountId(a.getAccountId());
                    m.setAddedAt(LocalDateTime.now());
                    sessionAccountMap.save(m);
                }
            }
        }

        // 4) Persist the new offer (always, even ESCALATE — audit trail)
        Offer o = new Offer();
        o.setSessionId(sessionId);
        o.setTurnIndex(nextTurn + 1);
        o.setStrategy(decision.strategy.name());
        o.setOfferPercent(decision.offerPercent);
        o.setOfferAmount(decision.offerAmount);
        o.setFramingText(decision.framingText);
        o.setInstallmentPlanJson(decision.installmentPlanJson);
        o.setGuardrailCheckPassed(decision.guardrailPassed);
        o.setGuardrailReason(decision.guardrailReason);
        o.setAccepted(false);
        o.setMadeAt(LocalDateTime.now());
        offers.save(o);

        // 5) Log agent utterance
        Transcript tAgent = new Transcript();
        tAgent.setSessionId(sessionId);
        tAgent.setCustomerId(s.getCustomerId());
        tAgent.setAccountId(s.getAccountId());
        tAgent.setTurnIndex(nextTurn + 1);
        tAgent.setSpeaker("AGENT");
        tAgent.setUtterance(decision.framingText);
        tAgent.setSentiment("NEUTRAL");
        tAgent.setSentimentScore(BigDecimal.ZERO);
        tAgent.setObjectionType("NONE");
        transcriptService.save(tAgent, agentId, "INSERT");

        // 6) Update session
        String seq = s.getStrategySequence() == null || s.getStrategySequence().isEmpty()
                ? decision.strategy.name()
                : s.getStrategySequence() + "," + decision.strategy.name();
        s.setStrategySequence(seq);
        s.setTurnCount(nextTurn + 2);
        s.setFinalOfferPercent(decision.offerPercent);
        s.setFinalOfferAmount(decision.offerAmount);
        if (decision.terminal) {
            s.setSessionStatus("ESCALATED");
            s.setEndedAt(LocalDateTime.now());
        }
        sessions.save(s);

        ApiDtos.TurnResponse resp = new ApiDtos.TurnResponse();
        resp.setSessionId(sessionId);
        resp.setTurnIndex(nextTurn + 1);
        resp.setBorrowerUtterance(req.getBorrowerUtterance());
        resp.setSentiment(analysis.getSentiment().name());
        resp.setSentimentScore(analysis.getSentimentScore());
        resp.setObjectionType(analysis.getObjectionType().name());
        resp.setStrategyChosen(decision.strategy.name());
        resp.setOfferPercent(decision.offerPercent);
        resp.setOfferAmount(decision.offerAmount);
        resp.setFramingText(decision.framingText);
        resp.setInstallmentPlanJson(decision.installmentPlanJson);
        resp.setGuardrailPassed(decision.guardrailPassed);
        resp.setGuardrailReason(decision.guardrailReason);
        resp.setTerminal(decision.terminal);
        return resp;
    }

    @Transactional
    public ApiDtos.SettlementDto acceptLastOffer(Long sessionId, Long agentId) {
        NegotiationSession s = sessions.findById(sessionId).orElseThrow();
        List<Offer> log = offers.findBySessionIdOrderByMadeAtAsc(sessionId);
        if (log.isEmpty()) throw new IllegalStateException("No offers to accept");
        Offer last = log.get(log.size() - 1);
        if (!Boolean.TRUE.equals(last.getGuardrailCheckPassed())) {
            // Extra defence-in-depth: refuse to settle on a clamped offer
            throw new IllegalStateException("Cannot accept: offer failed guardrail.");
        }
        last.setAccepted(true);
        offers.save(last);

        s.setSessionStatus("ACCEPTED");
        s.setEndedAt(LocalDateTime.now());
        s.setFinalOfferPercent(last.getOfferPercent());
        s.setFinalOfferAmount(last.getOfferAmount());
        sessions.save(s);

        String bundleId = null;
        List<Long> acctIds = new ArrayList<>();
        acctIds.add(s.getAccountId());
        if (Boolean.TRUE.equals(s.getBundleFlag())) {
            bundleId = UUID.randomUUID().toString();
            for (SessionAccountMap m : sessionAccountMap.findBySessionId(sessionId)) {
                if (!acctIds.contains(m.getAccountId())) acctIds.add(m.getAccountId());
            }
        }

        ApiDtos.SettlementDto head = null;
        for (Long accId : acctIds) {
            Account a = accounts.findById(accId).orElseThrow();
            Settlement st = new Settlement();
            st.setSessionId(sessionId);
            st.setCustomerId(s.getCustomerId());
            st.setAccountId(a.getAccountId());
            st.setAgentId(agentId);
            st.setLenderId(s.getLenderId());
            st.setSettledPercent(last.getOfferPercent());
            BigDecimal amt = a.getOutstandingAmount()
                    .multiply(last.getOfferPercent())
                    .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
            st.setSettledAmount(amt);
            if (last.getInstallmentPlanJson() != null) {
                st.setSettlementType("INSTALLMENT");
                int cnt = countInstallments(last.getInstallmentPlanJson());
                st.setInstallmentCount(cnt);
                st.setInstallmentPlan(last.getInstallmentPlanJson());
            } else if (bundleId != null) {
                st.setSettlementType("BUNDLED");
                st.setInstallmentCount(1);
            } else {
                st.setSettlementType("LUMP_SUM");
                st.setInstallmentCount(1);
            }
            st.setBundleGroupId(bundleId);
            st.setStatus("PENDING");
            st.setSettledAt(LocalDateTime.now());
            Settlement saved = settlements.save(st);

            a.setAccountStatus("SETTLED");
            accounts.save(a);

            if (head == null) head = toDto(saved);
        }
        return head;
    }

    @Transactional
    public void rejectAndEnd(Long sessionId) {
        NegotiationSession s = sessions.findById(sessionId).orElseThrow();
        s.setSessionStatus("REJECTED");
        s.setEndedAt(LocalDateTime.now());
        sessions.save(s);
    }

    private int countInstallments(String json) {
        if (json == null) return 1;
        int count = 0;
        int i = 0;
        while ((i = json.indexOf("\"installment\"", i)) >= 0) { count++; i++; }
        return Math.max(1, count);
    }

    private ApiDtos.SettlementDto toDto(Settlement s) {
        ApiDtos.SettlementDto d = new ApiDtos.SettlementDto();
        d.setSettlementId(s.getSettlementId());
        d.setSessionId(s.getSessionId());
        d.setCustomerId(s.getCustomerId());
        d.setAccountId(s.getAccountId());
        d.setSettledPercent(s.getSettledPercent());
        d.setSettledAmount(s.getSettledAmount());
        d.setSettlementType(s.getSettlementType());
        d.setInstallmentCount(s.getInstallmentCount());
        d.setBundleGroupId(s.getBundleGroupId());
        d.setStatus(s.getStatus());
        d.setSettledAt(s.getSettledAt());
        return d;
    }

    public Optional<NegotiationSession> getSession(Long id) { return sessions.findById(id); }
}
