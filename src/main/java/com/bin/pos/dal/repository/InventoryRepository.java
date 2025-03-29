package com.bin.pos.dal.repository;

import com.bin.pos.dal.model.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<InventoryItem, Long> {

    // Use JOIN FETCH to eagerly load the product when fetching inventory items
    @Query("SELECT i FROM InventoryItem i JOIN FETCH i.product")
    List<InventoryItem> findAll();

    // Use JOIN FETCH for specific item query
    @Query("SELECT i FROM InventoryItem i JOIN FETCH i.product WHERE i.id = :id")
    Optional<InventoryItem> findById(Long id);

    // Use JOIN FETCH for itemId query
    @Query("SELECT i FROM InventoryItem i JOIN FETCH i.product WHERE i.itemId = :itemId")
    Optional<InventoryItem> findByItemId(String itemId);

    // Use JOIN FETCH for low stock items
    @Query("SELECT i FROM InventoryItem i JOIN FETCH i.product WHERE i.quantity < 10")
    List<InventoryItem> findLowStockItems();

    // If you need custom queries for category or search, add them here with JOIN FETCH
    @Query("SELECT i FROM InventoryItem i JOIN FETCH i.product WHERE i.product.category = :category")
    List<InventoryItem> findByCategory(String category);

    @Query("SELECT i FROM InventoryItem i JOIN FETCH i.product WHERE LOWER(i.product.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<InventoryItem> searchByProductName(String searchTerm);
}