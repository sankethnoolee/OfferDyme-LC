package com.lc.settlement.controller;

import com.lc.settlement.entity.Lender;
import com.lc.settlement.entity.NegotiationPolicy;
import com.lc.settlement.entity.NegotiationStrategy;
import com.lc.settlement.repository.LenderRepository;
import com.lc.settlement.service.PolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Admin CRUD for the two configuration tables that back the negotiation engine:
 *
 *   /api/policy/policies    — floor/ceiling rows per (product, dpd)
 *   /api/policy/strategies  — the "plays" Claude can pick per turn (HOLD, LOWER, ...)
 *
 * These are the single source of truth — edit here (or directly in the DB)
 * and the next Claude turn picks them up immediately.
 */
@RestController
@RequestMapping("/api/policy")
@RequiredArgsConstructor
public class PolicyController {

    private final PolicyService policyService;
    private final LenderRepository lenderRepo;

    // ---------------- Policies ----------------

    @GetMapping("/policies")
    public List<NegotiationPolicy> listPolicies(@RequestParam(defaultValue = "1") Long lenderId) {
        return policyService.listPolicies(lenderId);
    }

    @PostMapping("/policies")
    public ResponseEntity<NegotiationPolicy> createPolicy(@RequestBody NegotiationPolicy body,
                                                          @RequestParam(defaultValue = "1") Long lenderId) {
        attachLender(body, lenderId);
        if (body.getActive() == null) body.setActive(true);
        if (body.getPriority() == null) body.setPriority(0);
        return ResponseEntity.ok(policyService.save(body));
    }

    @PutMapping("/policies/{id}")
    public ResponseEntity<NegotiationPolicy> updatePolicy(@PathVariable Long id,
                                                          @RequestBody NegotiationPolicy body,
                                                          @RequestParam(defaultValue = "1") Long lenderId) {
        body.setId(id);
        attachLender(body, lenderId);
        return ResponseEntity.ok(policyService.save(body));
    }

    @DeleteMapping("/policies/{id}")
    public ResponseEntity<Map<String, Object>> deletePolicy(@PathVariable Long id) {
        policyService.deletePolicy(id);
        return ResponseEntity.ok(Map.of("deleted", id));
    }

    // ---------------- Strategies ----------------

    @GetMapping("/strategies")
    public List<NegotiationStrategy> listStrategies(@RequestParam(defaultValue = "1") Long lenderId,
                                                    @RequestParam(defaultValue = "false") boolean onlyActive) {
        return onlyActive
                ? policyService.activeStrategies(lenderId)
                : policyService.listStrategies(lenderId);
    }

    @PostMapping("/strategies")
    public ResponseEntity<NegotiationStrategy> createStrategy(@RequestBody NegotiationStrategy body,
                                                              @RequestParam(defaultValue = "1") Long lenderId) {
        attachLender(body, lenderId);
        if (body.getActive() == null) body.setActive(true);
        if (body.getPriority() == null) body.setPriority(99);
        return ResponseEntity.ok(policyService.save(body));
    }

    @PutMapping("/strategies/{id}")
    public ResponseEntity<NegotiationStrategy> updateStrategy(@PathVariable Long id,
                                                              @RequestBody NegotiationStrategy body,
                                                              @RequestParam(defaultValue = "1") Long lenderId) {
        body.setId(id);
        attachLender(body, lenderId);
        return ResponseEntity.ok(policyService.save(body));
    }

    @DeleteMapping("/strategies/{id}")
    public ResponseEntity<Map<String, Object>> deleteStrategy(@PathVariable Long id) {
        policyService.deleteStrategy(id);
        return ResponseEntity.ok(Map.of("deleted", id));
    }

    // ---------------- Resolver (debug / UI preview) ----------------

    /**
     * Resolve which policy row would apply for a given (product, dpd).
     * Useful for the admin UI so the user can preview what combination
     * matches before saving a row.
     */
    @GetMapping("/policies/resolve")
    public NegotiationPolicy resolvePreview(@RequestParam(defaultValue = "1") Long lenderId,
                                            @RequestParam(required = false) String productType,
                                            @RequestParam(required = false) String dpdBucket) {
        return policyService.resolve(lenderId, productType, dpdBucket);
    }

    // ---------------- Helpers ----------------

    private void attachLender(NegotiationPolicy p, Long lenderId) {
        if (p.getLender() == null || p.getLender().getId() == null) {
            Lender l = lenderRepo.findById(lenderId)
                    .orElseThrow(() -> new IllegalArgumentException("Lender not found: " + lenderId));
            p.setLender(l);
        }
    }

    private void attachLender(NegotiationStrategy s, Long lenderId) {
        if (s.getLender() == null || s.getLender().getId() == null) {
            Lender l = lenderRepo.findById(lenderId)
                    .orElseThrow(() -> new IllegalArgumentException("Lender not found: " + lenderId));
            s.setLender(l);
        }
    }
}
