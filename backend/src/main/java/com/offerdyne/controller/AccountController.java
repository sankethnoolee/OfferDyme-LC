package com.offerdyne.controller;

import com.offerdyne.entity.Account;
import com.offerdyne.repository.AccountRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountRepository accounts;
    public AccountController(AccountRepository accounts) { this.accounts = accounts; }

    @GetMapping public List<Account> all() { return accounts.findAll(); }

    @GetMapping("/{id}") public Account one(@PathVariable Long id) {
        return accounts.findById(id).orElseThrow();
    }
}
