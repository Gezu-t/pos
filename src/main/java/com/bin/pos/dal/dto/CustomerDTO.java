package com.bin.pos.dal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for customers in transaction views
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDTO {
    private Long id;
    private String customerId;
    private String name;
    private String email;
    private String phone;
    private String address;
    private String type;
}