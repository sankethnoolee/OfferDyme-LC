package com.lc.settlement.service;

import com.lc.settlement.dto.AccountSummaryDto;
import com.lc.settlement.dto.CustomerDto;
import com.lc.settlement.entity.Account;
import com.lc.settlement.entity.Customer;
import com.lc.settlement.entity.Settlement;
import com.lc.settlement.repository.AccountRepository;
import com.lc.settlement.repository.CustomerRepository;
import com.lc.settlement.repository.SettlementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerService {

    private final CustomerRepository customerRepo;
    private final AccountRepository accountRepo;
    private final SettlementRepository settlementRepo;

    public List<CustomerDto> listAllCustomersWithAccounts() {
        return listAllCustomersWithAccounts(null);
    }

    /**
     * If lenderId is provided, only accounts for that lender are returned,
     * and customers with zero matching accounts are dropped.
     */
    public List<CustomerDto> listAllCustomersWithAccounts(Long lenderId) {
        return customerRepo.findAllByOrderByFullNameAsc().stream()
                .map(c -> toDtoWithAccounts(c, lenderId))
                .filter(dto -> lenderId == null || (dto.getAccountCount() != null && dto.getAccountCount() > 0))
                .collect(Collectors.toList());
    }

    public CustomerDto getById(Long id) {
        return getById(id, null);
    }

    public CustomerDto getById(Long id, Long lenderId) {
        Customer c = customerRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + id));
        return toDtoWithAccounts(c, lenderId);
    }

    private CustomerDto toDtoWithAccounts(Customer c, Long lenderId) {
        List<Account> accounts = lenderId == null
                ? accountRepo.findByCustomerId(c.getId())
                : accountRepo.findByCustomerIdAndLenderId(c.getId(), lenderId);

        List<AccountSummaryDto> accountDtos = accounts.stream().map(a -> {
            boolean accepted = hasAcceptedSettlement(a.getId());
            return AccountSummaryDto.builder()
                    .id(a.getId())
                    .accountNumber(a.getAccountNumber())
                    .productType(a.getProductType())
                    .outstandingAmount(a.getOutstandingAmount())
                    .dpdBucket(a.getDpdBucket())
                    .daysPastDue(a.getDaysPastDue())
                    .status(a.getStatus())
                    .assignedAgentName(a.getAssignedAgent() == null ? null : a.getAssignedAgent().getName())
                    .lenderId(a.getLender() == null ? null : a.getLender().getId())
                    .lenderName(a.getLender() == null ? null : a.getLender().getName())
                    .lenderCode(a.getLender() == null ? null : a.getLender().getCode())
                    .hasAcceptedSettlement(accepted)
                    .build();
        }).collect(Collectors.toList());

        double total = accounts.stream()
                .mapToDouble(a -> a.getOutstandingAmount() == null ? 0.0 : a.getOutstandingAmount())
                .sum();

        return CustomerDto.builder()
                .id(c.getId())
                .fullName(c.getFullName())
                .panNumber(c.getPanNumber())
                .email(c.getEmail())
                .phone(c.getPhone())
                .city(c.getCity())
                .state(c.getState())
                .annualIncome(c.getAnnualIncome())
                .accountCount(accounts.size())
                .totalOutstanding(total)
                .accounts(accountDtos)
                .filteredLenderId(lenderId)
                .build();
    }

    private boolean hasAcceptedSettlement(Long accountId) {
        List<Settlement> accepted = settlementRepo.findCoveringAccountWithStatus(accountId, "ACCEPTED");
        return accepted != null && !accepted.isEmpty();
    }
}
