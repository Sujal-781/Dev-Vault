package com.devvault.repository;

import com.devvault.model.Issue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable; // ✅ Correct import
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IssueRepository extends JpaRepository<Issue, Long> {

    // 🔸 Total issues in system
    @Query("SELECT COUNT(i) FROM Issue i")
    long countTotalIssues();

    // 🔸 Total issues assigned to a user
    @Query("SELECT COUNT(i) FROM Issue i WHERE i.assignedTo.id = :userId")
    long countAssignedIssues(@Param("userId") Long userId);

    // 🔸 Total unassigned issues
    @Query("SELECT COUNT(i) FROM Issue i WHERE i.assignedTo IS NULL")
    long countUnassignedIssues();

    // 🔸 Count of issues by difficulty
    @Query("SELECT i.difficulty, COUNT(i) FROM Issue i GROUP BY i.difficulty")
    List<Object[]> countIssuesByDifficulty();

    // 🔸 Total reward points earned by user
    @Query("SELECT SUM(i.rewardPoints) FROM Issue i WHERE i.assignedTo.id = :userId")
    Integer getTotalRewardPoints(@Param("userId") Long userId);

    // 🔸 Overdue issues
    @Query("SELECT i FROM Issue i WHERE i.dueDate < CURRENT_DATE")
    List<Issue> findOverdueIssues();

    // 🔸 Find issues by status
    List<Issue> findByStatus(String upperCase);

    // 🔸 Find issues by difficulty
    List<Issue> findByDifficulty(String difficulty);

    // 🔹 Paginated filtering combinations
    Page<Issue> findByStatusIgnoreCaseAndDifficultyIgnoreCase(String status, String difficulty, Pageable pageable);

    Page<Issue> findByStatusIgnoreCase(String status, Pageable pageable);

    Page<Issue> findByDifficultyIgnoreCase(String difficulty, Pageable pageable);
}
