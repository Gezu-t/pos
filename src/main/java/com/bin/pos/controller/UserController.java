package com.bin.pos.controller;

import com.bin.pos.dal.dto.ApiResponse;
import com.bin.pos.dal.dto.PasswordChangeRequest;
import com.bin.pos.dal.dto.PasswordResetRequest;
import com.bin.pos.dal.dto.UserDTO;
import com.bin.pos.dal.model.User;
import com.bin.pos.exception.ResourceNotFoundException;
import com.bin.pos.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getAllUsers() {
        try {
            List<UserDTO> users = userService.getAllUsers().stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(new ApiResponse<>(true, "Users retrieved successfully", users));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error retrieving users: " + e.getMessage(), null));
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurityService.isCurrentUser(#id)")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable Long id) {
        try {
            User user = userService.getUserById(id);
            return ResponseEntity.ok(new ApiResponse<>(true, "User retrieved successfully", convertToDto(user)));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error retrieving user: " + e.getMessage(), null));
        }
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserDTO>> getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Long currentUserId = getCurrentUserId(authentication);
            User user = userService.getUserById(currentUserId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Current user retrieved successfully", convertToDto(user)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error retrieving current user: " + e.getMessage(), null));
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDTO>> createUser(@Valid @RequestBody User user) {
        try {
            User createdUser = userService.createUser(user);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, "User created successfully", convertToDto(createdUser)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "Error creating user: " + e.getMessage(), null));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurityService.isCurrentUser(#id)")
    public ResponseEntity<ApiResponse<UserDTO>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserDTO user
    ) {
        try {
            User updatedUser = userService.updateUser(id, user);
            return ResponseEntity.ok(new ApiResponse<>(true, "User updated successfully", convertToDto(updatedUser)));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "Error updating user: " + e.getMessage(), null));
        }
    }

    @PutMapping("/{id}/password/reset")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @PathVariable Long id,
            @Valid @RequestBody PasswordResetRequest request
    ) {
        try {
            userService.resetPassword(id, request.getNewPassword());
            return ResponseEntity.ok(new ApiResponse<>(true, "Password reset successfully", null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "Error resetting password: " + e.getMessage(), null));
        }
    }

    @PutMapping("/password/change")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody PasswordChangeRequest request
    ) {
        try {
            // Get current user's ID from authentication context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Long currentUserId = getCurrentUserId(authentication);

            // Ensure the user is changing their own password
            if (!currentUserId.equals(request.getUserId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse<>(false, "Cannot change password for another user", null));
            }

            boolean success = userService.changePassword(
                    currentUserId,
                    request.getOldPassword(),
                    request.getNewPassword()
            );

            if (success) {
                return ResponseEntity.ok(new ApiResponse<>(true, "Password changed successfully", null));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(false, "Invalid old password", null));
            }
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error changing password: " + e.getMessage(), null));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(new ApiResponse<>(true, "User deleted successfully", null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error deleting user: " + e.getMessage(), null));
        }
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDTO>> updateUserStatus(
            @PathVariable Long id,
            @RequestParam boolean active
    ) {
        try {
            User updatedUser = userService.updateUserStatus(id, active);
            return ResponseEntity.ok(new ApiResponse<>(true,
                    "User status updated to " + (active ? "active" : "inactive"),
                    convertToDto(updatedUser)));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error updating user status: " + e.getMessage(), null));
        }
    }

    private UserDTO convertToDto(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setRoles(user.getRoles());
        dto.setActive(user.isActive());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setLastLogin(user.getLastLogin());
        return dto;
    }

    // Helper method to get current user's ID
    private Long getCurrentUserId(Authentication authentication) {
        if (authentication.getPrincipal() instanceof User) {
            return ((User) authentication.getPrincipal()).getId();
        }
        throw new RuntimeException("Unable to determine current user");
    }
}