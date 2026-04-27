package com.lc.settlement.controller;

import com.lc.settlement.dto.AccountDetailDto;
import com.lc.settlement.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService service;

    @GetMapping("/{id}")
    public AccountDetailDto get(@PathVariable Long id) {
        return service.getDetail(id);
    }
}
