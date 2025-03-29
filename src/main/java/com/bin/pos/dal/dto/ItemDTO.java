package com.bin.pos.dal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for inventory items in transaction
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemDTO {
    private Long id;
    private String itemId;
    private String name;
    private String category;
    private BigDecimal price;
    private int quantity;
    private String unit;
}