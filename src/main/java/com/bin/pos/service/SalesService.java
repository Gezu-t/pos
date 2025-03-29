package com.bin.pos.service;

import com.bin.pos.dal.dto.TransactionDTO;
import com.bin.pos.dal.model.*;
import com.bin.pos.dal.repository.InventoryRepository;
import com.bin.pos.dal.repository.PaymentRepository;
import com.bin.pos.dal.repository.SalesRepository;
import com.bin.pos.util.DTOConverter;
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
    private final DTOConverter dtoConverter;

    @Autowired
    public SalesService(
            SalesRepository salesRepository,
            InventoryRepository inventoryRepository,
            PaymentRepository paymentRepository,
            InventoryService inventoryService,
            DTOConverter dtoConverter) {
        this.salesRepository = salesRepository;
        this.inventoryRepository = inventoryRepository;
        this.paymentRepository = paymentRepository;
        this.inventoryService = inventoryService;
        this.dtoConverter = dtoConverter;
    }

    /**
     * Get all transactions as DTOs
     */
    @Transactional(readOnly = true)
    public List<TransactionDTO> getAllTransactionsAsDTO() {
        List<SalesTransaction> transactions = salesRepository.findAllWithBasicDetails();
        return dtoConverter.convertToDTO(transactions);
    }

    /**
     * Get transaction by ID as DTO
     */
    @Transactional(readOnly = true)
    public Optional<TransactionDTO> getTransactionByIdAsDTO(Long id) {
        return salesRepository.findByIdWithDetails(id)
                .map(dtoConverter::convertToDTO);
    }

    /**
     * Get transaction by transaction ID as DTO
     */
    @Transactional(readOnly = true)
    public Optional<TransactionDTO> getTransactionByTransactionIdAsDTO(String transactionId) {
        return salesRepository.findByTransactionIdWithDetails(transactionId)
                .map(dtoConverter::convertToDTO);
    }

    /**
     * Get all transactions by customer as DTOs
     */
    @Transactional(readOnly = true)
    public List<TransactionDTO> getTransactionsByCustomerAsDTO(String customerId) {
        List<SalesTransaction> transactions = salesRepository.findByCustomerCustomerId(customerId);
        return dtoConverter.convertToDTO(transactions);
    }

    /**
     * Get all transactions by status as DTOs
     */
    @Transactional(readOnly = true)
    public List<TransactionDTO> getTransactionsByStatusAsDTO(TransactionStatus status) {
        List<SalesTransaction> transactions = salesRepository.findByStatus(status);
        return dtoConverter.convertToDTO(transactions);
    }

    /**
     * Get all transactions in period as DTOs
     */
    @Transactional(readOnly = true)
    public List<TransactionDTO> getTransactionsInPeriodAsDTO(LocalDateTime start, LocalDateTime end) {
        List<SalesTransaction> transactions = salesRepository.findTransactionsInPeriod(start, end);
        return dtoConverter.convertToDTO(transactions);
    }

    // Original methods for entity operations
    public List<SalesTransaction> getAllTransactions() {
        return salesRepository.findAll();
    }

    public Optional<SalesTransaction> getTransactionById(Long id) {
        return salesRepository.findById(id);
    }

    public Optional<SalesTransaction> getTransactionByTransactionId(String transactionId) {
        return salesRepository.findByTransactionId(transactionId);
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
        // Generate a business identifier if not provided
        if (transaction.getTransactionId() == null || transaction.getTransactionId().isEmpty()) {
            transaction.setTransactionId("TRX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }

        // Check if customer is present
        if (transaction.getCustomer() == null) {
            throw new IllegalArgumentException("Customer is required for transactions. Customer cannot be null.");
        }

        // Set initial transaction values
        transaction.setCreationTime(LocalDateTime.now());
        transaction.setStatus(TransactionStatus.DRAFT);

        // Set default payment method if not provided
        if (transaction.getPaymentMethod() == null) {
            transaction.setPaymentMethod(PaymentMethod.CASH);
        }

        if (transaction.getAmountPaid() == null) {
            transaction.setAmountPaid(BigDecimal.ZERO);
        }

        // Ensure tax rate is set
        if (transaction.getTaxRate() == null) {
            transaction.setTaxRate(BigDecimal.ZERO);
        }

        // Save the transaction
        SalesTransaction savedTransaction = salesRepository.save(transaction);

        // Process transaction items if provided
        if (transaction.getItems() != null && !transaction.getItems().isEmpty()) {
            for (TransactionItem item : transaction.getItems()) {
                // Make sure the item reference is set
                if (item.getItem() == null) {
                    throw new IllegalArgumentException("Item reference cannot be null for transaction items");
                }

                // Set the reference to the saved transaction
                item.setSalesTransaction(savedTransaction);

                // Set default values if missing
                if (item.getDiscountAmount() == null) {
                    item.setDiscountAmount(BigDecimal.ZERO);
                }
            }
        }

        // Save again with the updated items
        return salesRepository.save(savedTransaction);
    }

    @Transactional
    public SalesTransaction addItemToTransaction(Long transactionId, Long itemId, int quantity, BigDecimal unitPrice) {
        Optional<SalesTransaction> transactionOpt = salesRepository.findById(transactionId);
        Optional<InventoryItem> itemOpt = inventoryRepository.findById(itemId);

        if (transactionOpt.isPresent() && itemOpt.isPresent()) {
            SalesTransaction transaction = transactionOpt.get();
            InventoryItem item = itemOpt.get();

            // Check if item is already in the transaction
            Optional<TransactionItem> existingItem = transaction.getItems().stream()
                    .filter(i -> i.getItem().getId().equals(itemId))
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
    public SalesTransaction addItemToTransactionByIds(String transactionId, String itemId, int quantity, BigDecimal unitPrice) {
        Optional<SalesTransaction> transactionOpt = salesRepository.findByTransactionId(transactionId);
        Optional<InventoryItem> itemOpt = inventoryRepository.findByItemId(itemId);

        if (transactionOpt.isPresent() && itemOpt.isPresent()) {
            return addItemToTransaction(transactionOpt.get().getId(), itemOpt.get().getId(), quantity, unitPrice);
        }

        return null;
    }

    @Transactional
    public boolean removeItemFromTransaction(Long transactionId, Long transactionItemId) {
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
    public boolean removeItemFromTransactionByTransactionId(String transactionId, Long transactionItemId) {
        Optional<SalesTransaction> transactionOpt = salesRepository.findByTransactionId(transactionId);
        if (transactionOpt.isPresent()) {
            return removeItemFromTransaction(transactionOpt.get().getId(), transactionItemId);
        }
        return false;
    }

    @Transactional
    public SalesTransaction updateItemQuantity(Long transactionId, Long transactionItemId, int newQuantity) {
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
    public SalesTransaction updateItemQuantityByTransactionId(String transactionId, Long transactionItemId, int newQuantity) {
        Optional<SalesTransaction> transactionOpt = salesRepository.findByTransactionId(transactionId);
        if (transactionOpt.isPresent()) {
            return updateItemQuantity(transactionOpt.get().getId(), transactionItemId, newQuantity);
        }
        return null;
    }

    @Transactional
    public SalesTransaction applyDiscount(Long transactionId, Long transactionItemId, BigDecimal discountAmount) {
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
    public SalesTransaction applyDiscountByTransactionId(String transactionId, Long transactionItemId, BigDecimal discountAmount) {
        Optional<SalesTransaction> transactionOpt = salesRepository.findByTransactionId(transactionId);
        if (transactionOpt.isPresent()) {
            return applyDiscount(transactionOpt.get().getId(), transactionItemId, discountAmount);
        }
        return null;
    }

    @Transactional
    public SalesTransaction processPayment(Long transactionId, PaymentMethod paymentMethod, BigDecimal amount, String referenceNumber) {
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
            for (TransactionItem item : transaction.getItems()) {
                inventoryService.updateItemQuantity(item.getItem().getId(), -item.getQuantity());
            }

            return salesRepository.save(transaction);
        }

        return null;
    }

    @Transactional
    public SalesTransaction processPaymentByTransactionId(String transactionId, PaymentMethod paymentMethod, BigDecimal amount, String referenceNumber) {
        Optional<SalesTransaction> transactionOpt = salesRepository.findByTransactionId(transactionId);
        if (transactionOpt.isPresent()) {
            return processPayment(transactionOpt.get().getId(), paymentMethod, amount, referenceNumber);
        }
        return null;
    }

    @Transactional
    public SalesTransaction voidTransaction(Long transactionId) {
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
    public SalesTransaction voidTransactionByTransactionId(String transactionId) {
        Optional<SalesTransaction> transactionOpt = salesRepository.findByTransactionId(transactionId);
        if (transactionOpt.isPresent()) {
            try {
                return voidTransaction(transactionOpt.get().getId());
            } catch (IllegalStateException e) {
                throw e;
            }
        }
        return null;
    }

    @Transactional
    public SalesTransaction processReturn(Long transactionId) {
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
                for (TransactionItem item : transaction.getItems()) {
                    inventoryService.updateItemQuantity(item.getItem().getId(), item.getQuantity());
                }

                // Update transaction status
                transaction.setStatus(TransactionStatus.RETURNED);
                return salesRepository.save(transaction);
            } else {
                throw new IllegalStateException("Can only return completed transactions");
            }
        }

        return null;
    }

    @Transactional
    public SalesTransaction processReturnByTransactionId(String transactionId) {
        Optional<SalesTransaction> transactionOpt = salesRepository.findByTransactionId(transactionId);
        if (transactionOpt.isPresent()) {
            try {
                return processReturn(transactionOpt.get().getId());
            } catch (IllegalStateException e) {
                throw e;
            }
        }
        return null;
    }
}