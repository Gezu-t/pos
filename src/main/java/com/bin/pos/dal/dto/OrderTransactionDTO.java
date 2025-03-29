package com.bin.pos.dal.dto;

import com.bin.pos.dal.model.OrderTransaction;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderTransactionDTO {
    private Long id;
    private String orderId;
    private String customerName;
    private LocalDateTime orderDate;
    private LocalDateTime estimatedDeliveryDate;
    private LocalDateTime actualDeliveryDate;
    private String status;
    private String shippingAddress;
    private String trackingNumber;
    private BigDecimal subtotal;
    private BigDecimal shippingCost;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private BigDecimal taxRate;
    private String paymentMethod;

    public OrderTransactionDTO() {}

    public OrderTransactionDTO(OrderTransaction order) {
        this.id = order.getId();
        this.orderId = order.getOrderId();
        this.customerName = order.getCustomer() != null ? order.getCustomer().getName() : "N/A";
        this.orderDate = order.getOrderDate();
        this.estimatedDeliveryDate = order.getEstimatedDeliveryDate();
        this.actualDeliveryDate = order.getActualDeliveryDate();
        this.status = order.getStatus() != null ? order.getStatus().toString() : null;
        this.shippingAddress = order.getShippingAddress();
        this.trackingNumber = order.getTrackingNumber();
        this.subtotal = order.getSubtotal();
        this.shippingCost = order.getShippingCost();
        this.taxAmount = order.getTaxAmount();
        this.totalAmount = order.getTotalAmount();
        this.taxRate = order.getTaxRate();
        this.paymentMethod = order.getPaymentMethod() != null ? order.getPaymentMethod().toString() : null;
    }
}