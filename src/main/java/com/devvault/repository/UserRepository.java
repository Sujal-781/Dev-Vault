package com.devvault.repository;

import com.devvault.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email); // ✅ CORRECT

    // 🔢 Leaderboard: username and total rewardPoints
    @Query("SELECT u.username, SUM(i.rewardPoints) " +
            "FROM User u JOIN Issue i ON u.id = i.assignedTo.id " +
            "GROUP BY u.id, u.username " +
            "ORDER BY SUM(i.rewardPoints) DESC")
    List<Object[]> getLeaderboard();
}
