package com.bin.pos.controller;

import com.bin.pos.dal.dto.ApiResponse;
import com.bin.pos.dal.dto.TransactionDTO;
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
    public ResponseEntity<?> getAllTransactions() {
        try {
            List<TransactionDTO> transactions = salesService.getAllTransactionsAsDTO();
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "An unexpected error occurred: " + e.getMessage(), null, LocalDateTime.now()));
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<?> getTransactionById(@PathVariable Long id) {
        try {
            return salesService.getTransactionByIdAsDTO(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "An unexpected error occurred: " + e.getMessage(), null, LocalDateTime.now()));
        }
    }

    @GetMapping("/by-transaction-id/{transactionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<?> getTransactionByTransactionId(@PathVariable String transactionId) {
        try {
            return salesService.getTransactionByTransactionIdAsDTO(transactionId)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "An unexpected error occurred: " + e.getMessage(), null, LocalDateTime.now()));
        }
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<?> getTransactionsByCustomer(@PathVariable String customerId) {
        try {
            List<TransactionDTO> transactions = salesService.getTransactionsByCustomerAsDTO(customerId);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "An unexpected error occurred: " + e.getMessage(), null, LocalDateTime.now()));
        }
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<?> getTransactionsByStatus(@PathVariable TransactionStatus status) {
        try {
            List<TransactionDTO> transactions = salesService.getTransactionsByStatusAsDTO(status);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "An unexpected error occurred: " + e.getMessage(), null, LocalDateTime.now()));
        }
    }

    @GetMapping("/period")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<?> getTransactionsInPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        try {
            List<TransactionDTO> transactions = salesService.getTransactionsInPeriodAsDTO(start, end);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "An unexpected error occurred: " + e.getMessage(), null, LocalDateTime.now()));
        }
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<?> createTransaction(@RequestBody SalesTransaction transaction) {
        try {
            SalesTransaction createdTransaction = salesService.createTransaction(transaction);
            // Convert to DTO for the response
            TransactionDTO dto = salesService.getTransactionByIdAsDTO(createdTransaction.getId()).orElse(null);
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (IllegalArgumentException e) {
            // Handle validation errors
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null, LocalDateTime.now()));
        } catch (Exception e) {
            // Handle unexpected errors
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "An unexpected error occurred: " + e.getMessage(), null, LocalDateTime.now()));
        }
    }

    @PostMapping("/{id}/items")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<?> addItemToTransaction(
            @PathVariable Long id,
            @RequestParam String itemId,
            @RequestParam int quantity,
            @RequestParam(required = false) BigDecimal unitPrice) {
        try {
            SalesTransaction updatedTransaction = salesService.addItemToTransactionByIds(
                    id.toString(), itemId, quantity, unitPrice);

            if (updatedTransaction != null) {
                TransactionDTO dto = salesService.getTransactionByIdAsDTO(updatedTransaction.getId()).orElse(null);
                return ResponseEntity.ok(dto);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "An unexpected error occurred: " + e.getMessage(), null, LocalDateTime.now()));
        }
    }

    @DeleteMapping("/{id}/items/{itemId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<?> removeItemFromTransaction(
            @PathVariable Long id,
            @PathVariable Long itemId) {
        try {
            boolean removed = salesService.removeItemFromTransaction(id, itemId);

            if (removed) {
                TransactionDTO dto = salesService.getTransactionByIdAsDTO(id).orElse(null);
                return ResponseEntity.ok(dto);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "An unexpected error occurred: " + e.getMessage(), null, LocalDateTime.now()));
        }
    }

    @PutMapping("/{id}/items/{itemId}/quantity")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<?> updateItemQuantity(
            @PathVariable Long id,
            @PathVariable Long itemId,
            @RequestParam int quantity) {
        try {
            SalesTransaction updatedTransaction = salesService.updateItemQuantity(id, itemId, quantity);

            if (updatedTransaction != null) {
                TransactionDTO dto = salesService.getTransactionByIdAsDTO(updatedTransaction.getId()).orElse(null);
                return ResponseEntity.ok(dto);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "An unexpected error occurred: " + e.getMessage(), null, LocalDateTime.now()));
        }
    }

    @PutMapping("/{id}/items/{itemId}/discount")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<?> applyDiscount(
            @PathVariable Long id,
            @PathVariable Long itemId,
            @RequestParam BigDecimal amount) {
        try {
            SalesTransaction updatedTransaction = salesService.applyDiscount(id, itemId, amount);

            if (updatedTransaction != null) {
                TransactionDTO dto = salesService.getTransactionByIdAsDTO(updatedTransaction.getId()).orElse(null);
                return ResponseEntity.ok(dto);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "An unexpected error occurred: " + e.getMessage(), null, LocalDateTime.now()));
        }
    }

    @PostMapping("/{id}/payment")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<?> processPayment(
            @PathVariable Long id,
            @RequestParam PaymentMethod paymentMethod,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String referenceNumber) {
        try {
            SalesTransaction updatedTransaction = salesService.processPayment(id, paymentMethod, amount, referenceNumber);

            if (updatedTransaction != null) {
                TransactionDTO dto = salesService.getTransactionByIdAsDTO(updatedTransaction.getId()).orElse(null);
                return ResponseEntity.ok(dto);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null, LocalDateTime.now()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "An unexpected error occurred: " + e.getMessage(), null, LocalDateTime.now()));
        }
    }

    @PostMapping("/{id}/void")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> voidTransaction(@PathVariable Long id) {
        try {
            SalesTransaction updatedTransaction = salesService.voidTransaction(id);

            if (updatedTransaction != null) {
                TransactionDTO dto = salesService.getTransactionByIdAsDTO(updatedTransaction.getId()).orElse(null);
                return ResponseEntity.ok(dto);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null, LocalDateTime.now()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "An unexpected error occurred: " + e.getMessage(), null, LocalDateTime.now()));
        }
    }

    @PostMapping("/{id}/return")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> processReturn(@PathVariable Long id) {
        try {
            SalesTransaction updatedTransaction = salesService.processReturn(id);

            if (updatedTransaction != null) {
                TransactionDTO dto = salesService.getTransactionByIdAsDTO(updatedTransaction.getId()).orElse(null);
                return ResponseEntity.ok(dto);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null, LocalDateTime.now()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "An unexpected error occurred: " + e.getMessage(), null, LocalDateTime.now()));
        }
    }

    // By transactionId endpoints for convenience

    @PostMapping("/by-transaction-id/{transactionId}/items")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<?> addItemToTransactionByTransactionId(
            @PathVariable String transactionId,
            @RequestParam String itemId,
            @RequestParam int quantity,
            @RequestParam(required = false) BigDecimal unitPrice) {
        try {
            SalesTransaction updatedTransaction = salesService.addItemToTransactionByIds(
                    transactionId, itemId, quantity, unitPrice);

            if (updatedTransaction != null) {
                TransactionDTO dto = salesService.getTransactionByIdAsDTO(updatedTransaction.getId()).orElse(null);
                return ResponseEntity.ok(dto);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "An unexpected error occurred: " + e.getMessage(), null, LocalDateTime.now()));
        }
    }

    @DeleteMapping("/by-transaction-id/{transactionId}/items/{itemId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<?> removeItemFromTransactionByTransactionId(
            @PathVariable String transactionId,
            @PathVariable Long itemId) {
        try {
            boolean removed = salesService.removeItemFromTransactionByTransactionId(transactionId, itemId);

            if (removed) {
                TransactionDTO dto = salesService.getTransactionByTransactionIdAsDTO(transactionId).orElse(null);
                return ResponseEntity.ok(dto);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "An unexpected error occurred: " + e.getMessage(), null, LocalDateTime.now()));
        }
    }

    @PutMapping("/by-transaction-id/{transactionId}/items/{itemId}/quantity")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<?> updateItemQuantityByTransactionId(
            @PathVariable String transactionId,
            @PathVariable Long itemId,
            @RequestParam int quantity) {
        try {
            SalesTransaction updatedTransaction = salesService.updateItemQuantityByTransactionId(
                    transactionId, itemId, quantity);

            if (updatedTransaction != null) {
                TransactionDTO dto = salesService.getTransactionByIdAsDTO(updatedTransaction.getId()).orElse(null);
                return ResponseEntity.ok(dto);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "An unexpected error occurred: " + e.getMessage(), null, LocalDateTime.now()));
        }
    }

    @PutMapping("/by-transaction-id/{transactionId}/items/{itemId}/discount")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<?> applyDiscountByTransactionId(
            @PathVariable String transactionId,
            @PathVariable Long itemId,
            @RequestParam BigDecimal amount) {
        try {
            SalesTransaction updatedTransaction = salesService.applyDiscountByTransactionId(
                    transactionId, itemId, amount);

            if (updatedTransaction != null) {
                TransactionDTO dto = salesService.getTransactionByIdAsDTO(updatedTransaction.getId()).orElse(null);
                return ResponseEntity.ok(dto);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "An unexpected error occurred: " + e.getMessage(), null, LocalDateTime.now()));
        }
    }

    @PostMapping("/by-transaction-id/{transactionId}/payment")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<?> processPaymentByTransactionId(
            @PathVariable String transactionId,
            @RequestParam PaymentMethod paymentMethod,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String referenceNumber) {
        try {
            SalesTransaction updatedTransaction = salesService.processPaymentByTransactionId(
                    transactionId, paymentMethod, amount, referenceNumber);

            if (updatedTransaction != null) {
                TransactionDTO dto = salesService.getTransactionByIdAsDTO(updatedTransaction.getId()).orElse(null);
                return ResponseEntity.ok(dto);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null, LocalDateTime.now()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "An unexpected error occurred: " + e.getMessage(), null, LocalDateTime.now()));
        }
    }

    @PostMapping("/by-transaction-id/{transactionId}/void")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> voidTransactionByTransactionId(@PathVariable String transactionId) {
        try {
            SalesTransaction updatedTransaction = salesService.voidTransactionByTransactionId(transactionId);

            if (updatedTransaction != null) {
                TransactionDTO dto = salesService.getTransactionByIdAsDTO(updatedTransaction.getId()).orElse(null);
                return ResponseEntity.ok(dto);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null, LocalDateTime.now()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "An unexpected error occurred: " + e.getMessage(), null, LocalDateTime.now()));
        }
    }

    @PostMapping("/by-transaction-id/{transactionId}/return")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> processReturnByTransactionId(@PathVariable String transactionId) {
        try {
            SalesTransaction updatedTransaction = salesService.processReturnByTransactionId(transactionId);

            if (updatedTransaction != null) {
                TransactionDTO dto = salesService.getTransactionByIdAsDTO(updatedTransaction.getId()).orElse(null);
                return ResponseEntity.ok(dto);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null, LocalDateTime.now()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "An unexpected error occurred: " + e.getMessage(), null, LocalDateTime.now()));
        }
    }
}