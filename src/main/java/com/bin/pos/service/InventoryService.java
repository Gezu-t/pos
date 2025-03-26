package com.bin.pos.service;


import com.bin.pos.dal.model.InventoryItem;
import com.bin.pos.dal.repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    @Autowired
    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    public List<InventoryItem> getAllItems() {
        return inventoryRepository.findAll();
    }

    public Optional<InventoryItem> getItemById(String itemId) {
        return inventoryRepository.findById(itemId);
    }

    public List<InventoryItem> searchItems(String searchTerm) {
        return inventoryRepository.findByNameContainingIgnoreCase(searchTerm);
    }

    public List<InventoryItem> getItemsByCategory(String category) {
        return inventoryRepository.findByCategory(category);
    }

    public List<InventoryItem> getLowStockItems() {
        return inventoryRepository.findLowStockItems();
    }

    @Transactional
    public InventoryItem createItem(InventoryItem item) {
        if (item.getItemId() == null || item.getItemId().isEmpty()) {
            item.setItemId(UUID.randomUUID().toString());
        }
        return inventoryRepository.save(item);
    }

    @Transactional
    public boolean updateItemQuantity(String itemId, int quantityChange) {
        Optional<InventoryItem> itemOpt = inventoryRepository.findById(itemId);
        if (itemOpt.isPresent()) {
            InventoryItem item = itemOpt.get();
            int newQuantity = item.getQuantity() + quantityChange;
            if (newQuantity >= 0) {
                item.setQuantity(newQuantity);
                inventoryRepository.save(item);
                return true;
            }
        }
        return false;
    }

    @Transactional
    public void deleteItem(String itemId) {
        inventoryRepository.deleteById(itemId);
    }
}
