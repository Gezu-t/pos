package com.bin.pos.dal.repository;

import com.bin.pos.dal.model.SalesTransaction;
import com.bin.pos.dal.model.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SalesRepository extends JpaRepository<SalesTransaction, Long> {

    // Find by business identifier
    Optional<SalesTransaction> findByTransactionId(String transactionId);

    List<SalesTransaction> findByCustomerCustomerId(String customerId);

    List<SalesTransaction> findByStatus(TransactionStatus status);

    @Query("SELECT s FROM SalesTransaction s WHERE s.creationTime BETWEEN :startDate AND :endDate")
    List<SalesTransaction> findTransactionsInPeriod(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT SUM(s.totalAmount) FROM SalesTransaction s WHERE s.status = 'COMPLETED' AND s.completionTime BETWEEN :startDate AND :endDate")
    BigDecimal calculateRevenueInPeriod(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(s) FROM SalesTransaction s WHERE s.status = :status AND s.creationTime BETWEEN :startDate AND :endDate")
    Long countTransactionsByStatusInPeriod(
            @Param("status") TransactionStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}