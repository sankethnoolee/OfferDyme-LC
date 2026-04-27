package com.lc.settlement.controller;

import com.lc.settlement.dto.FieldAgentDto;
import com.lc.settlement.service.FieldAgentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/agents")
@RequiredArgsConstructor
public class FieldAgentController {

    private final FieldAgentService service;

    @GetMapping
    public List<FieldAgentDto> listAll() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public FieldAgentDto get(@PathVariable Long id) {
        return service.getById(id);
    }
}
