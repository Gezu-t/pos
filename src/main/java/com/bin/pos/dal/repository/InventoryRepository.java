package com.bin.pos.dal.repository;

import com.bin.pos.dal.model.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryRepository extends JpaRepository<InventoryItem, String> {
    List<InventoryItem> findByCategory(String category);
    List<InventoryItem> findByNameContainingIgnoreCase(String namePart);

    @Query("SELECT i FROM InventoryItem i WHERE i.quantity < 10")
    List<InventoryItem> findLowStockItems();
}
