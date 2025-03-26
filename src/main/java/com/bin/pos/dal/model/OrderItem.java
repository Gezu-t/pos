package com.bin.pos.dal.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private OrderTransaction order;

    @ManyToOne
    @JoinColumn(name = "item_id")
    private InventoryItem item;

    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal discountAmount;

    public BigDecimal getSubtotal() {
        BigDecimal rawSubtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        return discountAmount != null ? rawSubtotal.subtract(discountAmount) : rawSubtotal;
    }
}