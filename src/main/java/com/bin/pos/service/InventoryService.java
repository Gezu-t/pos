package com.bin.pos.service;

import com.bin.pos.dal.dto.InventoryItemDTO;
import com.bin.pos.dal.model.InventoryItem;
import com.bin.pos.dal.model.Product;
import com.bin.pos.dal.repository.InventoryRepository;
import com.bin.pos.dal.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;

    @Autowired
    public InventoryService(InventoryRepository inventoryRepository, ProductRepository productRepository) {
        this.inventoryRepository = inventoryRepository;
        this.productRepository = productRepository;
    }

    // Add @Transactional to keep session open during lazy loading
    @Transactional(readOnly = true)
    public List<InventoryItemDTO> getAllItems() {
        return inventoryRepository.findAll().stream()
                .map(InventoryItemDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<InventoryItemDTO> getItemById(Long id) {
        return inventoryRepository.findById(id).map(InventoryItemDTO::new);
    }

    @Transactional(readOnly = true)
    public Optional<InventoryItemDTO> getItemByItemId(String itemId) {
        return inventoryRepository.findByItemId(itemId).map(InventoryItemDTO::new);
    }

    // Add @Transactional to keep session open during lazy loading
    @Transactional(readOnly = true)
    public List<InventoryItemDTO> searchItems(String searchTerm) {
        return inventoryRepository.findAll().stream()
                .filter(item -> item.getProduct().getName().toLowerCase().contains(searchTerm.toLowerCase()))
                .map(InventoryItemDTO::new)
                .collect(Collectors.toList());
    }

    // Add @Transactional to keep session open during lazy loading
    @Transactional(readOnly = true)
    public List<InventoryItemDTO> getItemsByCategory(String category) {
        return inventoryRepository.findAll().stream()
                .filter(item -> item.getProduct().getCategory().equalsIgnoreCase(category))
                .map(InventoryItemDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InventoryItemDTO> getLowStockItems() {
        return inventoryRepository.findLowStockItems().stream()
                .map(InventoryItemDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public InventoryItemDTO createItem(InventoryItemDTO itemDTO) {
        InventoryItem item = new InventoryItem();

        // Generate itemId if not provided
        item.setItemId(
                Optional.ofNullable(itemDTO.getItemId())
                        .filter(id -> !id.isEmpty())
                        .orElse("ITEM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
        );

        item.setQuantity(itemDTO.getQuantity());
        item.setPrice(itemDTO.getPrice());
        item.setUnit(itemDTO.getUnit());

        // Find and set product
        Product product = productRepository.findByProductId(itemDTO.getProduct().getProductId())
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + itemDTO.getProduct().getProductId()));
        item.setProduct(product);

        // Explicitly set name and category from product
        item.setName(product.getName());
        item.setCategory(product.getCategory());

        return new InventoryItemDTO(inventoryRepository.save(item));
    }


    @Transactional
    public InventoryItemDTO updateItem(Long id, InventoryItemDTO itemDTO) {
        return inventoryRepository.findById(id).map(existingItem -> {
            existingItem.setQuantity(itemDTO.getQuantity());
            existingItem.setPrice(itemDTO.getPrice());
            existingItem.setUnit(itemDTO.getUnit());
            Product product = productRepository.findByProductId(itemDTO.getProduct().getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + itemDTO.getProduct().getProductId()));
            existingItem.setProduct(product);
            return new InventoryItemDTO(inventoryRepository.save(existingItem));
        }).orElse(null);
    }

    @Transactional
    public InventoryItemDTO updateItemByItemId(String itemId, InventoryItemDTO itemDTO) {
        return inventoryRepository.findByItemId(itemId).map(existingItem -> {
            return updateItem(existingItem.getId(), itemDTO);
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