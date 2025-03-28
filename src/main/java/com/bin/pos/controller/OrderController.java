package com.bin.pos.controller;

import com.bin.pos.dal.model.OrderStatus;
import com.bin.pos.dal.model.OrderTransaction;
import com.bin.pos.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<List<OrderTransaction>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<OrderTransaction> getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-order-id/{orderId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<OrderTransaction> getOrderByOrderId(@PathVariable String orderId) {
        return orderService.getOrderByOrderId(orderId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<List<OrderTransaction>> getOrdersByCustomer(@PathVariable String customerId) {
        return ResponseEntity.ok(orderService.getOrdersByCustomer(customerId));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<List<OrderTransaction>> getOrdersByStatus(@PathVariable OrderStatus status) {
        return ResponseEntity.ok(orderService.getOrdersByStatus(status));
    }

    @GetMapping("/period")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<OrderTransaction>> getOrdersInPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(orderService.getOrdersInPeriod(start, end));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE', 'CASHIER')")
    public ResponseEntity<OrderTransaction> createOrder(@RequestBody OrderTransaction order) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(order));
    }

    @PostMapping("/{id}/items")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE', 'CASHIER')")
    public ResponseEntity<OrderTransaction> addItemToOrder(
            @PathVariable Long id,
            @RequestParam String itemId,
            @RequestParam int quantity,
            @RequestParam(required = false) BigDecimal unitPrice) {
        OrderTransaction updated = orderService.addItemToOrder(id, itemId, quantity, unitPrice);
        return updated != null ?
                ResponseEntity.ok(updated) :
                ResponseEntity.badRequest().build();
    }

    @PostMapping("/by-order-id/{orderId}/items")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE', 'CASHIER')")
    public ResponseEntity<OrderTransaction> addItemToOrderByOrderId(
            @PathVariable String orderId,
            @RequestParam String itemId,
            @RequestParam int quantity,
            @RequestParam(required = false) BigDecimal unitPrice) {
        OrderTransaction updated = orderService.addItemToOrderByOrderId(orderId, itemId, quantity, unitPrice);
        return updated != null ?
                ResponseEntity.ok(updated) :
                ResponseEntity.badRequest().build();
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<OrderTransaction> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam OrderStatus status) {
        OrderTransaction updated = orderService.updateOrderStatus(id, status);
        return updated != null ?
                ResponseEntity.ok(updated) :
                ResponseEntity.notFound().build();
    }

    @PutMapping("/by-order-id/{orderId}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<OrderTransaction> updateOrderStatusByOrderId(
            @PathVariable String orderId,
            @RequestParam OrderStatus status) {
        OrderTransaction updated = orderService.updateOrderStatusByOrderId(orderId, status);
        return updated != null ?
                ResponseEntity.ok(updated) :
                ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}/tracking")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<OrderTransaction> updateTrackingInfo(
            @PathVariable Long id,
            @RequestParam String trackingNumber) {
        OrderTransaction updated = orderService.updateTrackingInfo(id, trackingNumber);
        return updated != null ?
                ResponseEntity.ok(updated) :
                ResponseEntity.notFound().build();
    }

    @PutMapping("/by-order-id/{orderId}/tracking")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<OrderTransaction> updateTrackingInfoByOrderId(
            @PathVariable String orderId,
            @RequestParam String trackingNumber) {
        OrderTransaction updated = orderService.updateTrackingInfoByOrderId(orderId, trackingNumber);
        return updated != null ?
                ResponseEntity.ok(updated) :
                ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<OrderTransaction> cancelOrder(@PathVariable Long id) {
        try {
            OrderTransaction cancelled = orderService.cancelOrder(id);
            return cancelled != null ?
                    ResponseEntity.ok(cancelled) :
                    ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/by-order-id/{orderId}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<OrderTransaction> cancelOrderByOrderId(@PathVariable String orderId) {
        try {
            OrderTransaction cancelled = orderService.cancelOrderByOrderId(orderId);
            return cancelled != null ?
                    ResponseEntity.ok(cancelled) :
                    ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}/refund")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<OrderTransaction> processOrderRefund(@PathVariable Long id) {
        try {
            OrderTransaction refunded = orderService.processOrderRefund(id);
            return refunded != null ?
                    ResponseEntity.ok(refunded) :
                    ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/by-order-id/{orderId}/refund")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<OrderTransaction> processOrderRefundByOrderId(@PathVariable String orderId) {
        try {
            OrderTransaction refunded = orderService.processOrderRefundByOrderId(orderId);
            return refunded != null ?
                    ResponseEntity.ok(refunded) :
                    ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}