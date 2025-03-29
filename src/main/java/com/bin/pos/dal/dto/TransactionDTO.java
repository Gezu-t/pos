package com.bin.pos.dal.dto;

import com.bin.pos.dal.model.PaymentMethod;
import com.bin.pos.dal.model.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO for transaction list view and detail view
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDTO {
    private Long id;
    private String transactionId;

    // Customer simplified to avoid lazy loading issues
    private CustomerDTO customer;

    private LocalDateTime creationTime;
    private LocalDateTime completionTime;

    private TransactionStatus status;
    private PaymentMethod paymentMethod;
    private BigDecimal amountPaid;

    // Items list
    private List<TransactionItemDTO> items = new ArrayList<>();

    // Financial information
    private BigDecimal taxRate;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;

    private String notes;
}