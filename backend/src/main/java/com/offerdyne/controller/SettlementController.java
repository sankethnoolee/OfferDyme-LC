package com.offerdyne.controller;

import com.offerdyne.entity.Settlement;
import com.offerdyne.repository.SettlementRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/settlements")
public class SettlementController {

    private final SettlementRepository settlements;
    public SettlementController(SettlementRepository s) { this.settlements = s; }

    @GetMapping public List<Settlement> all() { return settlements.findAll(); }

    @GetMapping("/by-customer/{id}")
    public List<Settlement> byCustomer(@PathVariable Long id) {
        return settlements.findByCustomerId(id);
    }

    @GetMapping("/by-account/{id}")
    public List<Settlement> byAccount(@PathVariable Long id) {
        return settlements.findByAccountId(id);
    }

    @GetMapping("/by-session/{id}")
    public List<Settlement> bySession(@PathVariable Long id) {
        return settlements.findBySessionId(id);
    }

    @GetMapping("/bundle/{groupId}")
    public List<Settlement> byBundle(@PathVariable String groupId) {
        return settlements.findByBundleGroupId(groupId);
    }
}
