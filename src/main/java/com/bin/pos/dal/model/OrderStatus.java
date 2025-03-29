package com.bin.pos.dal.model;

public enum OrderStatus {
    PENDING,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    REFUNDED,
    COMPLETED,
    RETURNED,
    FAILED,
    EXPIRED,
    UNKNOWN
}