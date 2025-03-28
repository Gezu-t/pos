package com.bin.pos.controller;

import com.bin.pos.dal.model.InventoryItem;
import com.bin.pos.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    @Autowired
    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<List<InventoryItem>> getAllItems() {
        return ResponseEntity.ok(inventoryService.getAllItems());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<InventoryItem> getItemById(@PathVariable Long id) {
        return inventoryService.getItemById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-item-id/{itemId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<InventoryItem> getItemByItemId(@PathVariable String itemId) {
        if (itemId == null || itemId.equals("undefined") || itemId.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return inventoryService.getItemByItemId(itemId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/category/{category}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<List<InventoryItem>> getItemsByCategory(@PathVariable String category) {
        return ResponseEntity.ok(inventoryService.getItemsByCategory(category));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<List<InventoryItem>> searchItems(@RequestParam String query) {
        return ResponseEntity.ok(inventoryService.searchItems(query));
    }

    @GetMapping("/low-stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<List<InventoryItem>> getLowStockItems() {
        return ResponseEntity.ok(inventoryService.getLowStockItems());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<InventoryItem> createItem(@RequestBody InventoryItem item) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryService.createItem(item));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<InventoryItem> updateItem(@PathVariable Long id, @RequestBody InventoryItem item) {
        InventoryItem updatedItem = inventoryService.updateItem(id, item);
        return updatedItem != null ?
                ResponseEntity.ok(updatedItem) :
                ResponseEntity.notFound().build();
    }

    @PutMapping("/by-item-id/{itemId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<InventoryItem> updateItemByItemId(
            @PathVariable String itemId,
            @RequestBody InventoryItem item) {
        if (itemId == null || itemId.equals("undefined") || itemId.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        InventoryItem updatedItem = inventoryService.updateItemByItemId(itemId, item);
        return updatedItem != null ?
                ResponseEntity.ok(updatedItem) :
                ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}/quantity")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<Void> updateItemQuantity(
            @PathVariable Long id,
            @RequestParam int change) {
        boolean updated = inventoryService.updateItemQuantity(id, change);
        return updated ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
    }

    @PutMapping("/by-item-id/{itemId}/quantity")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<Void> updateItemQuantityByItemId(
            @PathVariable String itemId,
            @RequestParam int change) {
        if (itemId == null || itemId.equals("undefined") || itemId.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        boolean updated = inventoryService.updateItemQuantityByItemId(itemId, change);
        return updated ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        boolean deleted = inventoryService.deleteItem(id);
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/by-item-id/{itemId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> deleteItemByItemId(@PathVariable String itemId) {
        if (itemId == null || itemId.equals("undefined") || itemId.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        boolean deleted = inventoryService.deleteItemByItemId(itemId);
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
}