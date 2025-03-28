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

    public Optional<InventoryItem> getItemById(Long id) {
        return inventoryRepository.findById(id);
    }

    public Optional<InventoryItem> getItemByItemId(String itemId) {
        return inventoryRepository.findByItemId(itemId);
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
            item.setItemId("ITEM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        return inventoryRepository.save(item);
    }

    @Transactional
    public InventoryItem updateItem(Long id, InventoryItem itemDetails) {
        return inventoryRepository.findById(id).map(existingItem -> {
            // Update fields but preserve ID and itemId
            existingItem.setName(itemDetails.getName());
            existingItem.setCategory(itemDetails.getCategory());
            existingItem.setPrice(itemDetails.getPrice());
            existingItem.setQuantity(itemDetails.getQuantity());
            existingItem.setUnit(itemDetails.getUnit());
            return inventoryRepository.save(existingItem);
        }).orElse(null);
    }

    @Transactional
    public InventoryItem updateItemByItemId(String itemId, InventoryItem itemDetails) {
        return inventoryRepository.findByItemId(itemId).map(existingItem -> {
            return updateItem(existingItem.getId(), itemDetails);
        }).orElse(null);
    }

    @Transactional
    public boolean updateItemQuantity(Long id, int quantityChange) {
        Optional<InventoryItem> itemOpt = inventoryRepository.findById(id);
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
    public boolean updateItemQuantityByItemId(String itemId, int quantityChange) {
        Optional<InventoryItem> itemOpt = inventoryRepository.findByItemId(itemId);
        if (itemOpt.isPresent()) {
            return updateItemQuantity(itemOpt.get().getId(), quantityChange);
        }
        return false;
    }

    @Transactional
    public boolean deleteItem(Long id) {
        if (inventoryRepository.existsById(id)) {
            inventoryRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional
    public boolean deleteItemByItemId(String itemId) {
        Optional<InventoryItem> itemOpt = inventoryRepository.findByItemId(itemId);
        if (itemOpt.isPresent()) {
            inventoryRepository.delete(itemOpt.get());
            return true;
        }
        return false;
    }
}