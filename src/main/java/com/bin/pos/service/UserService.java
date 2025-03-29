package com.bin.pos.service;

import com.bin.pos.dal.dto.UserDTO;
import com.bin.pos.dal.model.User;
import com.bin.pos.dal.repository.UserRepository;
import com.bin.pos.exception.UserAlreadyExistsException;
import com.bin.pos.exception.UserNotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Password validation regex
//    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
//            "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$"
//    );

    @Autowired
    public UserService(UserRepository userRepository, @Lazy PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }

    @Transactional
    public User createUser(User user) {
        // Validate input
        validateUserCreation(user);

        // Encode password
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Set default roles if not provided
        setDefaultRolesIfEmpty(user);

        // Set account creation time
        user.setCreatedAt(LocalDateTime.now());
        user.setActive(true);

        return userRepository.save(user);
    }

    private void validateUserCreation(User user) {
        // Check for null or empty username
        if (StringUtils.isBlank(user.getUsername())) {
            throw new IllegalArgumentException("Username cannot be empty");
        }

        // Check username existence
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new UserAlreadyExistsException("Username already exists: " + user.getUsername());
        }

        // Check email existence if provided
        if (StringUtils.isNotBlank(user.getEmail()) &&
                userRepository.existsByEmail(user.getEmail())) {
            throw new UserAlreadyExistsException("Email already in use: " + user.getEmail());
        }

        // Validate password
        validatePassword(user.getPassword());
    }

    private void validatePassword(String password) {
        if (StringUtils.isBlank(password)) {
            throw new IllegalArgumentException("Password cannot be empty");
        }

//        if (!PASSWORD_PATTERN.matcher(password).matches()) {
//            throw new IllegalArgumentException("Password does not meet complexity requirements. " +
//                    "It must be at least 8 characters long and contain at least one uppercase letter, " +
//                    "one lowercase letter, one number, and one special character.");
//        }
    }

    private void setDefaultRolesIfEmpty(User user) {
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            Set<String> defaultRoles = new HashSet<>();
            defaultRoles.add("EMPLOYEE");
            user.setRoles(defaultRoles);
        }
    }

    @Transactional
    public User updateUser(Long id, UserDTO userDetails) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        if (StringUtils.isNotBlank(userDetails.getFullName())) {
            user.setFullName(userDetails.getFullName());
        }

        if (StringUtils.isNotBlank(userDetails.getEmail())) {
            if (!user.getEmail().equals(userDetails.getEmail()) &&
                    userRepository.existsByEmail(userDetails.getEmail())) {
                throw new UserAlreadyExistsException("Email already in use: " + userDetails.getEmail());
            }
            user.setEmail(userDetails.getEmail());
        }

        if (userDetails.isActive() != user.isActive()) {
            user.setActive(userDetails.isActive());
        }

        if (userDetails.getRoles() != null && !userDetails.getRoles().isEmpty()) {
            user.setRoles(userDetails.getRoles());
        }

        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    @Transactional
    public void updateLastLogin(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        // Validate old password
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        // Validate new password
        validatePassword(newPassword);

        // Encode and update new password
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordChangedAt(LocalDateTime.now());
        userRepository.save(user);
        return true;
    }

    @Transactional
    public void resetPassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        // Validate new password
        validatePassword(newPassword);

        // Encode and update password
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordChangedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    // Additional method to check if user exists
    public boolean userExists(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * Add these two methods to your existing UserService class
     */

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        userRepository.delete(user);
    }

    @Transactional
    public User updateUserStatus(Long id, boolean active) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        user.setActive(active);
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }
}