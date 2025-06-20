package com.devvault.Controller;

import com.devvault.exception.ResourceNotFoundException;
import com.devvault.model.User;
import com.devvault.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.devvault.dto.UserDTO;
import jakarta.validation.Valid;
import com.devvault.dto.UserUpdateDTO;
import com.devvault.dto.UserResponseDTO;
import com.devvault.util.DtoConverter;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // 🔐 Create a new user - ADMIN only
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<User> createUser(@Valid @RequestBody UserDTO userDTO) {
        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setRole(userDTO.getRole());

        User savedUser = userRepository.save(user);
        return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
    }

    // 🔐 Get all users - ADMIN only
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<UserResponseDTO> response = users.stream()
                .map(DtoConverter::toUserResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    // 🔐 Get user by ID - ADMIN only
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> getUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        return ResponseEntity.ok(DtoConverter.toUserResponse(user));
    }

    // 🔐 Update user by ID (Upsert style) - ADMIN only
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateDTO dto
    ) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setUsername(dto.getUsername());
                    user.setEmail(dto.getEmail());
                    user.setRole(dto.getRole());
                    if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
                        user.setPassword(passwordEncoder.encode(dto.getPassword()));
                    }
                    return ResponseEntity.ok(DtoConverter.toUserResponse(userRepository.save(user)));
                })
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setId(id);
                    newUser.setUsername(dto.getUsername());
                    newUser.setEmail(dto.getEmail());
                    newUser.setRole(dto.getRole());
                    newUser.setPassword(passwordEncoder.encode(dto.getPassword()));
                    return new ResponseEntity<>(DtoConverter.toUserResponse(userRepository.save(newUser)), HttpStatus.CREATED);
                });
    }

    // 🔐 Delete user by ID - ADMIN only
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        log.info("Deleting user with ID: {}", id);
        if (!userRepository.existsById(id)) {
            log.warn("Attempt to delete non-existent user with ID: {}", id);
            throw new ResourceNotFoundException("User not found with ID: " + id);
        }
        userRepository.deleteById(id);
        log.info("User deleted with ID: {}", id);
        return ResponseEntity.ok("User deleted successfully.");
    }

    // 🔓 Get leaderboard - top 10 users by rewardPoints
    @GetMapping("/leaderboard")
    public ResponseEntity<List<UserResponseDTO>> getLeaderboard() {
        List<User> topUsers = userRepository.findTop10ByOrderByRewardPointsDesc();
        List<UserResponseDTO> response = topUsers.stream()
                .map(DtoConverter::toUserResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    // 🔐 Get current logged-in user's profile
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponseDTO> getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return ResponseEntity.ok(DtoConverter.toUserResponse(user));
    }
}
