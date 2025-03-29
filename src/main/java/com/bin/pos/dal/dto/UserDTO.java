package com.bin.pos.dal.dto;


import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.Set;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDTO {
    private Long id;
    private String username;
    private String fullName;
    private String email;
    private Set<String> roles;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
}