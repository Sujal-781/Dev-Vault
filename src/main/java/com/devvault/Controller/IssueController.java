package com.devvault.Controller;

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

import java.util.List;

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
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            return ResponseEntity.badRequest().build();
        }

        issue.setAssignedTo(user);
        issue.setStatus("CLAIMED");
        Issue savedIssue = issueRepository.save(issue);
        return ResponseEntity.ok(savedIssue);
    }

    // 🔓 Get all issues - public
    @GetMapping
    public List<Issue> getAllIssues() {
        return issueRepository.findAll();
    }

    // ✅ NEW: Filter + Pagination: GET /issues/filter?status=OPEN&difficulty=EASY&page=0&size=5
    @GetMapping("/filter")
    public ResponseEntity<Page<Issue>> filterIssues(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String difficulty,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);

        if (status != null && difficulty != null) {
            return ResponseEntity.ok(
                    issueRepository.findByStatusIgnoreCaseAndDifficultyIgnoreCase(status, difficulty, pageable)
            );
        } else if (status != null) {
            return ResponseEntity.ok(
                    issueRepository.findByStatusIgnoreCase(status, pageable)
            );
        } else if (difficulty != null) {
            return ResponseEntity.ok(
                    issueRepository.findByDifficultyIgnoreCase(difficulty, pageable)
            );
        } else {
            return ResponseEntity.ok(issueRepository.findAll(pageable));
        }
    }

    // 🔓 Get issue by ID - public
    @GetMapping("/{id}")
    public Issue getIssueById(@PathVariable Long id) {
        return issueRepository.findById(id).orElse(null);
    }

    // 🔓 Get issues by status
    @GetMapping("/status/{status}")
    public List<Issue> getIssuesByStatus(@PathVariable String status) {
        return issueRepository.findByStatus(status.toUpperCase());
    }

    // 🔓 Get issues by difficulty
    @GetMapping("/difficulty/{difficulty}")
    public List<Issue> getIssuesByDifficulty(@PathVariable String difficulty) {
        return issueRepository.findByDifficulty(difficulty.toUpperCase());
    }

    // 🔐 Assign issue to any user - only ADMIN
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{issueId}/assign/{userId}")
    public ResponseEntity<Issue> assignIssueToUser(@PathVariable Long issueId, @PathVariable Long userId) {
        Issue issue = issueRepository.findById(issueId).orElse(null);
        User user = userRepository.findById(userId).orElse(null);

        if (issue == null || user == null) {
            return ResponseEntity.notFound().build();
        }

        issue.setAssignedTo(user);
        issue.setStatus("CLAIMED");
        return ResponseEntity.ok(issueRepository.save(issue));
    }

    // 🔐 Update issue - only assigned user or ADMIN
    @PreAuthorize("hasRole('ADMIN') or @issueSecurity.isOwner(#id)")
    @PutMapping("/{id}")
    public ResponseEntity<Issue> updateIssue(@PathVariable Long id, @RequestBody Issue updatedIssue) {
        return issueRepository.findById(id).map(issue -> {
            issue.setTitle(updatedIssue.getTitle());
            issue.setDescription(updatedIssue.getDescription());
            issue.setDifficulty(updatedIssue.getDifficulty());

            // 🏁 Handle CLOSED status and reward points
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
        }).orElse(ResponseEntity.notFound().build());
    }

    // 🔐 Delete issue - only assigned user or ADMIN
    @PreAuthorize("hasRole('ADMIN') or @issueSecurity.isOwner(#id)")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteIssue(@PathVariable Long id) {
        if (issueRepository.existsById(id)) {
            issueRepository.deleteById(id);
            return ResponseEntity.ok("Issue deleted successfully.");
        } else {
            return ResponseEntity.status(404).body("Issue not found.");
        }
    }
}
