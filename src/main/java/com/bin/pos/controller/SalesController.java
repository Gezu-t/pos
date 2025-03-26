package com.bin.pos.controller;


import com.bin.pos.dal.model.PaymentMethod;
import com.bin.pos.dal.model.SalesTransaction;
import com.bin.pos.dal.model.TransactionStatus;
import com.bin.pos.service.SalesService;
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
@RequestMapping("/api/sales")
public class SalesController {

    private final SalesService salesService;

    @Autowired
    public SalesController(SalesService salesService) {
        this.salesService = salesService;
    }

    @GetMapping
    public ResponseEntity<List<SalesTransaction>> getAllTransactions() {
        return ResponseEntity.ok(salesService.getAllTransactions());
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<SalesTransaction> getTransactionById(@PathVariable String transactionId) {
        return salesService.getTransactionById(transactionId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<SalesTransaction>> getTransactionsByCustomer(@PathVariable String customerId) {
        return ResponseEntity.ok(salesService.getTransactionsByCustomer(customerId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<SalesTransaction>> getTransactionsByStatus(@PathVariable TransactionStatus status) {
        return ResponseEntity.ok(salesService.getTransactionsByStatus(status));
    }

    @GetMapping("/period")
    public ResponseEntity<List<SalesTransaction>> getTransactionsInPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(salesService.getTransactionsInPeriod(start, end));
    }

    @PostMapping
    public ResponseEntity<SalesTransaction> createTransaction(@RequestBody SalesTransaction transaction) {
        return ResponseEntity.status(HttpStatus.CREATED).body(salesService.createTransaction(transaction));
    }

    @PostMapping("/{transactionId}/items")
    public ResponseEntity<SalesTransaction> addItemToTransaction(
            @PathVariable String transactionId,
            @RequestBody Map<String, Object> request) {

        String itemId = (String) request.get("itemId");
        int quantity = (Integer) request.get("quantity");
        BigDecimal unitPrice = request.get("unitPrice") != null ?
                new BigDecimal(request.get("unitPrice").toString()) : null;

        SalesTransaction updated = salesService.addItemToTransaction(transactionId, itemId, quantity, unitPrice);
        return updated != null ?
                ResponseEntity.ok(updated) :
                ResponseEntity.badRequest().build();
    }

    @DeleteMapping("/{transactionId}/items/{itemId}")
    public ResponseEntity<Void> removeItemFromTransaction(
            @PathVariable String transactionId,
            @PathVariable Long itemId) {
        boolean removed = salesService.removeItemFromTransaction(transactionId, itemId);
        return removed ?
                ResponseEntity.ok().build() :
                ResponseEntity.badRequest().build();
    }

    @PutMapping("/{transactionId}/items/{itemId}/quantity")
    public ResponseEntity<SalesTransaction> updateItemQuantity(
            @PathVariable String transactionId,
            @PathVariable Long itemId,
            @RequestParam int quantity) {
        SalesTransaction updated = salesService.updateItemQuantity(transactionId, itemId, quantity);
        return updated != null ?
                ResponseEntity.ok(updated) :
                ResponseEntity.badRequest().build();
    }

    @PutMapping("/{transactionId}/items/{itemId}/discount")
    public ResponseEntity<SalesTransaction> applyDiscount(
            @PathVariable String transactionId,
            @PathVariable Long itemId,
            @RequestParam BigDecimal amount) {
        SalesTransaction updated = salesService.applyDiscount(transactionId, itemId, amount);
        return updated != null ?
                ResponseEntity.ok(updated) :
                ResponseEntity.badRequest().build();
    }

    @PostMapping("/{transactionId}/payment")
    public ResponseEntity<SalesTransaction> processPayment(
            @PathVariable String transactionId,
            @RequestBody Map<String, Object> request) {

        PaymentMethod paymentMethod = PaymentMethod.valueOf((String) request.get("paymentMethod"));
        BigDecimal amount = new BigDecimal(request.get("amount").toString());
        String referenceNumber = (String) request.get("referenceNumber");

        try {
            SalesTransaction completed = salesService.processPayment(transactionId, paymentMethod, amount, referenceNumber);
            return ResponseEntity.ok(completed);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{transactionId}/void")
    public ResponseEntity<SalesTransaction> voidTransaction(@PathVariable String transactionId) {
        try {
            SalesTransaction voided = salesService.voidTransaction(transactionId);
            return voided != null ?
                    ResponseEntity.ok(voided) :
                    ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{transactionId}/return")
    public ResponseEntity<SalesTransaction> processReturn(@PathVariable String transactionId) {
        try {
            SalesTransaction returned = salesService.processReturn(transactionId);
            return returned != null ?
                    ResponseEntity.ok(returned) :
                    ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}