package com.bin.pos.service;


import com.bin.pos.dal.model.InventoryItem;
import com.bin.pos.dal.model.Product;
import com.bin.pos.dal.model.ProductStatus;
import com.bin.pos.dal.repository.InventoryRepository;
import com.bin.pos.dal.repository.ProductRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class DataMigrationService {

//    private final InventoryRepository inventoryRepository;
//    private final ProductRepository productRepository;
//
//    @Autowired
//    public DataMigrationService(InventoryRepository inventoryRepository, ProductRepository productRepository) {
//        this.inventoryRepository = inventoryRepository;
//        this.productRepository = productRepository;
//    }
//
//    @PostConstruct
//    @Transactional
//    public void migrateInventoryData() {
//        // Fetch all inventory items
//        List<InventoryItem> inventoryItems = inventoryRepository.findAll();
//
//        // Map to group items by name, category, and price to avoid duplicate products
//        Map<String, Product> productMap = new HashMap<>();
//
//        for (InventoryItem item : inventoryItems) {
//            // Skip if already linked to a product
//            if (item.getProduct() != null) {
//                continue;
//            }
//
//
//            // Check if we’ve already created a product for this key
//            Product product = productMap.get(key);
//            if (product == null) {
//                // Create a new Product
//                product = new Product();
//                product.setProductId("PROD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
//                product.setName(item.getName());
//                product.setUnitPrice(item.getPrice()); // Use unitCost as unitPrice initially
//                product.setCategory(item.getCategory());
//                product.setStatus(ProductStatus.ACTIVE);
//                product = productRepository.save(product);
//                productMap.put(key, product);
//            }
//
//            // Link the inventory item to the product
//            item.setProduct(product);
//            inventoryRepository.save(item);
//        }
//
//        // Optional: Log or print completion
//        System.out.println("Inventory data migration completed. Created " + productMap.size() + " products.");
//    }
}