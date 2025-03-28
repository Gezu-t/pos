package com.bin.pos.controller;

import com.bin.pos.dal.model.PaymentMethod;
import com.bin.pos.dal.model.SalesTransaction;
import com.bin.pos.dal.model.TransactionStatus;
import com.bin.pos.service.SalesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sales")
public class SalesController {

    private final SalesService salesService;

    @Autowired
    public SalesController(SalesService salesService) {
        this.salesService = salesService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<List<SalesTransaction>> getAllTransactions() {
        return ResponseEntity.ok(salesService.getAllTransactions());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE', 'CASHIER')")
    public ResponseEntity<SalesTransaction> getTransactionById(@PathVariable Long id) {
        return salesService.getTransactionById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-transaction-id/{transactionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE', 'CASHIER')")
    public ResponseEntity<SalesTransaction> getTransactionByTransactionId(@PathVariable String transactionId) {
        if (transactionId == null || transactionId.equals("undefined") || transactionId.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return salesService.getTransactionByTransactionId(transactionId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<List<SalesTransaction>> getTransactionsByCustomer(@PathVariable String customerId) {
        if (customerId == null || customerId.equals("undefined") || customerId.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(salesService.getTransactionsByCustomer(customerId));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<List<SalesTransaction>> getTransactionsByStatus(@PathVariable TransactionStatus status) {
        return ResponseEntity.ok(salesService.getTransactionsByStatus(status));
    }

    @GetMapping("/period")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<SalesTransaction>> getTransactionsInPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(salesService.getTransactionsInPeriod(start, end));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE', 'CASHIER')")
    public ResponseEntity<SalesTransaction> createTransaction(@RequestBody SalesTransaction transaction) {
        return ResponseEntity.status(HttpStatus.CREATED).body(salesService.createTransaction(transaction));
    }

    @PostMapping("/{id}/items")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE', 'CASHIER')")
    public ResponseEntity<SalesTransaction> addItemToTransaction(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {

        try {
            Long itemId = Long.valueOf(request.get("itemId").toString());
            int quantity = Integer.parseInt(request.get("quantity").toString());
            BigDecimal unitPrice = request.get("unitPrice") != null ?
                    new BigDecimal(request.get("unitPrice").toString()) : null;

            SalesTransaction updated = salesService.addItemToTransaction(id, itemId, quantity, unitPrice);
            return updated != null ?
                    ResponseEntity.ok(updated) :
                    ResponseEntity.badRequest().build();
        } catch (NumberFormatException | NullPointerException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/by-transaction-id/{transactionId}/items")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE', 'CASHIER')")
    public ResponseEntity<SalesTransaction> addItemToTransactionByTransactionId(
            @PathVariable String transactionId,
            @RequestBody Map<String, Object> request) {

        if (transactionId == null || transactionId.equals("undefined") || transactionId.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            String itemId = request.get("itemId").toString();
            int quantity = Integer.parseInt(request.get("quantity").toString());
            BigDecimal unitPrice = request.get("unitPrice") != null ?
                    new BigDecimal(request.get("unitPrice").toString()) : null;

            SalesTransaction updated = salesService.addItemToTransactionByIds(transactionId, itemId, quantity, unitPrice);
            return updated != null ?
                    ResponseEntity.ok(updated) :
                    ResponseEntity.badRequest().build();
        } catch (NumberFormatException | NullPointerException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}/items/{itemId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE', 'CASHIER')")
    public ResponseEntity<Void> removeItemFromTransaction(
            @PathVariable Long id,
            @PathVariable Long itemId) {
        boolean removed = salesService.removeItemFromTransaction(id, itemId);
        return removed ?
                ResponseEntity.ok().build() :
                ResponseEntity.badRequest().build();
    }

    @DeleteMapping("/by-transaction-id/{transactionId}/items/{itemId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE', 'CASHIER')")
    public ResponseEntity<Void> removeItemFromTransactionByTransactionId(
            @PathVariable String transactionId,
            @PathVariable Long itemId) {
        if (transactionId == null || transactionId.equals("undefined") || transactionId.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        boolean removed = salesService.removeItemFromTransactionByTransactionId(transactionId, itemId);
        return removed ?
                ResponseEntity.ok().build() :
                ResponseEntity.badRequest().build();
    }

    @PutMapping("/{id}/items/{itemId}/quantity")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE', 'CASHIER')")
    public ResponseEntity<SalesTransaction> updateItemQuantity(
            @PathVariable Long id,
            @PathVariable Long itemId,
            @RequestParam int quantity) {
        SalesTransaction updated = salesService.updateItemQuantity(id, itemId, quantity);
        return updated != null ?
                ResponseEntity.ok(updated) :
                ResponseEntity.badRequest().build();
    }

    @PutMapping("/by-transaction-id/{transactionId}/items/{itemId}/quantity")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE', 'CASHIER')")
    public ResponseEntity<SalesTransaction> updateItemQuantityByTransactionId(
            @PathVariable String transactionId,
            @PathVariable Long itemId,
            @RequestParam int quantity) {
        if (transactionId == null || transactionId.equals("undefined") || transactionId.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        SalesTransaction updated = salesService.updateItemQuantityByTransactionId(transactionId, itemId, quantity);
        return updated != null ?
                ResponseEntity.ok(updated) :
                ResponseEntity.badRequest().build();
    }

    @PutMapping("/{id}/items/{itemId}/discount")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<SalesTransaction> applyDiscount(
            @PathVariable Long id,
            @PathVariable Long itemId,
            @RequestParam BigDecimal amount) {
        SalesTransaction updated = salesService.applyDiscount(id, itemId, amount);
        return updated != null ?
                ResponseEntity.ok(updated) :
                ResponseEntity.badRequest().build();
    }

    @PutMapping("/by-transaction-id/{transactionId}/items/{itemId}/discount")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<SalesTransaction> applyDiscountByTransactionId(
            @PathVariable String transactionId,
            @PathVariable Long itemId,
            @RequestParam BigDecimal amount) {
        if (transactionId == null || transactionId.equals("undefined") || transactionId.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        SalesTransaction updated = salesService.applyDiscountByTransactionId(transactionId, itemId, amount);
        return updated != null ?
                ResponseEntity.ok(updated) :
                ResponseEntity.badRequest().build();
    }

    @PostMapping("/{id}/payment")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE', 'CASHIER')")
    public ResponseEntity<SalesTransaction> processPayment(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {

        try {
            PaymentMethod paymentMethod = PaymentMethod.valueOf((String) request.get("paymentMethod"));
            BigDecimal amount = new BigDecimal(request.get("amount").toString());
            String referenceNumber = (String) request.get("referenceNumber");

            SalesTransaction completed = salesService.processPayment(id, paymentMethod, amount, referenceNumber);
            return ResponseEntity.ok(completed);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/by-transaction-id/{transactionId}/payment")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE', 'CASHIER')")
    public ResponseEntity<SalesTransaction> processPaymentByTransactionId(
            @PathVariable String transactionId,
            @RequestBody Map<String, Object> request) {

        if (transactionId == null || transactionId.equals("undefined") || transactionId.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            PaymentMethod paymentMethod = PaymentMethod.valueOf((String) request.get("paymentMethod"));
            BigDecimal amount = new BigDecimal(request.get("amount").toString());
            String referenceNumber = (String) request.get("referenceNumber");

            SalesTransaction completed = salesService.processPaymentByTransactionId(transactionId, paymentMethod, amount, referenceNumber);
            return ResponseEntity.ok(completed);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/void")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<SalesTransaction> voidTransaction(@PathVariable Long id) {
        try {
            SalesTransaction voided = salesService.voidTransaction(id);
            return voided != null ?
                    ResponseEntity.ok(voided) :
                    ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/by-transaction-id/{transactionId}/void")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<SalesTransaction> voidTransactionByTransactionId(@PathVariable String transactionId) {
        if (transactionId == null || transactionId.equals("undefined") || transactionId.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            SalesTransaction voided = salesService.voidTransactionByTransactionId(transactionId);
            return voided != null ?
                    ResponseEntity.ok(voided) :
                    ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/return")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<SalesTransaction> processReturn(@PathVariable Long id) {
        try {
            SalesTransaction returned = salesService.processReturn(id);
            return returned != null ?
                    ResponseEntity.ok(returned) :
                    ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/by-transaction-id/{transactionId}/return")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<SalesTransaction> processReturnByTransactionId(@PathVariable String transactionId) {
        if (transactionId == null || transactionId.equals("undefined") || transactionId.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            SalesTransaction returned = salesService.processReturnByTransactionId(transactionId);
            return returned != null ?
                    ResponseEntity.ok(returned) :
                    ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}