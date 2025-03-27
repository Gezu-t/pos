package com.bin.pos.dal.dto;


import lombok.Data;

@Data
public class PasswordChangeRequest {
    private Long userId;
    private String oldPassword;
    private String newPassword;
}
