package com.devvault.Controller;

import com.devvault.exception.ResourceNotFoundException;
import com.devvault.model.Issue;
import com.devvault.model.User;
import com.devvault.repository.IssueRepository;
import com.devvault.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/issues")
public class IssueController {

    @Autowired
    private IssueRepository issueRepository;

    @Autowired
    private UserRepository userRepository;

    // 🔐 Create new issue - auto assign to logged-in user (USER role)
    @PreAuthorize("hasRole('USER')")
    @PostMapping
    public ResponseEntity<Issue> createIssue(@RequestBody Issue issue) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        issue.setAssignedTo(user);
        issue.setStatus("CLAIMED");
        Issue savedIssue = issueRepository.save(issue);
        return ResponseEntity.ok(savedIssue);
    }

    // ✅ Filter + Pagination: GET /issues/filter?status=OPEN&difficulty=EASY&page=0&size=5
    @GetMapping("/filter")
    public ResponseEntity<Page<Issue>> filterIssues(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String difficulty,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);

        if (status != null && difficulty != null) {
            return ResponseEntity.ok(issueRepository.findByStatusIgnoreCaseAndDifficultyIgnoreCase(status, difficulty, pageable));
        } else if (status != null) {
            return ResponseEntity.ok(issueRepository.findByStatusIgnoreCase(status, pageable));
        } else if (difficulty != null) {
            return ResponseEntity.ok(issueRepository.findByDifficultyIgnoreCase(difficulty, pageable));
        } else {
            return ResponseEntity.ok(issueRepository.findAll(pageable));
        }
    }

    // 🔓 Get issue by ID - public
    @GetMapping("/{id}")
    public ResponseEntity<Issue> getIssueById(@PathVariable Long id) {
        Issue issue = issueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found with ID: " + id));
        return ResponseEntity.ok(issue);
    }

    // 🔐 Assign issue to any user - only ADMIN
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{issueId}/assign/{userId}")
    public ResponseEntity<Issue> assignIssueToUser(@PathVariable Long issueId, @PathVariable Long userId) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        issue.setAssignedTo(user);
        issue.setStatus("CLAIMED");
        return ResponseEntity.ok(issueRepository.save(issue));
    }

    // 🔐 Update issue - only assigned user or ADMIN
    @PreAuthorize("hasRole('ADMIN') or @issueSecurity.isOwner(#id)")
    @PutMapping("/{id}")
    public ResponseEntity<Issue> updateIssue(@PathVariable Long id, @RequestBody Issue updatedIssue) {
        Issue issue = issueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found"));

        issue.setTitle(updatedIssue.getTitle());
        issue.setDescription(updatedIssue.getDescription());
        issue.setDifficulty(updatedIssue.getDifficulty());

        String prevStatus = issue.getStatus();
        String newStatus = updatedIssue.getStatus();

        if (!"CLOSED".equalsIgnoreCase(prevStatus) && "CLOSED".equalsIgnoreCase(newStatus)) {
            issue.setStatus("CLOSED");

            User assignee = issue.getAssignedTo();
            if (assignee != null) {
                int reward = switch (issue.getDifficulty().toUpperCase()) {
                    case "EASY" -> 10;
                    case "MEDIUM" -> 20;
                    case "HARD" -> 30;
                    default -> 5;
                };
                assignee.setRewardPoints(assignee.getRewardPoints() + reward);
                userRepository.save(assignee);
            }
        } else {
            issue.setStatus(newStatus);
        }

        return ResponseEntity.ok(issueRepository.save(issue));
    }

    // 🔐 Delete issue - only assigned user or ADMIN
    @PreAuthorize("hasRole('ADMIN') or @issueSecurity.isOwner(#id)")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteIssue(@PathVariable Long id) {
        if (!issueRepository.existsById(id)) {
            throw new ResourceNotFoundException("Issue not found with ID: " + id);
        }
        issueRepository.deleteById(id);
        return ResponseEntity.ok("Issue deleted successfully.");
    }
}
