package com.bin.pos.dal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDTO {
    private String username;
    private String fullName;
    private List<String> roles;
}
