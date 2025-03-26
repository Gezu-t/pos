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

    public Optional<ServiceOffering> getServiceById(String serviceId) {
        return serviceRepository.findById(serviceId);
    }

    public List<ServiceOffering> searchServices(String searchTerm) {
        return serviceRepository.findByNameContainingIgnoreCase(searchTerm);
    }

    public List<ServiceOffering> getServicesByCategory(String category) {
        return serviceRepository.findByCategory(category);
    }

    @Transactional
    public ServiceOffering createService(ServiceOffering service) {
        if (service.getServiceId() == null || service.getServiceId().isEmpty()) {
            service.setServiceId(UUID.randomUUID().toString());
        }
        service.setActive(true);
        return serviceRepository.save(service);
    }

    @Transactional
    public ServiceOffering updateService(ServiceOffering service) {
        return serviceRepository.save(service);
    }

    @Transactional
    public boolean toggleServiceStatus(String serviceId) {
        Optional<ServiceOffering> serviceOpt = serviceRepository.findById(serviceId);

        if (serviceOpt.isPresent()) {
            ServiceOffering service = serviceOpt.get();
            service.setActive(!service.isActive());
            serviceRepository.save(service);
            return true;
        }

        return false;
    }

    @Transactional
    public void deleteService(String serviceId) {
        serviceRepository.deleteById(serviceId);
    }
}
