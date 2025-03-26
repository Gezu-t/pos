package com.bin.pos.dal.repository;


import com.bin.pos.dal.model.OrderStatus;
import com.bin.pos.dal.model.OrderTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<OrderTransaction, String> {
    List<OrderTransaction> findByCustomerCustomerId(String customerId);
    List<OrderTransaction> findByStatus(OrderStatus status);

    @Query("SELECT o FROM OrderTransaction o WHERE o.orderDate BETWEEN :start AND :end")
    List<OrderTransaction> findOrdersInPeriod(
            @Param("start") LocalDateTime startTime,
            @Param("end") LocalDateTime endTime);
}