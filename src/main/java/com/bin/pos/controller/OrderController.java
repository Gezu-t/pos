package com.bin.pos.controller;

import com.bin.pos.dal.dto.OrderTransactionDTO;
import com.bin.pos.dal.model.OrderStatus;
import com.bin.pos.dal.model.OrderTransaction;
import com.bin.pos.service.OrderService;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
    public ResponseEntity<List<OrderTransactionDTO>> getAllOrders() {
        List<OrderTransactionDTO> orderDTOs = orderService.getAllOrders()
                .stream()
                .map(OrderTransactionDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(orderDTOs);
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
    public ResponseEntity<OrderTransactionDTO> getOrderByOrderId(@PathVariable String orderId) {
        if (orderId == null || orderId.equals("undefined") || orderId.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Optional<OrderTransaction> orderOpt = orderService.getOrderByOrderId(orderId);
        return orderOpt.map(OrderTransactionDTO::new)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<List<OrderTransaction>> getOrdersByCustomer(@PathVariable String customerId) {
        if (customerId == null || customerId.equals("undefined") || customerId.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
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
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<OrderTransaction> createOrder(@RequestBody OrderTransaction order) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(order));
    }

    @PostMapping("/{id}/items")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<OrderTransaction> addItemToOrder(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {

        try {
            Long itemId = Long.valueOf(request.get("itemId").toString());
            int quantity = Integer.parseInt(request.get("quantity").toString());
            BigDecimal unitPrice = request.get("unitPrice") != null ?
                    new BigDecimal(request.get("unitPrice").toString()) : null;

            OrderTransaction updated = orderService.addItemToOrder(id, itemId, quantity, unitPrice);
            return updated != null ?
                    ResponseEntity.ok(updated) :
                    ResponseEntity.badRequest().build();
        } catch (NumberFormatException | NullPointerException e) {
            return ResponseEntity.badRequest().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }
    }

    @PostMapping("/by-order-id/{orderId}/items")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<OrderTransaction> addItemToOrderByOrderId(
            @PathVariable String orderId,
            @RequestBody Map<String, Object> request) {

        if (orderId == null || orderId.equals("undefined") || orderId.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            String itemId = request.get("itemId").toString();
            int quantity = Integer.parseInt(request.get("quantity").toString());
            BigDecimal unitPrice = request.get("unitPrice") != null ?
                    new BigDecimal(request.get("unitPrice").toString()) : null;

            OrderTransaction updated = orderService.addItemToOrderByIds(orderId, itemId, quantity, unitPrice);
            return updated != null ?
                    ResponseEntity.ok(updated) :
                    ResponseEntity.badRequest().build();
        } catch (NumberFormatException | NullPointerException e) {
            return ResponseEntity.badRequest().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }
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
    @Transactional
    public ResponseEntity<OrderTransaction> updateOrderStatusByOrderId(
            @PathVariable String orderId,
            @RequestParam OrderStatus status) {
        if (orderId == null || orderId.equals("undefined") || orderId.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        OrderTransaction updated = orderService.updateOrderStatusByOrderId(orderId, status);

        if (updated != null) {
            // Force initialization of the items collection
            updated.getItems().size();
            return ResponseEntity.ok(updated);
        } else {
            return ResponseEntity.notFound().build();
        }
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
        if (orderId == null || orderId.equals("undefined") || orderId.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
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
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }
    }

    @PutMapping("/by-order-id/{orderId}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<OrderTransaction> cancelOrderByOrderId(@PathVariable String orderId) {
        if (orderId == null || orderId.equals("undefined") || orderId.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        try {
            OrderTransaction cancelled = orderService.cancelOrderByOrderId(orderId);
            return cancelled != null ?
                    ResponseEntity.ok(cancelled) :
                    ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
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
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }
    }

    @PutMapping("/by-order-id/{orderId}/refund")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<OrderTransaction> processOrderRefundByOrderId(@PathVariable String orderId) {
        if (orderId == null || orderId.equals("undefined") || orderId.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        try {
            OrderTransaction refunded = orderService.processOrderRefundByOrderId(orderId);
            return refunded != null ?
                    ResponseEntity.ok(refunded) :
                    ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }
    }
}