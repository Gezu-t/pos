package com.bin.pos.service;

import com.bin.pos.dal.model.Customer;
import com.bin.pos.dal.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    @Autowired
    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public Optional<Customer> getCustomerById(Long id) {
        return customerRepository.findById(id);
    }

    public Optional<Customer> getCustomerByCustomerId(String customerId) {
        return customerRepository.findByCustomerId(customerId);
    }

    public Optional<Customer> getCustomerByEmail(String email) {
        return customerRepository.findByEmail(email);
    }

    public List<Customer> searchCustomers(String searchTerm) {
        return customerRepository.findByNameContainingIgnoreCase(searchTerm);
    }

    @Transactional
    public Customer createCustomer(Customer customer) {
        // Generate a unique customerId (business identifier) if not provided
        if (customer.getCustomerId() == null || customer.getCustomerId().isEmpty()) {
            customer.setCustomerId("CUST-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        return customerRepository.save(customer);
    }

    @Transactional
    public Customer updateCustomer(Long id, Customer customerDetails) {
        return customerRepository.findById(id).map(existingCustomer -> {
            // Update fields from customerDetails to existingCustomer
            existingCustomer.setName(customerDetails.getName());
            existingCustomer.setEmail(customerDetails.getEmail());
            existingCustomer.setPhone(customerDetails.getPhone());
            existingCustomer.setAddress(customerDetails.getAddress());
            existingCustomer.setType(customerDetails.getType());
            // Don't update the customerId or id
            return customerRepository.save(existingCustomer);
        }).orElse(null);
    }

    @Transactional
    public Customer updateCustomerByCustomerId(String customerId, Customer customerDetails) {
        return customerRepository.findByCustomerId(customerId).map(existingCustomer -> {
            return updateCustomer(existingCustomer.getId(), customerDetails);
        }).orElse(null);
    }

    @Transactional
    public boolean deleteCustomer(Long id) {
        if (customerRepository.existsById(id)) {
            customerRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional
    public boolean deleteCustomerByCustomerId(String customerId) {
        Optional<Customer> customerOpt = customerRepository.findByCustomerId(customerId);
        if (customerOpt.isPresent()) {
            customerRepository.delete(customerOpt.get());
            return true;
        }
        return false;
    }
}