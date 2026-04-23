package com.offerdyne.strategy;

import com.offerdyne.entity.Account;
import com.offerdyne.entity.Lender;
import com.offerdyne.entity.NegotiationSession;
import com.offerdyne.nlp.NlpAnalysis;

import java.math.BigDecimal;
import java.util.List;

/** Transient state passed to the StrategyEngine for each decision tick. */
public class NegotiationContext {
    public Lender lender;
    public NegotiationSession session;
    public Account anchorAccount;
    public List<Account> customerAccounts;   // for BUNDLE detection
    public NlpAnalysis latestSignal;
    public BigDecimal currentOfferPercent;   // last offer made
    public int consecutiveRejections;
    public int turnIndex;
    public boolean isBundled;
}
