package com.bin.pos.dal.repository;


import com.bin.pos.dal.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, String> {
    List<Supplier> findByActive(boolean active);
    List<Supplier> findByNameContainingIgnoreCase(String namePart);
}