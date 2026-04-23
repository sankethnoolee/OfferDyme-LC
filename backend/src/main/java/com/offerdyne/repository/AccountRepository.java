package com.offerdyne.repository;

import com.offerdyne.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByCustomerId(Long customerId);
    List<Account> findByCustomerIdAndAccountStatus(Long customerId, String status);
}
