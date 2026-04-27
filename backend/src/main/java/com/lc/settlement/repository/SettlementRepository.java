package com.lc.settlement.repository;

import com.lc.settlement.entity.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {
    List<Settlement> findByAccountIdOrderByCreatedAtDesc(Long accountId);
    List<Settlement> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    /** ACCEPTED settlements whose primary account is this one. */
    List<Settlement> findByAccountIdAndStatus(Long accountId, String status);

    /**
     * ACCEPTED settlements that COVER the given account — whether it was
     * the primary account or one of the bundled linked accounts.
     */
    @Query("SELECT DISTINCT s FROM Settlement s " +
           "LEFT JOIN s.linkedAccounts la " +
           "WHERE s.status = :status AND (s.account.id = :accountId OR la.id = :accountId)")
    List<Settlement> findCoveringAccountWithStatus(@Param("accountId") Long accountId,
                                                   @Param("status") String status);

    /** All settlements that list this account (primary or linked). */
    @Query("SELECT DISTINCT s FROM Settlement s " +
           "LEFT JOIN s.linkedAccounts la " +
           "WHERE s.account.id = :accountId OR la.id = :accountId " +
           "ORDER BY s.createdAt DESC")
    List<Settlement> findAllCoveringAccount(@Param("accountId") Long accountId);
}
