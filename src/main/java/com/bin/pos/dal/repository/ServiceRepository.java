package com.bin.pos.dal.repository;

import com.bin.pos.dal.model.ServiceOffering;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceRepository extends JpaRepository<ServiceOffering, Long> {

    // Find by business identifier
    Optional<ServiceOffering> findByServiceId(String serviceId);

    List<ServiceOffering> findByActive(boolean active);

    List<ServiceOffering> findByNameContainingIgnoreCase(String searchTerm);

    List<ServiceOffering> findByCategory(String category);

    boolean existsByServiceId(String serviceId);
}