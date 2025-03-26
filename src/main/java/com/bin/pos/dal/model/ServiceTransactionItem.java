package com.bin.pos.dal.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "service_transaction_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceTransactionItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "transaction_id")
    private SalesTransaction salesTransaction;

    @ManyToOne
    @JoinColumn(name = "service_id")
    private ServiceOffering service;

    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal discountAmount;
    private String notes;

    public BigDecimal getSubtotal() {
        BigDecimal rawSubtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        return discountAmount != null ? rawSubtotal.subtract(discountAmount) : rawSubtotal;
    }
}
