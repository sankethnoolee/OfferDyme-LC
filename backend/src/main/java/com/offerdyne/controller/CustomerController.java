package com.offerdyne.controller;

import com.offerdyne.entity.Account;
import com.offerdyne.entity.Customer;
import com.offerdyne.repository.AccountRepository;
import com.offerdyne.repository.CustomerRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerRepository customers;
    private final AccountRepository accounts;

    public CustomerController(CustomerRepository customers, AccountRepository accounts) {
        this.customers = customers;
        this.accounts = accounts;
    }

    @GetMapping public List<Customer> all() { return customers.findAll(); }

    @GetMapping("/{id}") public Customer one(@PathVariable Long id) {
        return customers.findById(id).orElseThrow();
    }

    @GetMapping("/{id}/accounts")
    public List<Account> accountsFor(@PathVariable Long id) {
        return accounts.findByCustomerId(id);
    }
}
