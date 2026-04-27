package com.lc.settlement.controller;

import com.lc.settlement.dto.CustomerDto;
import com.lc.settlement.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService service;

    @GetMapping
    public List<CustomerDto> listAll(@RequestParam(value = "lenderId", required = false) Long lenderId) {
        return service.listAllCustomersWithAccounts(lenderId);
    }

    @GetMapping("/{id}")
    public CustomerDto get(@PathVariable Long id,
                           @RequestParam(value = "lenderId", required = false) Long lenderId) {
        return service.getById(id, lenderId);
    }
}
