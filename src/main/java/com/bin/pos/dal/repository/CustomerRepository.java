package com.bin.pos.dal.repository;

import com.bin.pos.dal.model.Customer;
import com.bin.pos.dal.model.CustomerType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, String> {
    List<Customer> findByType(CustomerType type);
    Optional<Customer> findByEmail(String email);
    List<Customer> findByNameContainingIgnoreCase(String namePart);
}
