package com.devvault.repository;

import com.devvault.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email); // ✅ CORRECT

    // 🏅 Top 10 users by reward points
    List<User> findTop10ByOrderByRewardPointsDesc();
}

