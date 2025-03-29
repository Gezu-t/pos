package com.bin.pos.dal.dto;

import com.bin.pos.dal.model.InventoryItem;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class InventoryItemDTO {
    private Long id;
    private String itemId;
    private ProductDTO product;  // This will include name and category
    private int quantity;
    private BigDecimal price;
    private String unit;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public InventoryItemDTO() {}

    public InventoryItemDTO(InventoryItem inventoryItem) {
        this.id = inventoryItem.getId();
        this.itemId = inventoryItem.getItemId();
        this.product = new ProductDTO(inventoryItem.getProduct());
        this.quantity = inventoryItem.getQuantity();
        this.price = inventoryItem.getPrice();
        this.unit = inventoryItem.getUnit();
        this.createdAt = inventoryItem.getCreatedAt();
        this.updatedAt = inventoryItem.getUpdatedAt();
    }
}