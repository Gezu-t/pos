package com.bin.pos.dal.repository;

import com.bin.pos.dal.model.PaymentMethod;
import com.bin.pos.dal.model.PaymentStatus;
import com.bin.pos.dal.model.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentTransaction, String> {
    List<PaymentTransaction> findBySalesTransactionTransactionId(String transactionId);
    List<PaymentTransaction> findByPaymentMethod(PaymentMethod paymentMethod);
    List<PaymentTransaction> findByStatus(PaymentStatus status);
}