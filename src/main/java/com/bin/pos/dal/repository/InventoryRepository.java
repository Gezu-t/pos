package com.bin.pos.dal.repository;

import com.bin.pos.dal.model.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<InventoryItem, Long> {

    // Find by business identifier
    Optional<InventoryItem> findByItemId(String itemId);

    List<InventoryItem> findByNameContainingIgnoreCase(String searchTerm);

    List<InventoryItem> findByCategory(String category);

    @Query("SELECT i FROM InventoryItem i WHERE i.quantity <= 10")
    List<InventoryItem> findLowStockItems();

    boolean existsByItemId(String itemId);
}