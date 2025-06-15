package com.devvault.repository;

import com.devvault.model.Issue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IssueRepository extends JpaRepository<Issue, Long> {
    List<Issue> findByStatus(String status);
    List<Issue> findByDifficulty(String difficulty);
}
