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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    // 🔐 Create a new user - ADMIN only
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        log.info("Creating new user: {}", user.getEmail());
        User savedUser = userRepository.save(user);
        log.info("User created with ID: {}", savedUser.getId());
        return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
    }

    // 🔐 Get all users - ADMIN only
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        log.info("Fetching all users");
        List<User> users = userRepository.findAll();
        log.info("Found {} users", users.size());
        return ResponseEntity.ok(users);
    }

    // 🔐 Get user by ID - ADMIN only
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        log.info("Fetching user with ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User not found with ID: {}", id);
                    return new ResourceNotFoundException("User not found with ID: " + id);
                });
        return ResponseEntity.ok(user);
    }

    // 🔐 Update user by ID (Upsert style) - ADMIN only
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User updatedUser) {
        log.info("Updating user with ID: {}", id);
        return userRepository.findById(id)
                .map(existingUser -> {
                    existingUser.setUsername(updatedUser.getUsername());
                    existingUser.setEmail(updatedUser.getEmail());
                    existingUser.setRole(updatedUser.getRole());
                    User saved = userRepository.save(existingUser);
                    log.info("User updated: ID {}", id);
                    return ResponseEntity.ok(saved);
                })
                .orElseGet(() -> {
                    updatedUser.setId(id);
                    User saved = userRepository.save(updatedUser);
                    log.info("User not found, created new user with ID: {}", id);
                    return new ResponseEntity<>(saved, HttpStatus.CREATED);
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
    public ResponseEntity<List<User>> getLeaderboard() {
        log.info("Fetching top 10 users by reward points");
        List<User> topUsers = userRepository.findTop10ByOrderByRewardPointsDesc();
        log.info("Fetched {} users in leaderboard", topUsers.size());
        return ResponseEntity.ok(topUsers);
    }

    // 🔐 Get current logged-in user's profile
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Fetching profile for logged-in user: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Logged-in user not found: {}", email);
                    return new ResourceNotFoundException("User not found with email: " + email);
                });
        return ResponseEntity.ok(user);
    }
}
