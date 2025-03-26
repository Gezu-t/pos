package com.bin.pos.dal.repository;

import com.bin.pos.dal.model.PurchaseOrder;
import com.bin.pos.dal.model.PurchaseOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, String> {
    List<PurchaseOrder> findBySupplierSupplierId(String supplierId);
    List<PurchaseOrder> findByStatus(PurchaseOrderStatus status);

    @Query("SELECT po FROM PurchaseOrder po WHERE po.orderDate BETWEEN :start AND :end")
    List<PurchaseOrder> findByOrderDateBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}
