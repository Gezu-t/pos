package com.bin.pos.dal.dto;

import com.bin.pos.dal.dto.CustomerDTO;
import com.bin.pos.dal.dto.ItemDTO;
import com.bin.pos.dal.dto.TransactionDTO;
import com.bin.pos.dal.dto.TransactionItemDTO;
import com.bin.pos.dal.model.Customer;
import com.bin.pos.dal.model.InventoryItem;
import com.bin.pos.dal.model.SalesTransaction;
import com.bin.pos.dal.model.TransactionItem;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class to convert between entity and DTO objects
 */
@Component
public class DTOConverter {

    /**
     * Convert a SalesTransaction entity to a TransactionDTO
     */
    public TransactionDTO convertToDTO(SalesTransaction transaction) {
        if (transaction == null) {
            return null;
        }

        TransactionDTO dto = new TransactionDTO();
        dto.setId(transaction.getId());
        dto.setTransactionId(transaction.getTransactionId());

        // Convert customer if present
        if (transaction.getCustomer() != null) {
            dto.setCustomer(convertToDTO(transaction.getCustomer()));
        }

        dto.setCreationTime(transaction.getCreationTime());
        dto.setCompletionTime(transaction.getCompletionTime());
        dto.setStatus(transaction.getStatus());
        dto.setPaymentMethod(transaction.getPaymentMethod());
        dto.setAmountPaid(transaction.getAmountPaid());

        // Convert items if present
        if (transaction.getItems() != null) {
            dto.setItems(transaction.getItems().stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList()));
        }

        dto.setTaxRate(transaction.getTaxRate());
        dto.setSubtotal(transaction.getSubtotal());
        dto.setTaxAmount(transaction.getTaxAmount());
        dto.setTotalAmount(transaction.getTotalAmount());
        dto.setNotes(transaction.getNotes());

        return dto;
    }

    /**
     * Convert a list of SalesTransaction entities to DTOs
     */
    public List<TransactionDTO> convertToDTO(List<SalesTransaction> transactions) {
        if (transactions == null) {
            return null;
        }

        return transactions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Convert a Customer entity to a CustomerDTO
     */
    public CustomerDTO convertToDTO(Customer customer) {
        if (customer == null) {
            return null;
        }

        CustomerDTO dto = new CustomerDTO();
        dto.setId(customer.getId());
        dto.setCustomerId(customer.getCustomerId());
        dto.setName(customer.getName());
        dto.setEmail(customer.getEmail());
        dto.setPhone(customer.getPhone());
        dto.setAddress(customer.getAddress());
        dto.setType(customer.getType().toString());

        return dto;
    }

    /**
     * Convert a TransactionItem entity to a TransactionItemDTO
     */
    public TransactionItemDTO convertToDTO(TransactionItem item) {
        if (item == null) {
            return null;
        }

        TransactionItemDTO dto = new TransactionItemDTO();
        dto.setId(item.getId());

        // Convert item if present
        if (item.getItem() != null) {
            dto.setItem(convertToDTO(item.getItem()));
        }

        dto.setQuantity(item.getQuantity());
        dto.setUnitPrice(item.getUnitPrice());
        dto.setDiscountAmount(item.getDiscountAmount());
        dto.setSubtotal(item.getUnitPrice().multiply(
                new java.math.BigDecimal(item.getQuantity())).subtract(item.getDiscountAmount()));

        return dto;
    }

    /**
     * Convert an InventoryItem entity to an ItemDTO
     */
    public ItemDTO convertToDTO(InventoryItem item) {
        if (item == null) {
            return null;
        }

        ItemDTO dto = new ItemDTO();
        dto.setId(item.getId());
        dto.setItemId(item.getItemId());
        // Get name and category from the Product
        dto.setName(item.getProduct() != null ? item.getProduct().getName() : "Unknown");
        dto.setCategory(item.getProduct() != null ? item.getProduct().getCategory() : "Uncategorized");
        dto.setPrice(item.getPrice());
        dto.setQuantity(item.getQuantity());
        dto.setUnit(item.getUnit());

        return dto;
    }
}