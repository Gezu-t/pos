package com.bin.pos.controller;

import com.bin.pos.dal.model.ServiceOffering;
import com.bin.pos.service.ServiceOfferingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/service-offerings")
public class ServiceOfferingController {

    private final ServiceOfferingService serviceOfferingService;

    @Autowired
    public ServiceOfferingController(ServiceOfferingService serviceOfferingService) {
        this.serviceOfferingService = serviceOfferingService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<List<ServiceOffering>> getAllServices() {
        return ResponseEntity.ok(serviceOfferingService.getAllServices());
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<List<ServiceOffering>> getActiveServices() {
        return ResponseEntity.ok(serviceOfferingService.getActiveServices());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<ServiceOffering> getServiceById(@PathVariable Long id) {
        return serviceOfferingService.getServiceById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-service-id/{serviceId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<ServiceOffering> getServiceByServiceId(@PathVariable String serviceId) {
        if (serviceId == null || serviceId.equals("undefined") || serviceId.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        return serviceOfferingService.getServiceByServiceId(serviceId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<List<ServiceOffering>> searchServices(@RequestParam String query) {
        return ResponseEntity.ok(serviceOfferingService.searchServices(query));
    }

    @GetMapping("/category/{category}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<List<ServiceOffering>> getServicesByCategory(@PathVariable String category) {
        return ResponseEntity.ok(serviceOfferingService.getServicesByCategory(category));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ServiceOffering> createService(@RequestBody ServiceOffering service) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(serviceOfferingService.createService(service));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ServiceOffering> updateService(
            @PathVariable Long id,
            @RequestBody ServiceOffering service) {
        ServiceOffering updated = serviceOfferingService.updateService(id, service);
        return updated != null ?
                ResponseEntity.ok(updated) :
                ResponseEntity.notFound().build();
    }

    @PutMapping("/by-service-id/{serviceId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ServiceOffering> updateServiceByServiceId(
            @PathVariable String serviceId,
            @RequestBody ServiceOffering service) {
        if (serviceId == null || serviceId.equals("undefined") || serviceId.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        ServiceOffering updated = serviceOfferingService.updateServiceByServiceId(serviceId, service);
        return updated != null ?
                ResponseEntity.ok(updated) :
                ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}/toggle")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> toggleServiceStatus(@PathVariable Long id) {
        boolean toggled = serviceOfferingService.toggleServiceStatus(id);
        return toggled ?
                ResponseEntity.ok().build() :
                ResponseEntity.notFound().build();
    }

    @PutMapping("/by-service-id/{serviceId}/toggle")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> toggleServiceStatusByServiceId(@PathVariable String serviceId) {
        if (serviceId == null || serviceId.equals("undefined") || serviceId.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        boolean toggled = serviceOfferingService.toggleServiceStatusByServiceId(serviceId);
        return toggled ?
                ResponseEntity.ok().build() :
                ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteService(@PathVariable Long id) {
        boolean deleted = serviceOfferingService.deleteService(id);
        return deleted ?
                ResponseEntity.noContent().build() :
                ResponseEntity.notFound().build();
    }

    @DeleteMapping("/by-service-id/{serviceId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteServiceByServiceId(@PathVariable String serviceId) {
        if (serviceId == null || serviceId.equals("undefined") || serviceId.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        boolean deleted = serviceOfferingService.deleteServiceByServiceId(serviceId);
        return deleted ?
                ResponseEntity.noContent().build() :
                ResponseEntity.notFound().build();
    }
}