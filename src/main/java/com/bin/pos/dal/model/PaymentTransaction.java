package com.bin.pos.dal.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentTransaction {
    @Id
    private String paymentId;

    @ManyToOne
    @JoinColumn(name = "transaction_id")
    private SalesTransaction salesTransaction;

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private LocalDateTime paymentTime;

    private String referenceNumber;

    @PrePersist
    protected void onCreate() {
        paymentTime = LocalDateTime.now();
    }
}