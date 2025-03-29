package com.bin.pos.dal.repository;

import com.bin.pos.dal.model.OrderStatus;
import com.bin.pos.dal.model.OrderTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<OrderTransaction, Long> {

    // Find by business identifier (orderId)
    Optional<OrderTransaction> findByOrderId(String orderId);

    List<OrderTransaction> findByCustomerCustomerId(String customerId);

    List<OrderTransaction> findByStatus(OrderStatus status);

    @Query("SELECT o FROM OrderTransaction o WHERE o.orderDate BETWEEN :startDate AND :endDate")
    List<OrderTransaction> findOrdersInPeriod(@Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate);

    @Query("SELECT SUM(o.totalAmount) FROM OrderTransaction o WHERE o.status = 'COMPLETED' AND o.orderDate BETWEEN :startDate AND :endDate")
    BigDecimal calculateRevenueInPeriod(@Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(o) FROM OrderTransaction o WHERE o.orderDate BETWEEN :startDate AND :endDate")
    Long countOrdersInPeriod(@Param("startDate") LocalDateTime startDate,
                             @Param("endDate") LocalDateTime endDate);

    @Query("SELECT o FROM OrderTransaction o LEFT JOIN FETCH o.customer")
    List<OrderTransaction> findAllWithCustomer();

    @Query("SELECT o FROM OrderTransaction o LEFT JOIN FETCH o.customer LEFT JOIN FETCH o.items WHERE o.orderId = :orderId")
    Optional<OrderTransaction> findByOrderIdWithCustomer(@Param("orderId") String orderId);
}