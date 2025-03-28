package com.bin.pos.service;

import com.bin.pos.dal.model.InventoryItem;
import com.bin.pos.dal.model.OrderItem;
import com.bin.pos.dal.model.OrderStatus;
import com.bin.pos.dal.model.OrderTransaction;
import com.bin.pos.dal.repository.InventoryRepository;
import com.bin.pos.dal.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryService inventoryService;

    @Autowired
    public OrderService(
            OrderRepository orderRepository,
            InventoryRepository inventoryRepository,
            InventoryService inventoryService) {
        this.orderRepository = orderRepository;
        this.inventoryRepository = inventoryRepository;
        this.inventoryService = inventoryService;
    }

    public List<OrderTransaction> getAllOrders() {
        return orderRepository.findAll();
    }

    public Optional<OrderTransaction> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    public Optional<OrderTransaction> getOrderByOrderId(String orderId) {
        return orderRepository.findByOrderId(orderId);
    }

    public List<OrderTransaction> getOrdersByCustomer(String customerId) {
        return orderRepository.findByCustomerCustomerId(customerId);
    }

    public List<OrderTransaction> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    public List<OrderTransaction> getOrdersInPeriod(LocalDateTime start, LocalDateTime end) {
        return orderRepository.findOrdersInPeriod(start, end);
    }

    @Transactional
    public OrderTransaction createOrder(OrderTransaction order) {
        // Generate a unique orderId (business identifier) if not provided
        if (order.getOrderId() == null || order.getOrderId().isEmpty()) {
            order.setOrderId("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }

        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);

        return orderRepository.save(order);
    }

    @Transactional
    public OrderTransaction addItemToOrder(Long orderId, String itemId, int quantity, BigDecimal unitPrice) {
        Optional<OrderTransaction> orderOpt = orderRepository.findById(orderId);
        Optional<InventoryItem> itemOpt = inventoryRepository.findById(itemId);

        if (orderOpt.isPresent() && itemOpt.isPresent()) {
            OrderTransaction order = orderOpt.get();
            InventoryItem item = itemOpt.get();

            // Check if item is in stock
            if (item.getQuantity() < quantity) {
                throw new IllegalStateException("Not enough items in stock");
            }

            // Add order item
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setItem(item);
            orderItem.setQuantity(quantity);
            orderItem.setUnitPrice(unitPrice != null ? unitPrice : item.getPrice());
            orderItem.setDiscountAmount(BigDecimal.ZERO);

            // Reevaluate totals
            recalculateOrderTotals(order);

            return orderRepository.save(order);
        }

        return null;
    }

    @Transactional
    public OrderTransaction addItemToOrderByOrderId(String orderId, String itemId, int quantity, BigDecimal unitPrice) {
        Optional<OrderTransaction> orderOpt = orderRepository.findByOrderId(orderId);

        if (orderOpt.isPresent()) {
            return addItemToOrder(orderOpt.get().getId(), itemId, quantity, unitPrice);
        }

        return null;
    }

    private void recalculateOrderTotals(OrderTransaction order) {
        // Implementation would calculate subtotal, tax, and total amount
        // This is simplified for this example
    }

    @Transactional
    public OrderTransaction updateOrderStatus(Long id, OrderStatus status) {
        Optional<OrderTransaction> orderOpt = orderRepository.findById(id);

        if (orderOpt.isPresent()) {
            OrderTransaction order = orderOpt.get();
            order.setStatus(status);

            // Update delivery date if status is DELIVERED
            if (status == OrderStatus.DELIVERED) {
                order.setActualDeliveryDate(LocalDateTime.now());
            }

            return orderRepository.save(order);
        }

        return null;
    }

    @Transactional
    public OrderTransaction updateOrderStatusByOrderId(String orderId, OrderStatus status) {
        Optional<OrderTransaction> orderOpt = orderRepository.findByOrderId(orderId);

        if (orderOpt.isPresent()) {
            return updateOrderStatus(orderOpt.get().getId(), status);
        }

        return null;
    }

    @Transactional
    public OrderTransaction updateTrackingInfo(Long id, String trackingNumber) {
        Optional<OrderTransaction> orderOpt = orderRepository.findById(id);

        if (orderOpt.isPresent()) {
            OrderTransaction order = orderOpt.get();
            order.setTrackingNumber(trackingNumber);
            order.setStatus(OrderStatus.SHIPPED);
            return orderRepository.save(order);
        }

        return null;
    }

    @Transactional
    public OrderTransaction updateTrackingInfoByOrderId(String orderId, String trackingNumber) {
        Optional<OrderTransaction> orderOpt = orderRepository.findByOrderId(orderId);

        if (orderOpt.isPresent()) {
            return updateTrackingInfo(orderOpt.get().getId(), trackingNumber);
        }

        return null;
    }

    @Transactional
    public OrderTransaction cancelOrder(Long id) {
        Optional<OrderTransaction> orderOpt = orderRepository.findById(id);

        if (orderOpt.isPresent()) {
            OrderTransaction order = orderOpt.get();

            if (order.getStatus() == OrderStatus.PENDING || order.getStatus() == OrderStatus.PROCESSING) {
                order.setStatus(OrderStatus.CANCELLED);
                return orderRepository.save(order);
            } else {
                throw new IllegalStateException("Cannot cancel order that has been shipped or delivered");
            }
        }

        return null;
    }

    @Transactional
    public OrderTransaction cancelOrderByOrderId(String orderId) {
        Optional<OrderTransaction> orderOpt = orderRepository.findByOrderId(orderId);

        if (orderOpt.isPresent()) {
            return cancelOrder(orderOpt.get().getId());
        }

        return null;
    }

    @Transactional
    public OrderTransaction processOrderRefund(Long id) {
        Optional<OrderTransaction> orderOpt = orderRepository.findById(id);

        if (orderOpt.isPresent()) {
            OrderTransaction order = orderOpt.get();

            if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.RETURNED) {
                order.setStatus(OrderStatus.REFUNDED);
                return orderRepository.save(order);
            } else {
                throw new IllegalStateException("Cannot refund order that hasn't been delivered or returned");
            }
        }

        return null;
    }

    @Transactional
    public OrderTransaction processOrderRefundByOrderId(String orderId) {
        Optional<OrderTransaction> orderOpt = orderRepository.findByOrderId(orderId);

        if (orderOpt.isPresent()) {
            return processOrderRefund(orderOpt.get().getId());
        }

        return null;
    }
}