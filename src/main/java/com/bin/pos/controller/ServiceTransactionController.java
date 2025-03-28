package com.bin.pos.controller;


import com.bin.pos.dal.model.SalesTransaction;
import com.bin.pos.service.ServiceTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/services")
public class ServiceTransactionController {

    private final ServiceTransactionService serviceTransactionService;

    @Autowired
    public ServiceTransactionController(ServiceTransactionService serviceTransactionService) {
        this.serviceTransactionService = serviceTransactionService;
    }

    @PostMapping("/transactions/{id}/items")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE', 'CASHIER')")
    public ResponseEntity<SalesTransaction> addServiceToTransaction(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {

        try {
            Long serviceId = Long.valueOf(request.get("serviceId").toString());
            int quantity = Integer.parseInt(request.get("quantity").toString());
            BigDecimal unitPrice = request.get("unitPrice") != null ?
                    new BigDecimal(request.get("unitPrice").toString()) : null;

            SalesTransaction updated = serviceTransactionService.addServiceToTransaction(id, serviceId, quantity, unitPrice);
            return updated != null ?
                    ResponseEntity.ok(updated) :
                    ResponseEntity.badRequest().build();
        } catch (NumberFormatException | NullPointerException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/transactions/by-transaction-id/{transactionId}/items")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE', 'CASHIER')")
    public ResponseEntity<SalesTransaction> addServiceToTransactionByTransactionId(
            @PathVariable String transactionId,
            @RequestBody Map<String, Object> request) {

        if (transactionId == null || transactionId.equals("undefined") || transactionId.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            String serviceId = request.get("serviceId").toString();
            int quantity = Integer.parseInt(request.get("quantity").toString());
            BigDecimal unitPrice = request.get("unitPrice") != null ?
                    new BigDecimal(request.get("unitPrice").toString()) : null;

            SalesTransaction updated = serviceTransactionService.addServiceToTransactionByIds(transactionId, serviceId, quantity, unitPrice);
            return updated != null ?
                    ResponseEntity.ok(updated) :
                    ResponseEntity.badRequest().build();
        } catch (NumberFormatException | NullPointerException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/transactions/{id}/items/{itemId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE', 'CASHIER')")
    public ResponseEntity<Void> removeServiceFromTransaction(
            @PathVariable Long id,
            @PathVariable Long itemId) {
        boolean removed = serviceTransactionService.removeServiceFromTransaction(id, itemId);
        return removed ?
                ResponseEntity.ok().build() :
                ResponseEntity.badRequest().build();
    }

    @DeleteMapping("/transactions/by-transaction-id/{transactionId}/items/{itemId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE', 'CASHIER')")
    public ResponseEntity<Void> removeServiceFromTransactionByTransactionId(
            @PathVariable String transactionId,
            @PathVariable Long itemId) {
        if (transactionId == null || transactionId.equals("undefined") || transactionId.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        boolean removed = serviceTransactionService.removeServiceFromTransactionByTransactionId(transactionId, itemId);
        return removed ?
                ResponseEntity.ok().build() :
                ResponseEntity.badRequest().build();
    }

    @PutMapping("/transactions/{id}/items/{itemId}/notes")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<SalesTransaction> updateServiceNotes(
            @PathVariable Long id,
            @PathVariable Long itemId,
            @RequestParam String notes) {
        SalesTransaction updated = serviceTransactionService.updateServiceNotes(id, itemId, notes);
        return updated != null ?
                ResponseEntity.ok(updated) :
                ResponseEntity.badRequest().build();
    }

    @PutMapping("/transactions/by-transaction-id/{transactionId}/items/{itemId}/notes")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<SalesTransaction> updateServiceNotesByTransactionId(
            @PathVariable String transactionId,
            @PathVariable Long itemId,
            @RequestParam String notes) {
        if (transactionId == null || transactionId.equals("undefined") || transactionId.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        SalesTransaction updated = serviceTransactionService.updateServiceNotesByTransactionId(transactionId, itemId, notes);
        return updated != null ?
                ResponseEntity.ok(updated) :
                ResponseEntity.badRequest().build();
    }

    @PutMapping("/transactions/{id}/items/{itemId}/quantity")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE', 'CASHIER')")
    public ResponseEntity<SalesTransaction> updateServiceQuantity(
            @PathVariable Long id,
            @PathVariable Long itemId,
            @RequestParam int quantity) {
        if (quantity <= 0) {
            return ResponseEntity.badRequest().build();
        }

        SalesTransaction updated = serviceTransactionService.updateServiceQuantity(id, itemId, quantity);
        return updated != null ?
                ResponseEntity.ok(updated) :
                ResponseEntity.badRequest().build();
    }

    @PutMapping("/transactions/by-transaction-id/{transactionId}/items/{itemId}/quantity")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE', 'CASHIER')")
    public ResponseEntity<SalesTransaction> updateServiceQuantityByTransactionId(
            @PathVariable String transactionId,
            @PathVariable Long itemId,
            @RequestParam int quantity) {
        if (transactionId == null || transactionId.equals("undefined") || transactionId.isEmpty() || quantity <= 0) {
            return ResponseEntity.badRequest().build();
        }

        SalesTransaction updated = serviceTransactionService.updateServiceQuantityByTransactionId(transactionId, itemId, quantity);
        return updated != null ?
                ResponseEntity.ok(updated) :
                ResponseEntity.badRequest().build();
    }
}
