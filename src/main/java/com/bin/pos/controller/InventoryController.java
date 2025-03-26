package com.bin.pos.controller;

import com.bin.pos.dal.model.InventoryItem;
import com.bin.pos.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<List<InventoryItem>> getAllItems() {
        return ResponseEntity.ok(inventoryService.getAllItems());
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<InventoryItem> getItemById(@PathVariable String itemId) {
        return inventoryService.getItemById(itemId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<InventoryItem>> getItemsByCategory(@PathVariable String category) {
        return ResponseEntity.ok(inventoryService.getItemsByCategory(category));
    }

    @GetMapping("/search")
    public ResponseEntity<List<InventoryItem>> searchItems(@RequestParam String query) {
        return ResponseEntity.ok(inventoryService.searchItems(query));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<InventoryItem>> getLowStockItems() {
        return ResponseEntity.ok(inventoryService.getLowStockItems());
    }

    @PostMapping
    public ResponseEntity<InventoryItem> createItem(@RequestBody InventoryItem item) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryService.createItem(item));
    }



    @PutMapping("/{itemId}/quantity")
    public ResponseEntity<Void> updateItemQuantity(
            @PathVariable String itemId,
            @RequestParam int change) {
        boolean updated = inventoryService.updateItemQuantity(itemId, change);
        return updated ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> deleteItem(@PathVariable String itemId) {
        inventoryService.deleteItem(itemId);
        return ResponseEntity.ok().build();
    }
}