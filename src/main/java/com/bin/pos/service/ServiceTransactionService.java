package com.bin.pos.service;


import com.bin.pos.dal.model.InventoryItem;
import com.bin.pos.dal.model.SalesTransaction;
import com.bin.pos.dal.model.ServiceOffering;
import com.bin.pos.dal.model.ServiceTransactionItem;
import com.bin.pos.dal.repository.PaymentRepository;
import com.bin.pos.dal.repository.SalesRepository;
import com.bin.pos.dal.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
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
    public SalesTransaction addServiceToTransaction(String transactionId, String serviceId, int quantity, BigDecimal unitPrice) {
        Optional<SalesTransaction> transactionOpt = salesRepository.findById(transactionId);
        Optional<ServiceOffering> serviceOpt = serviceRepository.findById(serviceId);

        if (transactionOpt.isPresent() && serviceOpt.isPresent()) {
            SalesTransaction transaction = transactionOpt.get();
            ServiceOffering service = serviceOpt.get();

            // Create service transaction item
            ServiceTransactionItem serviceItem = new ServiceTransactionItem();
            serviceItem.setSalesTransaction(transaction);
            serviceItem.setService(service);
            serviceItem.setQuantity(quantity);
            serviceItem.setUnitPrice(unitPrice != null ? unitPrice : service.getPrice());
            serviceItem.setDiscountAmount(BigDecimal.ZERO);

            // Add to transaction and save
            return salesRepository.save(transaction);
        }

        return null;
    }

    @Transactional
    public boolean removeServiceFromTransaction(String transactionId, Long serviceItemId) {
        Optional<SalesTransaction> transactionOpt = salesRepository.findById(transactionId);

        if (transactionOpt.isPresent()) {
            SalesTransaction transaction = transactionOpt.get();
            // Remove service item logic would go here
            // This depends on how you're storing service items within the transaction

            salesRepository.save(transaction);
            return true;
        }

        return false;
    }

    @Transactional
    public SalesTransaction updateServiceNotes(String transactionId, Long serviceItemId, String notes) {
        Optional<SalesTransaction> transactionOpt = salesRepository.findById(transactionId);

        if (transactionOpt.isPresent()) {
            SalesTransaction transaction = transactionOpt.get();
            // Update service notes logic would go here

            return salesRepository.save(transaction);
        }

        return null;
    }

}
