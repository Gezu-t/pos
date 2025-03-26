package com.bin.pos.dal.repository;

import com.bin.pos.dal.model.InventoryMovement;
import com.bin.pos.dal.model.MovementType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long> {
    List<InventoryMovement> findByItemItemId(String itemId);
    List<InventoryMovement> findByMovementType(MovementType movementType);

    @Query("SELECT m FROM InventoryMovement m WHERE m.item.itemId = :itemId AND m.timestamp BETWEEN :start AND :end")
    List<InventoryMovement> findByItemAndDateRange(
            @Param("itemId") String itemId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}