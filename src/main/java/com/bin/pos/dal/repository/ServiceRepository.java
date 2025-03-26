package com.bin.pos.dal.repository;

import com.bin.pos.dal.model.ServiceOffering;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<ServiceOffering, String> {
    List<ServiceOffering> findByCategory(String category);
    List<ServiceOffering> findByNameContainingIgnoreCase(String namePart);
    List<ServiceOffering> findByActive(boolean active);
}

