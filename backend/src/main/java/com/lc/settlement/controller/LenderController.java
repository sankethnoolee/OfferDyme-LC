package com.lc.settlement.controller;

import com.lc.settlement.dto.LenderDto;
import com.lc.settlement.service.LenderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lenders")
@RequiredArgsConstructor
public class LenderController {

    private final LenderService service;

    @GetMapping
    public List<LenderDto> listAll() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public LenderDto get(@PathVariable Long id) {
        return service.getById(id);
    }
}
