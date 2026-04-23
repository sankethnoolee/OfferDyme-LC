package com.offerdyne.controller;

import com.offerdyne.entity.FieldAgent;
import com.offerdyne.repository.FieldAgentRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/agents")
public class AgentController {

    private final FieldAgentRepository agents;

    public AgentController(FieldAgentRepository agents) { this.agents = agents; }

    @GetMapping public List<FieldAgent> all() { return agents.findAll(); }

    @GetMapping("/{id}") public FieldAgent one(@PathVariable Long id) {
        return agents.findById(id).orElseThrow();
    }
}
