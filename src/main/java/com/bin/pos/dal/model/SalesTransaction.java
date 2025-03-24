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

    private BigDecimal amountPaid;

    @OneToMany(mappedBy = "salesTransaction", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TransactionItem> items = new ArrayList<>();

    public BigDecimal getTotal() {
        return items.stream()
                .map(TransactionItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}