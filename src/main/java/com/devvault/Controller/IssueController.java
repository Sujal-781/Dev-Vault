package com.devvault.Controller;

import com.devvault.model.Issue;
import com.devvault.model.User;
import com.devvault.repository.IssueRepository;
import com.devvault.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/issues")
public class IssueController {

    @Autowired
    private IssueRepository issueRepository;

    @Autowired
    private UserRepository userRepository;

    // Create new issue
    @PostMapping
    public Issue createIssue(@RequestBody Issue issue) {
        return issueRepository.save(issue);
    }

    // Get all issues
    @GetMapping
    public List<Issue> getAllIssues() {
        return issueRepository.findAll();
    }

    // Get issue by ID
    @GetMapping("/{id}")
    public Issue getIssueById(@PathVariable Long id) {
        return issueRepository.findById(id).orElse(null);
    }

    // Get issues by status
    @GetMapping("/status/{status}")
    public List<Issue> getIssuesByStatus(@PathVariable String status) {
        return issueRepository.findByStatus(status.toUpperCase());
    }

    // Get issues by difficulty
    @GetMapping("/difficulty/{difficulty}")
    public List<Issue> getIssuesByDifficulty(@PathVariable String difficulty) {
        return issueRepository.findByDifficulty(difficulty.toUpperCase());
    }

    // Assign issue to a user
    @PutMapping("/{issueId}/assign/{userId}")
    public Issue assignIssueToUser(@PathVariable Long issueId, @PathVariable Long userId) {
        Issue issue = issueRepository.findById(issueId).orElse(null);
        User user = userRepository.findById(userId).orElse(null);
        if (issue != null && user != null) {
            issue.setAssignedTo(user);
            issue.setStatus("CLAIMED");
            return issueRepository.save(issue);
        }
        return null;
    }
}
