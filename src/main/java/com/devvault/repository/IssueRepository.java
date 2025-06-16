package com.devvault.repository;

import com.devvault.model.Issue;
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

    @Query("SELECT SUM(i.rewardPoints) FROM Issue i WHERE i.assignedTo.id = :userId")
    Integer getTotalRewardPoints(@Param("userId") Long userId);

    @Query("SELECT i FROM Issue i WHERE i.dueDate < CURRENT_DATE")
    List<Issue> findOverdueIssues();



    List<Issue> findByStatus(String upperCase);
}
