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
@Table(name = "sales_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String transactionId;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    private LocalDateTime creationTime;
    private LocalDateTime completionTime;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    private BigDecimal amountPaid = BigDecimal.ZERO;

    // Products in the transaction
    @OneToMany(mappedBy = "salesTransaction", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TransactionItem> items = new ArrayList<>();

    // Services in the transaction
    @OneToMany(mappedBy = "salesTransaction", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ServiceTransactionItem> serviceItems = new ArrayList<>();

    // For tax calculations
    private BigDecimal taxRate = BigDecimal.valueOf(0.0);
    private BigDecimal subtotal = BigDecimal.ZERO;
    private BigDecimal taxAmount = BigDecimal.ZERO;
    private BigDecimal totalAmount = BigDecimal.ZERO;

    private String notes;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public BigDecimal getProductSubtotal() {
        if (items == null || items.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return items.stream()
                .map(TransactionItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getServiceSubtotal() {
        if (serviceItems == null || serviceItems.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return serviceItems.stream()
                .map(ServiceTransactionItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getSubtotal() {
        return getProductSubtotal().add(getServiceSubtotal());
    }

    public BigDecimal getTaxAmount() {
        return getSubtotal().multiply(taxRate != null ? taxRate : BigDecimal.ZERO);
    }

    public BigDecimal getTotal() {
        return getSubtotal().add(getTaxAmount());
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        // Update calculated fields
        this.subtotal = getSubtotal();
        this.taxAmount = getTaxAmount();
        this.totalAmount = getTotal();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();

        // Update calculated fields
        this.subtotal = getSubtotal();
        this.taxAmount = getTaxAmount();
        this.totalAmount = getTotal();
    }
}