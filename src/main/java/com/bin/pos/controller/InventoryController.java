package com.bin.pos.controller;

import com.bin.pos.dal.dto.InventoryItemDTO;
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
    public ResponseEntity<List<InventoryItemDTO>> getAllItems() {
        return ResponseEntity.ok(inventoryService.getAllItems());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<InventoryItemDTO> getItemById(@PathVariable Long id) {
        return inventoryService.getItemById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-item-id/{itemId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<InventoryItemDTO> getItemByItemId(@PathVariable String itemId) {
        if (itemId == null || itemId.equals("undefined") || itemId.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return inventoryService.getItemByItemId(itemId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/category/{category}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<List<InventoryItemDTO>> getItemsByCategory(@PathVariable String category) {
        return ResponseEntity.ok(inventoryService.getItemsByCategory(category));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<List<InventoryItemDTO>> searchItems(@RequestParam String query) {
        return ResponseEntity.ok(inventoryService.searchItems(query));
    }

    @GetMapping("/low-stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<List<InventoryItemDTO>> getLowStockItems() {
        return ResponseEntity.ok(inventoryService.getLowStockItems());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<InventoryItemDTO> createItem(@RequestBody InventoryItemDTO itemDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryService.createItem(itemDTO));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<InventoryItemDTO> updateItem(@PathVariable Long id, @RequestBody InventoryItemDTO itemDTO) {
        InventoryItemDTO updatedItem = inventoryService.updateItem(id, itemDTO);
        return updatedItem != null ?
                ResponseEntity.ok(updatedItem) :
                ResponseEntity.notFound().build();
    }

    @PutMapping("/by-item-id/{itemId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<InventoryItemDTO> updateItemByItemId(
            @PathVariable String itemId,
            @RequestBody InventoryItemDTO itemDTO) {
        if (itemId == null || itemId.equals("undefined") || itemId.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        InventoryItemDTO updatedItem = inventoryService.updateItemByItemId(itemId, itemDTO);
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