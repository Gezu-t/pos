package com.bin.pos.dal.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "order_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    private LocalDateTime orderDate;
    private LocalDateTime estimatedDeliveryDate;
    private LocalDateTime actualDeliveryDate;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private String shippingAddress;
    private String trackingNumber;

    private BigDecimal subtotal = BigDecimal.ZERO;
    private BigDecimal shippingCost = BigDecimal.ZERO;
    private BigDecimal taxAmount = BigDecimal.ZERO;
    private BigDecimal totalAmount = BigDecimal.ZERO;
    private BigDecimal taxRate = BigDecimal.valueOf(0.0);

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    private String notes;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper methods for calculations
    public BigDecimal calculateSubtotal() {
        if (items == null || items.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal calculateTaxAmount() {
        return calculateSubtotal().multiply(taxRate != null ? taxRate : BigDecimal.ZERO);
    }

    public BigDecimal calculateTotal() {
        return calculateSubtotal()
                .add(calculateTaxAmount())
                .add(shippingCost != null ? shippingCost : BigDecimal.ZERO);
    }
}