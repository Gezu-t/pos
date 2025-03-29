package com.bin.pos.dal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for transaction items
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionItemDTO {
    private Long id;

    // Simplified item info
    private ItemDTO item;

    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal discountAmount;
    private BigDecimal subtotal;
}