package com.bin.pos.dal.repository;

import com.bin.pos.dal.model.Customer;
import com.bin.pos.dal.model.CustomerType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    // Find by business identifier
    Optional<Customer> findByCustomerId(String customerId);

    Optional<Customer> findByEmail(String email);

    List<Customer> findByNameContainingIgnoreCase(String searchTerm);

    List<Customer> findByType(CustomerType type);

    boolean existsByEmail(String email);

    boolean existsByCustomerId(String customerId);
}