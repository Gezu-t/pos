package com.bin.pos.service;

import com.bin.pos.dal.model.SalesTransaction;
import com.bin.pos.dal.model.ServiceOffering;
import com.bin.pos.dal.model.ServiceTransactionItem;
import com.bin.pos.dal.repository.PaymentRepository;
import com.bin.pos.dal.repository.SalesRepository;
import com.bin.pos.dal.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class ServiceTransactionService {

    private final SalesRepository salesRepository;
    private final ServiceRepository serviceRepository;
    private final PaymentRepository paymentRepository;

    @Autowired
    public ServiceTransactionService(
            SalesRepository salesRepository,
            ServiceRepository serviceRepository,
            PaymentRepository paymentRepository) {
        this.salesRepository = salesRepository;
        this.serviceRepository = serviceRepository;
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public SalesTransaction addServiceToTransaction(Long transactionId, Long serviceId, int quantity, BigDecimal unitPrice) {
        Optional<SalesTransaction> transactionOpt = salesRepository.findById(transactionId);
        Optional<ServiceOffering> serviceOpt = serviceRepository.findById(serviceId);

        if (transactionOpt.isPresent() && serviceOpt.isPresent()) {
            SalesTransaction transaction = transactionOpt.get();
            ServiceOffering service = serviceOpt.get();

            // Check if service is already in the transaction
            Optional<ServiceTransactionItem> existingItem = transaction.getServiceItems().stream()
                    .filter(i -> i.getService().getId().equals(serviceId))
                    .findFirst();

            if (existingItem.isPresent()) {
                // Increase quantity of existing item
                ServiceTransactionItem serviceItem = existingItem.get();
                serviceItem.setQuantity(serviceItem.getQuantity() + quantity);
            } else {
                // Create service transaction item
                ServiceTransactionItem serviceItem = new ServiceTransactionItem();
                serviceItem.setSalesTransaction(transaction);
                serviceItem.setService(service);
                serviceItem.setQuantity(quantity);
                serviceItem.setUnitPrice(unitPrice != null ? unitPrice : service.getPrice());
                serviceItem.setDiscountAmount(BigDecimal.ZERO);

                // Add to transaction's service items
                transaction.getServiceItems().add(serviceItem);
            }

            // Save and return updated transaction
            return salesRepository.save(transaction);
        }

        return null;
    }

    @Transactional
    public SalesTransaction addServiceToTransactionByIds(String transactionId, String serviceId, int quantity, BigDecimal unitPrice) {
        Optional<SalesTransaction> transactionOpt = salesRepository.findByTransactionId(transactionId);
        Optional<ServiceOffering> serviceOpt = serviceRepository.findByServiceId(serviceId);

        if (transactionOpt.isPresent() && serviceOpt.isPresent()) {
            return addServiceToTransaction(
                    transactionOpt.get().getId(),
                    serviceOpt.get().getId(),
                    quantity,
                    unitPrice
            );
        }

        return null;
    }

    @Transactional
    public boolean removeServiceFromTransaction(Long transactionId, Long serviceItemId) {
        Optional<SalesTransaction> transactionOpt = salesRepository.findById(transactionId);

        if (transactionOpt.isPresent()) {
            SalesTransaction transaction = transactionOpt.get();

            // Remove service item from transaction
            boolean removed = transaction.getServiceItems().removeIf(item -> item.getId().equals(serviceItemId));

            if (removed) {
                salesRepository.save(transaction);
                return true;
            }
        }

        return false;
    }

    @Transactional
    public boolean removeServiceFromTransactionByTransactionId(String transactionId, Long serviceItemId) {
        Optional<SalesTransaction> transactionOpt = salesRepository.findByTransactionId(transactionId);

        if (transactionOpt.isPresent()) {
            return removeServiceFromTransaction(transactionOpt.get().getId(), serviceItemId);
        }

        return false;
    }

    @Transactional
    public SalesTransaction updateServiceNotes(Long transactionId, Long serviceItemId, String notes) {
        Optional<SalesTransaction> transactionOpt = salesRepository.findById(transactionId);

        if (transactionOpt.isPresent()) {
            SalesTransaction transaction = transactionOpt.get();

            // Find and update the service item
            transaction.getServiceItems().stream()
                    .filter(item -> item.getId().equals(serviceItemId))
                    .findFirst()
                    .ifPresent(item -> item.setNotes(notes));

            return salesRepository.save(transaction);
        }

        return null;
    }

    @Transactional
    public SalesTransaction updateServiceNotesByTransactionId(String transactionId, Long serviceItemId, String notes) {
        Optional<SalesTransaction> transactionOpt = salesRepository.findByTransactionId(transactionId);

        if (transactionOpt.isPresent()) {
            return updateServiceNotes(transactionOpt.get().getId(), serviceItemId, notes);
        }

        return null;
    }

    @Transactional
    public SalesTransaction updateServiceQuantity(Long transactionId, Long serviceItemId, int quantity) {
        if (quantity < 1) {
            return null;
        }

        Optional<SalesTransaction> transactionOpt = salesRepository.findById(transactionId);

        if (transactionOpt.isPresent()) {
            SalesTransaction transaction = transactionOpt.get();

            // Find and update the service item
            transaction.getServiceItems().stream()
                    .filter(item -> item.getId().equals(serviceItemId))
                    .findFirst()
                    .ifPresent(item -> item.setQuantity(quantity));

            return salesRepository.save(transaction);
        }

        return null;
    }

    @Transactional
    public SalesTransaction updateServiceQuantityByTransactionId(String transactionId, Long serviceItemId, int quantity) {
        Optional<SalesTransaction> transactionOpt = salesRepository.findByTransactionId(transactionId);

        if (transactionOpt.isPresent()) {
            return updateServiceQuantity(transactionOpt.get().getId(), serviceItemId, quantity);
        }

        return null;
    }
}