package com.bin.pos.controller;


import com.bin.pos.dal.model.OrderStatus;
import com.bin.pos.dal.model.OrderTransaction;
import com.bin.pos.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<List<OrderTransaction>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderTransaction> getOrderById(@PathVariable String orderId) {
        return orderService.getOrderById(orderId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<OrderTransaction>> getOrdersByCustomer(@PathVariable String customerId) {
        return ResponseEntity.ok(orderService.getOrdersByCustomer(customerId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderTransaction>> getOrdersByStatus(@PathVariable OrderStatus status) {
        return ResponseEntity.ok(orderService.getOrdersByStatus(status));
    }

    @GetMapping("/period")
    public ResponseEntity<List<OrderTransaction>> getOrdersInPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(orderService.getOrdersInPeriod(start, end));
    }

    @PostMapping
    public ResponseEntity<OrderTransaction> createOrder(@RequestBody OrderTransaction order) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(order));
    }

    @PostMapping("/{orderId}/items")
    public ResponseEntity<OrderTransaction> addItemToOrder(
            @PathVariable String orderId,
            @RequestBody Map<String, Object> request) {

        String itemId = (String) request.get("itemId");
        int quantity = (Integer) request.get("quantity");
        BigDecimal unitPrice = request.get("unitPrice") != null ?
                new BigDecimal(request.get("unitPrice").toString()) : null;

        try {
            OrderTransaction updated = orderService.addItemToOrder(orderId, itemId, quantity, unitPrice);
            return updated != null ?
                    ResponseEntity.ok(updated) :
                    ResponseEntity.badRequest().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrderTransaction> updateOrderStatus(
            @PathVariable String orderId,
            @RequestParam OrderStatus status) {
        OrderTransaction updated = orderService.updateOrderStatus(orderId, status);
        return updated != null ?
                ResponseEntity.ok(updated) :
                ResponseEntity.notFound().build();
    }

    @PutMapping("/{orderId}/tracking")
    public ResponseEntity<OrderTransaction> updateTrackingInfo(
            @PathVariable String orderId,
            @RequestParam String trackingNumber) {
        OrderTransaction updated = orderService.updateTrackingInfo(orderId, trackingNumber);
        return updated != null ?
                ResponseEntity.ok(updated) :
                ResponseEntity.notFound().build();
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderTransaction> cancelOrder(@PathVariable String orderId) {
        try {
            OrderTransaction cancelled = orderService.cancelOrder(orderId);
            return cancelled != null ?
                    ResponseEntity.ok(cancelled) :
                    ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/{orderId}/refund")
    public ResponseEntity<OrderTransaction> processOrderRefund(@PathVariable String orderId) {
        try {
            OrderTransaction refunded = orderService.processOrderRefund(orderId);
            return refunded != null ?
                    ResponseEntity.ok(refunded) :
                    ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}
