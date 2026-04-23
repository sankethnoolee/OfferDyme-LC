package com.offerdyne.controller;

import com.offerdyne.entity.Lender;
import com.offerdyne.entity.LenderPolicy;
import com.offerdyne.repository.LenderPolicyRepository;
import com.offerdyne.repository.LenderRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lenders")
public class LenderController {

    private final LenderRepository lenders;
    private final LenderPolicyRepository policies;

    public LenderController(LenderRepository lenders, LenderPolicyRepository policies) {
        this.lenders = lenders;
        this.policies = policies;
    }

    @GetMapping public List<Lender> all() { return lenders.findAll(); }

    @GetMapping("/{id}") public Lender one(@PathVariable Long id) {
        return lenders.findById(id).orElseThrow();
    }

    @GetMapping("/{id}/policies")
    public List<LenderPolicy> policiesFor(@PathVariable Long id) {
        return policies.findByLenderIdAndActiveTrue(id);
    }
}
