package com.bin.pos.dal.repository;

import com.bin.pos.dal.model.SalesTransaction;
import com.bin.pos.dal.model.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SalesRepository extends JpaRepository<SalesTransaction, Long> {

    // Find transaction by transactionId with eager loading
    @Query("SELECT DISTINCT t FROM SalesTransaction t " +
            "LEFT JOIN FETCH t.customer " +
            "LEFT JOIN FETCH t.items i " +
            "LEFT JOIN FETCH i.item " +
            "WHERE t.transactionId = :transactionId")
    Optional<SalesTransaction> findByTransactionIdWithDetails(@Param("transactionId") String transactionId);

    // Find transaction by ID with eager loading
    @Query("SELECT DISTINCT t FROM SalesTransaction t " +
            "LEFT JOIN FETCH t.customer " +
            "LEFT JOIN FETCH t.items i " +
            "LEFT JOIN FETCH i.item " +
            "WHERE t.id = :id")
    Optional<SalesTransaction> findByIdWithDetails(@Param("id") Long id);

    // Get all transactions with basic details (excluding items for performance)
    @Query("SELECT DISTINCT t FROM SalesTransaction t " +
            "LEFT JOIN FETCH t.customer " +
            "ORDER BY t.creationTime DESC")
    List<SalesTransaction> findAllWithBasicDetails();

    // Original methods
    Optional<SalesTransaction> findByTransactionId(String transactionId);

    List<SalesTransaction> findByCustomerCustomerId(String customerId);

    List<SalesTransaction> findByStatus(TransactionStatus status);

    @Query("SELECT t FROM SalesTransaction t WHERE t.creationTime BETWEEN :start AND :end")
    List<SalesTransaction> findTransactionsInPeriod(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}