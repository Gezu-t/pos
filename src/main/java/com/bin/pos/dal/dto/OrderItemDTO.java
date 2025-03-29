package com.bin.pos.dal.dto;

import com.bin.pos.dal.model.OrderItem;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemDTO {
    private Long id;
    private String itemId;
    private int quantity;
    private BigDecimal unitPrice;

    public OrderItemDTO(OrderItem item) {
        this.id = item.getId();
        this.itemId = item.getOrderItemId(); // Adjust based on your OrderItem entity
        this.quantity = item.getQuantity();
        this.unitPrice = item.getUnitPrice();
    }
}