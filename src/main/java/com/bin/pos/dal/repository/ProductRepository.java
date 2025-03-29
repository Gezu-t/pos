package com.bin.pos.dal.repository;

import com.bin.pos.dal.model.Product;
import com.bin.pos.dal.model.ProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByProductId(String productId);

    List<Product> findByNameContainingIgnoreCase(String name);

    List<Product> findByCategory(String category);

    List<Product> findByStatus(ProductStatus status);

    @Query("SELECT p FROM Product p WHERE p.stockQuantity > 0 AND p.status = 'ACTIVE'")
    List<Product> findAvailableProducts();
}