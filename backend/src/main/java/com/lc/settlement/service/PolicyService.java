package com.lc.settlement.service;

import com.lc.settlement.entity.Account;
import com.lc.settlement.entity.NegotiationPolicy;
import com.lc.settlement.entity.NegotiationStrategy;
import com.lc.settlement.repository.NegotiationPolicyRepository;
import com.lc.settlement.repository.NegotiationStrategyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Resolves the bank-configured negotiation policy (floor/ceiling range)
 * and the list of active strategies that Claude may pick from.
 *
 * Most-specific-wins resolution:
 *   - a row with both productType and dpdBucket set scores highest
 *   - then (productType set, dpdBucket null) and vice-versa
 *   - finally the fallback row with both null
 *   - ties broken by `priority` (higher wins)
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PolicyService {

    private final NegotiationPolicyRepository policyRepo;
    private final NegotiationStrategyRepository strategyRepo;

    public List<NegotiationPolicy> listPolicies(Long lenderId) {
        return policyRepo.findByLenderId(lenderId);
    }

    public List<NegotiationStrategy> listStrategies(Long lenderId) {
        return strategyRepo.findByLenderIdOrderByPriorityAsc(lenderId);
    }

    public List<NegotiationStrategy> activeStrategies(Long lenderId) {
        return strategyRepo.findByLenderIdAndActiveTrueOrderByPriorityAsc(lenderId);
    }

    public NegotiationPolicy save(NegotiationPolicy p) {
        return policyRepo.save(p);
    }

    public NegotiationStrategy save(NegotiationStrategy s) {
        return strategyRepo.save(s);
    }

    public void deletePolicy(Long id) { policyRepo.deleteById(id); }
    public void deleteStrategy(Long id) { strategyRepo.deleteById(id); }

    /**
     * For a given account, find the best-matching active policy.
     */
    public NegotiationPolicy resolveForAccount(Account account) {
        if (account == null || account.getCustomer() == null) return null;
        Long lenderId = lenderIdFor(account);
        return resolve(lenderId, account.getProductType(), account.getDpdBucket());
    }

    /**
     * For a portfolio (list of accounts), use the WORST DPD bucket across
     * the accounts and product=null (mixed portfolio). The accounts must
     * already share the same lender — this policy is derived from that lender.
     */
    public NegotiationPolicy resolveForPortfolio(List<Account> accounts) {
        if (accounts == null || accounts.isEmpty()) return null;
        Long lenderId = lenderIdFor(accounts.get(0));
        String worstBucket = worstDpdBucket(accounts);
        return resolve(lenderId, null, worstBucket);
    }

    /** Public accessor so other services can resolve the effective lender for an account. */
    public Long lenderIdForAccount(Account a) {
        return lenderIdFor(a);
    }

    public NegotiationPolicy resolve(Long lenderId, String productType, String dpdBucket) {
        if (lenderId == null) return null;
        List<NegotiationPolicy> all = policyRepo.findByLenderIdAndActiveTrue(lenderId);
        return all.stream()
                .filter(p -> productMatches(p, productType) && dpdMatches(p, dpdBucket))
                .max(Comparator.comparingInt(p -> score(p)))
                .orElse(null);
    }

    private boolean productMatches(NegotiationPolicy p, String product) {
        return p.getProductType() == null || p.getProductType().equals(product);
    }

    private boolean dpdMatches(NegotiationPolicy p, String dpd) {
        return p.getDpdBucket() == null || p.getDpdBucket().equals(dpd);
    }

    private int score(NegotiationPolicy p) {
        int s = (p.getPriority() == null) ? 0 : p.getPriority();
        if (p.getProductType() != null) s += 10;
        if (p.getDpdBucket()   != null) s += 10;
        return s;
    }

    private Long lenderIdFor(Account a) {
        // 1) Prefer the lender DIRECTLY linked on the account (authoritative).
        if (a.getLender() != null && a.getLender().getId() != null) {
            return a.getLender().getId();
        }
        // 2) Fallback — lender of the assigned agent.
        if (a.getAssignedAgent() != null && a.getAssignedAgent().getLender() != null) {
            return a.getAssignedAgent().getLender().getId();
        }
        // 3) Safe default for legacy demo rows.
        return 1L;
    }

    /**
     * Order: DPD_30 < DPD_60 < DPD_90 < NPA (bigger number → worse).
     */
    private String worstDpdBucket(List<Account> accounts) {
        int worst = 0;
        String bucket = null;
        for (Account a : accounts) {
            int rank = rankDpd(a.getDpdBucket());
            if (rank > worst) { worst = rank; bucket = a.getDpdBucket(); }
        }
        return bucket;
    }

    private int rankDpd(String b) {
        if (b == null) return 0;
        String u = b.toUpperCase();
        if (u.contains("NPA")) return 100;
        if (u.contains("90"))  return 90;
        if (u.contains("60"))  return 60;
        if (u.contains("30"))  return 30;
        return 0;
    }

    public static List<NegotiationStrategy> orEmpty(List<NegotiationStrategy> list) {
        return list == null ? Collections.emptyList() : list;
    }
}
