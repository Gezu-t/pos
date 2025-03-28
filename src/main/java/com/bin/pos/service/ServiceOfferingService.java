package com.bin.pos.service;

import com.bin.pos.dal.model.ServiceOffering;
import com.bin.pos.dal.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ServiceOfferingService {

    private final ServiceRepository serviceRepository;

    @Autowired
    public ServiceOfferingService(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    public List<ServiceOffering> getAllServices() {
        return serviceRepository.findAll();
    }

    public List<ServiceOffering> getActiveServices() {
        return serviceRepository.findByActive(true);
    }

    public Optional<ServiceOffering> getServiceById(Long id) {
        return serviceRepository.findById(id);
    }

    public Optional<ServiceOffering> getServiceByServiceId(String serviceId) {
        return serviceRepository.findByServiceId(serviceId);
    }

    public List<ServiceOffering> searchServices(String searchTerm) {
        return serviceRepository.findByNameContainingIgnoreCase(searchTerm);
    }

    public List<ServiceOffering> getServicesByCategory(String category) {
        return serviceRepository.findByCategory(category);
    }

    @Transactional
    public ServiceOffering createService(ServiceOffering service) {
        // Generate a unique serviceId (business identifier) if not provided
        if (service.getServiceId() == null || service.getServiceId().isEmpty()) {
            service.setServiceId("SRV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }

        service.setActive(true);
        return serviceRepository.save(service);
    }

    @Transactional
    public ServiceOffering updateService(Long id, ServiceOffering serviceDetails) {
        return serviceRepository.findById(id).map(existingService -> {
            // Update fields from serviceDetails to existingService
            existingService.setName(serviceDetails.getName());
            existingService.setDescription(serviceDetails.getDescription());
            existingService.setPrice(serviceDetails.getPrice());
            existingService.setDurationMinutes(serviceDetails.getDurationMinutes());
            existingService.setCategory(serviceDetails.getCategory());
            // Don't update the serviceId or active status
            return serviceRepository.save(existingService);
        }).orElse(null);
    }

    @Transactional
    public ServiceOffering updateServiceByServiceId(String serviceId, ServiceOffering serviceDetails) {
        Optional<ServiceOffering> serviceOpt = serviceRepository.findByServiceId(serviceId);
        if (serviceOpt.isPresent()) {
            return updateService(serviceOpt.get().getId(), serviceDetails);
        }
        return null;
    }

    @Transactional
    public boolean toggleServiceStatus(Long id) {
        Optional<ServiceOffering> serviceOpt = serviceRepository.findById(id);

        if (serviceOpt.isPresent()) {
            ServiceOffering service = serviceOpt.get();
            service.setActive(!service.isActive());
            serviceRepository.save(service);
            return true;
        }

        return false;
    }

    @Transactional
    public boolean toggleServiceStatusByServiceId(String serviceId) {
        Optional<ServiceOffering> serviceOpt = serviceRepository.findByServiceId(serviceId);
        if (serviceOpt.isPresent()) {
            return toggleServiceStatus(serviceOpt.get().getId());
        }
        return false;
    }

    @Transactional
    public boolean deleteService(Long id) {
        if (serviceRepository.existsById(id)) {
            serviceRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional
    public boolean deleteServiceByServiceId(String serviceId) {
        Optional<ServiceOffering> serviceOpt = serviceRepository.findByServiceId(serviceId);
        if (serviceOpt.isPresent()) {
            serviceRepository.delete(serviceOpt.get());
            return true;
        }
        return false;
    }
}