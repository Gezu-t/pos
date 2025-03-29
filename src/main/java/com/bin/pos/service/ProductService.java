package com.bin.pos.service;

import com.bin.pos.dal.dto.ProductDTO;
import com.bin.pos.dal.model.Product;
import com.bin.pos.dal.model.ProductStatus;
import com.bin.pos.dal.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll().stream()
                .map(ProductDTO::new)
                .collect(Collectors.toList());
    }

    public Optional<ProductDTO> getProductById(Long id) {
        return productRepository.findById(id).map(ProductDTO::new);
    }

    public Optional<ProductDTO> getProductByProductId(String productId) {
        return productRepository.findByProductId(productId).map(ProductDTO::new);
    }

    @Transactional
    public ProductDTO createProduct(ProductDTO productDTO) {
        Product product = new Product();
        // Auto-generate productId if not provided or empty
        String productId = productDTO.getProductId();
        if (productId == null || productId.trim().isEmpty()) {
            productId = generateUniqueProductId();
        }
        product.setProductId(productId);
        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setUnitPrice(productDTO.getUnitPrice());
        product.setCategory(productDTO.getCategory());
        product.setBarcode(productDTO.getBarcode());
        product.setStatus(productDTO.getStatus() != null ?
                ProductStatus.valueOf(productDTO.getStatus()) : ProductStatus.ACTIVE);
        return new ProductDTO(productRepository.save(product));
    }

    @Transactional
    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        return productRepository.findById(id).map(existingProduct -> {
            existingProduct.setName(productDTO.getName());
            existingProduct.setDescription(productDTO.getDescription());
            existingProduct.setUnitPrice(productDTO.getUnitPrice());
            existingProduct.setCategory(productDTO.getCategory());
            existingProduct.setBarcode(productDTO.getBarcode());
            existingProduct.setStatus(productDTO.getStatus() != null ?
                    ProductStatus.valueOf(productDTO.getStatus()) : existingProduct.getStatus());
            // productId remains unchanged during update
            return new ProductDTO(productRepository.save(existingProduct));
        }).orElse(null);
    }

    @Transactional
    public ProductDTO updateProductByProductId(String productId, ProductDTO productDTO) {
        return productRepository.findByProductId(productId).map(existingProduct -> {
            return updateProduct(existingProduct.getId(), productDTO);
        }).orElse(null);
    }

    @Transactional
    public boolean deleteProduct(Long id) {
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional
    public boolean deleteProductByProductId(String productId) {
        Optional<Product> productOpt = productRepository.findByProductId(productId);
        if (productOpt.isPresent()) {
            productRepository.delete(productOpt.get());
            return true;
        }
        return false;
    }

    private String generateUniqueProductId() {
        String productId;
        do {
            productId = "PROD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (productRepository.findByProductId(productId).isPresent()); // Ensure uniqueness
        return productId;
    }


}