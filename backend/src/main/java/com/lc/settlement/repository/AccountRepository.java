package com.lc.settlement.repository;

import com.lc.settlement.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByCustomerId(Long customerId);
    List<Account> findByAssignedAgentId(Long agentId);
    List<Account> findByLenderId(Long lenderId);
    List<Account> findByCustomerIdAndLenderId(Long customerId, Long lenderId);
}
