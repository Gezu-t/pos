package com.bin.pos.dal.repository;


import com.bin.pos.dal.model.SalesTransaction;
import com.bin.pos.dal.model.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SalesRepository extends JpaRepository<SalesTransaction, String> {
    List<SalesTransaction> findByCustomerCustomerId(String customerId);
    List<SalesTransaction> findByStatus(TransactionStatus status);

    @Query("SELECT s FROM SalesTransaction s WHERE s.creationTime BETWEEN :start AND :end")
    List<SalesTransaction> findTransactionsInPeriod(
            @Param("start") LocalDateTime startTime,
            @Param("end") LocalDateTime endTime);
}