package com.bin.pos.service;


import com.bin.pos.dal.model.*;
import com.bin.pos.dal.repository.InventoryRepository;
import com.bin.pos.dal.repository.PaymentRepository;
import com.bin.pos.dal.repository.SalesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SalesService {

    private final SalesRepository salesRepository;
    private final InventoryRepository inventoryRepository;
    private final PaymentRepository paymentRepository;
    private final InventoryService inventoryService;

    @Autowired
    public SalesService(
            SalesRepository salesRepository,
            InventoryRepository inventoryRepository,
            PaymentRepository paymentRepository,
            InventoryService inventoryService) {
        this.salesRepository = salesRepository;
        this.inventoryRepository = inventoryRepository;
        this.paymentRepository = paymentRepository;
        this.inventoryService = inventoryService;
    }

    public List<SalesTransaction> getAllTransactions() {
        return salesRepository.findAll();
    }

    public Optional<SalesTransaction> getTransactionById(String transactionId) {
        return salesRepository.findById(transactionId);
    }

    public List<SalesTransaction> getTransactionsByCustomer(String customerId) {
        return salesRepository.findByCustomerCustomerId(customerId);
    }

    public List<SalesTransaction> getTransactionsByStatus(TransactionStatus status) {
        return salesRepository.findByStatus(status);
    }

    public List<SalesTransaction> getTransactionsInPeriod(LocalDateTime start, LocalDateTime end) {
        return salesRepository.findTransactionsInPeriod(start, end);
    }

    @Transactional
    public SalesTransaction createTransaction(SalesTransaction transaction) {
        transaction.setCreationTime(LocalDateTime.now());
        transaction.setStatus(TransactionStatus.DRAFT);
        return salesRepository.save(transaction);
    }

    @Transactional
    public SalesTransaction addItemToTransaction(String transactionId, String itemId, int quantity, BigDecimal unitPrice) {
        Optional<SalesTransaction> transactionOpt = salesRepository.findById(transactionId);
        Optional<InventoryItem> itemOpt = inventoryRepository.findById(itemId);

        if (transactionOpt.isPresent() && itemOpt.isPresent()) {
            SalesTransaction transaction = transactionOpt.get();
            InventoryItem item = itemOpt.get();

            // Check if item is already in the transaction
            Optional<TransactionItem> existingItem = transaction.getItems().stream()
                    .filter(i -> i.getItem().getItemId().equals(itemId))
                    .findFirst();

            if (existingItem.isPresent()) {
                // Increase quantity of existing item
                TransactionItem transactionItem = existingItem.get();
                transactionItem.setQuantity(transactionItem.getQuantity() + quantity);
            } else {
                // Add new transaction item
                TransactionItem transactionItem = new TransactionItem();
                transactionItem.setSalesTransaction(transaction);
                transactionItem.setItem(item);
                transactionItem.setQuantity(quantity);
                transactionItem.setUnitPrice(unitPrice != null ? unitPrice : item.getPrice());
                transactionItem.setDiscountAmount(BigDecimal.ZERO);
                transaction.getItems().add(transactionItem);
            }

            return salesRepository.save(transaction);
        }

        return null;
    }

    @Transactional
    public boolean removeItemFromTransaction(String transactionId, Long transactionItemId) {
        Optional<SalesTransaction> transactionOpt = salesRepository.findById(transactionId);

        if (transactionOpt.isPresent()) {
            SalesTransaction transaction = transactionOpt.get();
            boolean removed = transaction.getItems().removeIf(item -> item.getId().equals(transactionItemId));

            if (removed) {
                salesRepository.save(transaction);
                return true;
            }
        }

        return false;
    }

    @Transactional
    public SalesTransaction updateItemQuantity(String transactionId, Long transactionItemId, int newQuantity) {
        if (newQuantity < 1) {
            return null;
        }

        Optional<SalesTransaction> transactionOpt = salesRepository.findById(transactionId);

        if (transactionOpt.isPresent()) {
            SalesTransaction transaction = transactionOpt.get();
            transaction.getItems().stream()
                    .filter(item -> item.getId().equals(transactionItemId))
                    .findFirst()
                    .ifPresent(item -> item.setQuantity(newQuantity));

            return salesRepository.save(transaction);
        }

        return null;
    }

    @Transactional
    public SalesTransaction applyDiscount(String transactionId, Long transactionItemId, BigDecimal discountAmount) {
        Optional<SalesTransaction> transactionOpt = salesRepository.findById(transactionId);

        if (transactionOpt.isPresent()) {
            SalesTransaction transaction = transactionOpt.get();
            transaction.getItems().stream()
                    .filter(item -> item.getId().equals(transactionItemId))
                    .findFirst()
                    .ifPresent(item -> item.setDiscountAmount(discountAmount));

            return salesRepository.save(transaction);
        }

        return null;
    }

    @Transactional
    public SalesTransaction processPayment(String transactionId, PaymentMethod paymentMethod, BigDecimal amount, String referenceNumber) {
        Optional<SalesTransaction> transactionOpt = salesRepository.findById(transactionId);

        if (transactionOpt.isPresent()) {
            SalesTransaction transaction = transactionOpt.get();
            BigDecimal total = transaction.getTotal();

            // Validate payment amount
            if (amount.compareTo(total) < 0) {
                throw new IllegalArgumentException("Payment amount must be at least the transaction total");
            }

            // Create payment record
            PaymentTransaction payment = new PaymentTransaction();
            payment.setPaymentId(UUID.randomUUID().toString());
            payment.setSalesTransaction(transaction);
            payment.setAmount(amount);
            payment.setPaymentMethod(paymentMethod);
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setReferenceNumber(referenceNumber);

            paymentRepository.save(payment);

            // Update transaction
            transaction.setStatus(TransactionStatus.COMPLETED);
            transaction.setPaymentMethod(paymentMethod);
            transaction.setAmountPaid(amount);
            transaction.setCompletionTime(LocalDateTime.now());

            // Update inventory quantities
            transaction.getItems().forEach(item -> {
                inventoryService.updateItemQuantity(
                        item.getItem().getItemId(),
                        -item.getQuantity());
            });

            return salesRepository.save(transaction);
        }

        return null;
    }

    @Transactional
    public SalesTransaction voidTransaction(String transactionId) {
        Optional<SalesTransaction> transactionOpt = salesRepository.findById(transactionId);

        if (transactionOpt.isPresent()) {
            SalesTransaction transaction = transactionOpt.get();

            if (transaction.getStatus() != TransactionStatus.COMPLETED) {
                transaction.setStatus(TransactionStatus.CANCELLED);
                return salesRepository.save(transaction);
            } else {
                throw new IllegalStateException("Cannot void a completed transaction");
            }
        }

        return null;
    }

    @Transactional
    public SalesTransaction processReturn(String transactionId) {
        Optional<SalesTransaction> transactionOpt = salesRepository.findById(transactionId);

        if (transactionOpt.isPresent()) {
            SalesTransaction transaction = transactionOpt.get();

            if (transaction.getStatus() == TransactionStatus.COMPLETED) {
                // Create refund payment
                PaymentTransaction refund = new PaymentTransaction();
                refund.setPaymentId(UUID.randomUUID().toString());
                refund.setSalesTransaction(transaction);
                refund.setAmount(transaction.getTotal().negate());
                refund.setPaymentMethod(transaction.getPaymentMethod());
                refund.setStatus(PaymentStatus.REFUNDED);

                paymentRepository.save(refund);

                // Update inventory quantities
                transaction.getItems().forEach(item -> {
                    inventoryService.updateItemQuantity(
                            item.getItem().getItemId(),
                            item.getQuantity());
                });

                // Update transaction status
                transaction.setStatus(TransactionStatus.RETURNED);
                return salesRepository.save(transaction);
            } else {
                throw new IllegalStateException("Can only return completed transactions");
            }
        }

        return null;
    }
}