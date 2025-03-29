package com.bin.pos.dal.dto;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductDTO {
    private Long id;
    private String productId;
    private String name;
    private String description;
    private BigDecimal unitPrice;
    private String category;
    private String barcode;
    private String status;

    public ProductDTO() {}

    public ProductDTO(com.bin.pos.dal.model.Product product) {
        this.id = product.getId();
        this.productId = product.getProductId();
        this.name = product.getName();
        this.description = product.getDescription();
        this.unitPrice = product.getUnitPrice();
        this.category = product.getCategory();
        this.barcode = product.getBarcode();
        this.status = product.getStatus() != null ? product.getStatus().toString() : "ACTIVE";
    }
}